package dev.nofly;

import com.github.retrooper.packetevents.util.Vector3d;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import java.util.UUID;

public final class move implements Listener {

    private final no_fly plugin;

    public move(no_fly plugin) {
        this.plugin = plugin;
    }

    public void on_flying(Player player, Vector3d position, float yaw, float pitch, boolean on_ground) {
        track track = plugin.state().get(player.getUniqueId());
        long now = System.currentTimeMillis();

        if (position == null) {
            if (track.initialized) {
                track.on_ground = on_ground;
                track.last_on_ground = on_ground;
            }
            return;
        }

        if (!track.initialized) {
            initialize(track, position, on_ground, now);
            return;
        }

        if (now - track.last_join < plugin.config().join_grace_ms) {
            update_last(track, position, on_ground);
            return;
        }

        if (now < track.setback_until) {
            update_last(track, position, on_ground);
            return;
        }

        if (now < track.server_velocity_until) {
            update_last(track, position, on_ground);
            return;
        }

        double dx = position.getX() - track.last_x;
        double dy = position.getY() - track.last_y;
        double dz = position.getZ() - track.last_z;

        if (!finite(dx) || !finite(dy) || !finite(dz)) {
            initialize(track, position, on_ground, now);
            return;
        }

        double horizontal = Math.hypot(dx, dz);
        double max_delta = plugin.config().max_offset_per_tick;
        if (Math.abs(dx) > max_delta || Math.abs(dy) > max_delta || Math.abs(dz) > max_delta) {
            flag(player, track, "delta", now);
            setback(player, track, now);
            return;
        }

        if (exempt.check(player, track, now, plugin.config())) {
            reset_soft(track, position, on_ground);
            return;
        }

        if (on_ground) {
            if (plugin.config().ground_check_on
                    && track.air_ticks > 2
                    && !ground.verify(player, position.getX(), position.getY(), position.getZ())) {
                track.ground_spoof_vl++;
                if (track.ground_spoof_vl >= plugin.config().ground_check_vl) {
                    flag(player, track, "ground_spoof", now);
                    setback(player, track, now);
                    return;
                }
            } else {
                track.ground_spoof_vl = Math.max(0, track.ground_spoof_vl - 1);
            }

            track.ground_ticks++;
            track.air_ticks = 0;
            track.still_ticks = 0;
            track.ascent_ticks = 0;
            track.stalled_ticks = 0;
            track.last_vertical = 0.0;
            track.last_vel_x = 0.0;
            track.last_vel_y = 0.0;
            track.last_vel_z = 0.0;

            if (ground.is_safe(player, position.getX(), position.getY(), position.getZ())) {
                track.setback_x = position.getX();
                track.setback_y = position.getY();
                track.setback_z = position.getZ();
                track.has_setback = true;
            }

            track.vertical_buffer.decay(plugin.config().vertical_decay);
            track.horizontal_buffer.decay(plugin.config().horizontal_decay);
            track.prediction_buffer.decay(plugin.config().prediction_decay);
            update_last(track, position, on_ground);
            return;
        }

        track.air_ticks++;
        track.ground_ticks = 0;

        if (Math.abs(dy) <= plugin.config().horizontal_max_vertical) {
            track.still_ticks++;
        } else {
            track.still_ticks = 0;
        }

        if (Math.abs(dy) >= plugin.config().vertical_stalled_min
                && Math.abs(dy) <= plugin.config().vertical_stalled_max
                && Math.abs(dy - track.last_vertical) <= plugin.config().vertical_stalled_delta) {
            track.stalled_ticks++;
        } else {
            track.stalled_ticks = 0;
        }

        if (dy >= plugin.config().vertical_ascent_min) {
            track.ascent_ticks++;
        } else {
            track.ascent_ticks = 0;
        }

        boolean launch_grace = now < track.launch_grace_until;
        if (track.air_ticks == 1 && dy > 0.30) {
            track.launch_grace_until = now + 600L;
            launch_grace = true;
        }

        double tolerance_scale = 1.0 + Math.min(player.getPing() * plugin.config().ping_scale, 2.0);

        if (!launch_grace && plugin.config().prediction_on) {
            double expected_dy = simulate.next_vertical(track.last_vel_y);
            double expected_dx = simulate.next_horizontal(track.last_vel_x);
            double expected_dz = simulate.next_horizontal(track.last_vel_z);

            double off_x = Math.abs(dx - expected_dx);
            double off_y = Math.abs(dy - expected_dy);
            double off_z = Math.abs(dz - expected_dz);
            double offset = Math.sqrt(off_x * off_x + off_y * off_y + off_z * off_z);

            double prediction_tol = plugin.config().prediction_tolerance * plugin.config().leniency * tolerance_scale;
            if (offset > prediction_tol) {
                double severity = Math.min(1.0, (offset - prediction_tol) * 3.0);
                if (track.prediction_buffer.add(severity, plugin.config().prediction_buffer)) {
                    track.prediction_vl++;
                    flag(player, track, "prediction", now);
                    setback(player, track, now);
                    return;
                }
            } else {
                track.prediction_buffer.decay(plugin.config().prediction_decay);
            }
        }

        if (!launch_grace && plugin.config().vertical_on) {
            double tol = plugin.config().vertical_tolerance * plugin.config().leniency * tolerance_scale;
            double expected = simulate.next_vertical(track.last_vertical);
            double error = Math.abs(dy - expected);
            boolean stall_check = track.still_ticks >= plugin.config().vertical_hover_ticks
                    || track.ascent_ticks >= plugin.config().vertical_ascent_ticks
                    || track.stalled_ticks >= plugin.config().vertical_stalled_ticks;
            if (error > tol && track.air_ticks > plugin.config().vertical_startup_ticks) {
                double severity = Math.max(0.1, Math.min(2.0, (error - tol) * 5.0));
                if (track.vertical_buffer.add(severity, plugin.config().vertical_buffer)) {
                    track.vertical_vl++;
                    flag(player, track, "vertical", now);
                    setback(player, track, now);
                    return;
                }
            } else if (stall_check && track.air_ticks > plugin.config().vertical_startup_ticks) {
                if (track.vertical_buffer.add(0.5, plugin.config().vertical_buffer)) {
                    track.vertical_vl++;
                    flag(player, track, "vertical", now);
                    setback(player, track, now);
                    return;
                }
            } else {
                track.vertical_buffer.decay(plugin.config().vertical_decay);
            }
        }

        if (!launch_grace && plugin.config().horizontal_on) {
            boolean hbad = track.still_ticks >= plugin.config().horizontal_min_ticks
                    && Math.abs(dy) <= plugin.config().horizontal_max_vertical
                    && horizontal >= plugin.config().horizontal_min_horizontal;
            if (hbad) {
                if (track.horizontal_buffer.add(0.5, plugin.config().horizontal_buffer)) {
                    track.horizontal_vl++;
                    flag(player, track, "horizontal", now);
                    setback(player, track, now);
                    return;
                }
            } else {
                track.horizontal_buffer.decay(plugin.config().horizontal_decay);
            }
        }

        track.last_vel_x = dx;
        track.last_vel_y = dy;
        track.last_vel_z = dz;
        track.last_vertical = dy;
        track.last_horizontal = horizontal;
        update_last(track, position, on_ground);
    }

    public void tick(Player player) {
        if (!player.isOnline()) return;
        track track = plugin.state().get(player.getUniqueId());
        if (!track.initialized) return;
        if (track.vl > 0.0) {
            track.vl = Math.max(0.0, track.vl - 0.01);
        }
    }

    private void flag(Player player, track track, String kind, long now) {
        track.total_flags++;
        track.vl += 1.0;
        plugin.log().write(player, kind, track.total_flags);
        if (plugin.config().punish_kick && track.vl >= plugin.config().punish_kick_vl) {
            net.kyori.adventure.text.Component message =
                    net.kyori.adventure.text.Component.text("nofly: " + kind);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) player.kick(message);
            });
        }
    }

    private void setback(Player player, track track, long now) {
        if (!plugin.config().setback_on || !track.has_setback) return;
        track.setback_until = now + plugin.config().setback_grace_ms;
        double tx = track.setback_x;
        double ty = track.setback_y;
        double tz = track.setback_z;
        Location target = new Location(player.getWorld(), tx, ty, tz);
        if (!ground.is_safe(player, tx, ty, tz)) {
            target = player.getLocation();
            track.setback_x = target.getX();
            track.setback_y = target.getY();
            track.setback_z = target.getZ();
        }
        final Location final_target = target;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            player.teleport(final_target, PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.setFallDistance(0f);
            player.setVelocity(new Vector(0, 0, 0));
        });
        track.last_x = final_target.getX();
        track.last_y = final_target.getY();
        track.last_z = final_target.getZ();
        track.last_vel_x = 0.0;
        track.last_vel_y = 0.0;
        track.last_vel_z = 0.0;
        track.air_ticks = 0;
        track.ground_ticks = 0;
        track.still_ticks = 0;
        track.ascent_ticks = 0;
        track.stalled_ticks = 0;
        track.last_vertical = 0.0;
        track.launch_grace_until = 0L;
        track.vertical_buffer.reset();
        track.horizontal_buffer.reset();
        track.prediction_buffer.reset();
    }

    private void initialize(track track, Vector3d position, boolean on_ground, long now) {
        track.last_x = position.getX();
        track.last_y = position.getY();
        track.last_z = position.getZ();
        track.last_vel_x = 0.0;
        track.last_vel_y = 0.0;
        track.last_vel_z = 0.0;
        track.setback_x = position.getX();
        track.setback_y = position.getY();
        track.setback_z = position.getZ();
        track.has_setback = true;
        track.initialized = true;
        track.on_ground = on_ground;
        track.last_on_ground = on_ground;
        track.air_ticks = 0;
        track.ground_ticks = 0;
        track.still_ticks = 0;
        track.ascent_ticks = 0;
        track.stalled_ticks = 0;
        track.last_vertical = 0.0;
        track.last_horizontal = 0.0;
        track.launch_grace_until = 0L;
        track.vertical_buffer.reset();
        track.horizontal_buffer.reset();
        track.prediction_buffer.reset();
    }

    private void reset_soft(track track, Vector3d position, boolean on_ground) {
        track.last_x = position.getX();
        track.last_y = position.getY();
        track.last_z = position.getZ();
        track.last_vel_x = 0.0;
        track.last_vel_y = 0.0;
        track.last_vel_z = 0.0;
        track.air_ticks = 0;
        track.ground_ticks = 0;
        track.still_ticks = 0;
        track.ascent_ticks = 0;
        track.stalled_ticks = 0;
        track.last_vertical = 0.0;
        track.launch_grace_until = 0L;
        track.on_ground = on_ground;
        track.last_on_ground = on_ground;
        track.vertical_buffer.decay(plugin.config().vertical_decay);
        track.horizontal_buffer.decay(plugin.config().horizontal_decay);
        track.prediction_buffer.decay(plugin.config().prediction_decay);
    }

    private void update_last(track track, Vector3d position, boolean on_ground) {
        track.last_x = position.getX();
        track.last_y = position.getY();
        track.last_z = position.getZ();
        track.on_ground = on_ground;
        track.last_on_ground = on_ground;
    }

    private boolean finite(double value) {
        return Double.isFinite(value);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        track track = plugin.state().get(player.getUniqueId());
        Location loc = player.getLocation();
        track.last_x = loc.getX();
        track.last_y = loc.getY();
        track.last_z = loc.getZ();
        track.setback_x = loc.getX();
        track.setback_y = loc.getY();
        track.setback_z = loc.getZ();
        track.has_setback = true;
        track.initialized = true;
        track.last_join = System.currentTimeMillis();
        track.last_teleport = System.currentTimeMillis();
    }

    @EventHandler
    public void respawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getRespawnLocation();
        track track = plugin.state().get(player.getUniqueId());
        track.last_teleport = System.currentTimeMillis();
        track.setback_until = System.currentTimeMillis() + plugin.config().teleport_grace_ms;
        track.setback_x = loc.getX();
        track.setback_y = loc.getY();
        track.setback_z = loc.getZ();
        track.has_setback = true;
        track.initialized = false;
        track.last_vel_x = 0.0;
        track.last_vel_y = 0.0;
        track.last_vel_z = 0.0;
    }

    @EventHandler
    public void world_change(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        track track = plugin.state().get(player.getUniqueId());
        track.last_teleport = System.currentTimeMillis();
        track.setback_until = System.currentTimeMillis() + plugin.config().teleport_grace_ms;
        track.initialized = false;
        track.last_vel_x = 0.0;
        track.last_vel_y = 0.0;
        track.last_vel_z = 0.0;
    }

    @EventHandler
    public void teleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        if (to == null) return;
        track track = plugin.state().get(event.getPlayer().getUniqueId());
        long now = System.currentTimeMillis();
        track.last_teleport = now;
        track.setback_until = now + plugin.config().teleport_grace_ms;
        track.initialized = false;
        track.last_vel_x = 0.0;
        track.last_vel_y = 0.0;
        track.last_vel_z = 0.0;
    }

    @EventHandler
    public void velocity(PlayerVelocityEvent event) {
        track track = plugin.state().get(event.getPlayer().getUniqueId());
        long now = System.currentTimeMillis();
        track.last_velocity = now;
        track.server_velocity_until = now + plugin.config().velocity_grace_ms;
        track.last_vel_x = event.getVelocity().getX();
        track.last_vel_y = event.getVelocity().getY();
        track.last_vel_z = event.getVelocity().getZ();
        track.vertical_buffer.reset();
        track.horizontal_buffer.reset();
        track.prediction_buffer.reset();
    }

    @EventHandler
    public void glide(EntityToggleGlideEvent event) {
        if (!plugin.config().glide_on || !event.isGliding()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        Material chest = Material.AIR;
        if (player.getInventory().getChestplate() != null) {
            chest = player.getInventory().getChestplate().getType();
        }
        if (chest != Material.ELYTRA) {
            event.setCancelled(true);
            return;
        }
        if (player.isOnGround()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        track track = plugin.state().get(player.getUniqueId());
        track.last_damage = System.currentTimeMillis();
        track.vertical_buffer.reset();
        track.horizontal_buffer.reset();
        track.prediction_buffer.reset();
        track.still_ticks = 0;
        track.ascent_ticks = 0;
        track.stalled_ticks = 0;
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        plugin.state().drop(id);
        plugin.log().forget(id);
    }
}