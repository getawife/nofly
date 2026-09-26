package dev.nofly;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public final class move implements Listener {

    private static final double finite_limit = 3.0e7;

    private final no_fly plugin;

    public move(no_fly plugin) {
        this.plugin = plugin;
    }

    public void tick(Player player) {
        if (!player.isOnline()) {
            return;
        }

        track track = plugin.state().get(player.getUniqueId());
        Location current = player.getLocation();
        long now = System.currentTimeMillis();
        long sample_nanos = System.nanoTime();

        if (!is_valid(current)) {
            return;
        }

        if (!track.initialized || track.last == null || !same_world(track.last, current)) {
            initialize(track, current, now);
            track.last_sample_nanos = sample_nanos;
            return;
        }

        if (sample_nanos - track.last_sample_nanos > 150_000_000L) {
            reset_motion(track, current);
            track.last_sample_nanos = sample_nanos;
            return;
        }
        track.last_sample_nanos = sample_nanos;

        if (exempt.check(player, track, now, plugin.config())) {
            reset_motion(track, current);
            return;
        }

        if (player.isOnGround()) {
            settle(track, current);
            return;
        }

        double dx = current.getX() - track.last.getX();
        double dy = current.getY() - track.last.getY();
        double dz = current.getZ() - track.last.getZ();
        double horizontal = Math.hypot(dx, dz);

        if (track.air_ticks == 0 && dy > 0.65) {
            track.launch_grace_until = now + 800L;
        }

        track.air_ticks++;
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
        boolean vertical_bad = !launch_grace && vertical_bad(track, dy);
        boolean horizontal_bad = !launch_grace && horizontal_bad(track, horizontal, dy);

        if (vertical_bad) {
            double severity = violation_amount(track, dy);
            if (track.vertical_buffer.add(severity, plugin.config().vertical_buffer)) {
                flag(player, track, "vertical", now);
                return;
            }
        } else {
            track.vertical_buffer.decay(plugin.config().vertical_decay);
        }

        if (horizontal_bad) {
            if (track.horizontal_buffer.add(0.5, plugin.config().horizontal_buffer)) {
                flag(player, track, "horizontal", now);
                return;
            }
        } else {
            track.horizontal_buffer.decay(plugin.config().horizontal_decay);
        }

        track.last_vertical = dy;
        track.last = current.clone();
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        track track = plugin.state().get(player.getUniqueId());
        initialize(track, player.getLocation(), System.currentTimeMillis());
    }

    @EventHandler
    public void respawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = event.getRespawnLocation();
        long now = System.currentTimeMillis();
        track track = plugin.state().get(player.getUniqueId());
        track.last_teleport = now;
        track.setback_until = now + plugin.config().teleport_grace_ms;
        initialize(track, location, now);
    }

    @EventHandler
    public void world_change(org.bukkit.event.player.PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        long now = System.currentTimeMillis();
        track track = plugin.state().get(player.getUniqueId());
        track.last_teleport = now;
        track.setback_until = now + plugin.config().teleport_grace_ms;
        initialize(track, location, now);
    }

    @EventHandler
    public void teleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        track track = plugin.state().get(event.getPlayer().getUniqueId());
        long now = System.currentTimeMillis();
        track.last_teleport = now;
        track.setback_until = now + plugin.config().teleport_grace_ms;
        initialize(track, to, now);
    }

    @EventHandler
    public void velocity(org.bukkit.event.player.PlayerVelocityEvent event) {
        track track = plugin.state().get(event.getPlayer().getUniqueId());
        track.last_velocity = System.currentTimeMillis();
        track.vertical_buffer.reset();
        track.horizontal_buffer.reset();
    }

    @EventHandler
    public void glide(EntityToggleGlideEvent event) {
        if (!plugin.config().glide_on || !event.isGliding()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        boolean no_elytra = player.getInventory().getChestplate() == null
                || player.getInventory().getChestplate().getType() != Material.ELYTRA;
        if (no_elytra || player.isOnGround() || player.isFlying()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        track track = plugin.state().get(player.getUniqueId());
        track.last_damage = System.currentTimeMillis();
        track.vertical_buffer.reset();
        track.horizontal_buffer.reset();
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

    private boolean vertical_bad(track track, double dy) {
        if (!plugin.config().vertical_on || track.air_ticks <= plugin.config().vertical_startup_ticks) {
            return false;
        }

        double expected = simulate.next_vertical(track.last_vertical);
        double tolerance = plugin.config().vertical_tolerance * plugin.config().leniency;
        double error = Math.abs(dy - expected);

        if (track.still_ticks >= plugin.config().vertical_hover_ticks) {
            return true;
        }

        if (track.ascent_ticks >= plugin.config().vertical_ascent_ticks) {
            return true;
        }

        if (track.stalled_ticks >= plugin.config().vertical_stalled_ticks) {
            return true;
        }

        return error > tolerance;
    }

    private boolean horizontal_bad(track track, double horizontal, double dy) {
        if (!plugin.config().horizontal_on) {
            return false;
        }
        return track.still_ticks >= plugin.config().horizontal_min_ticks
                && Math.abs(dy) <= plugin.config().horizontal_max_vertical
                && horizontal >= plugin.config().horizontal_min_horizontal;
    }

    private double violation_amount(track track, double dy) {
        double expected = simulate.next_vertical(track.last_vertical);
        double tolerance = plugin.config().vertical_tolerance * plugin.config().leniency;
        double excess = Math.abs(dy - expected) - tolerance;
        return Math.max(0.5, Math.min(2.0, excess * 5.0));
    }

    private void flag(Player player, track track, String kind, long now) {
        track.total_flags++;
        plugin.log().write(player, kind, track.total_flags);
        track.vertical_buffer.reset();
        track.horizontal_buffer.reset();
        track.still_ticks = 0;
        track.ascent_ticks = 0;

        if (!plugin.config().setback_on || track.setback == null) {
            return;
        }

        track.setback_until = now + plugin.config().setback_grace_ms;
        Location setback = track.setback.clone();
        player.teleport(setback, PlayerTeleportEvent.TeleportCause.PLUGIN);
        track.last = setback.clone();
        track.air_ticks = 0;
        track.last_vertical = 0.0;
        track.launch_grace_until = 0L;
    }

    private void initialize(track track, Location location, long now) {
        Location copy = location.clone();
        track.last = copy;
        track.setback = copy.clone();
        track.initialized = true;
        track.air_ticks = 0;
        track.still_ticks = 0;
        track.ascent_ticks = 0;
        track.stalled_ticks = 0;
        track.last_vertical = 0.0;
        track.vertical_buffer.reset();
        track.horizontal_buffer.reset();
        track.last_teleport = now;
        track.last_sample_nanos = System.nanoTime();
        track.launch_grace_until = 0L;
    }

    private void reset_motion(track track, Location current) {
        track.last = current.clone();
        if (current.getWorld() != null && track.setback == null) {
            track.setback = current.clone();
        }
        track.air_ticks = 0;
        track.still_ticks = 0;
        track.ascent_ticks = 0;
        track.stalled_ticks = 0;
        track.last_vertical = 0.0;
        track.launch_grace_until = 0L;
        track.vertical_buffer.decay(plugin.config().vertical_decay);
        track.horizontal_buffer.decay(plugin.config().horizontal_decay);
    }

    private void settle(track track, Location current) {
        track.setback = current.clone();
        track.last = current.clone();
        track.air_ticks = 0;
        track.still_ticks = 0;
        track.ascent_ticks = 0;
        track.stalled_ticks = 0;
        track.last_vertical = 0.0;
        track.vertical_buffer.reset();
        track.horizontal_buffer.reset();
    }

    private boolean same_world(Location first, Location second) {
        return first.getWorld() != null && first.getWorld().equals(second.getWorld());
    }

    private boolean is_valid(Location location) {
        return location.getWorld() != null
                && Double.isFinite(location.getX())
                && Double.isFinite(location.getY())
                && Double.isFinite(location.getZ())
                && Math.abs(location.getX()) <= finite_limit
                && Math.abs(location.getZ()) <= finite_limit;
    }
}
