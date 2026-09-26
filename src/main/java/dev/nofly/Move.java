package dev.nofly;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class Move implements Listener {

    private final NoFly plugin;

    public Move(NoFly plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void move(PlayerMoveEvent event) {
        Config config = plugin.config();
        Player player = event.getPlayer();
        Track track = plugin.state().get(player.getUniqueId());
        long now = System.currentTimeMillis();

        Location from = event.getFrom();
        Location to = event.getTo();

        if (Exempt.check(player, track, now)) {
            track.setback = from;
            return;
        }

        double dy = to.getY() - from.getY();
        double dxz = Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ());

        if (config.vertical_on) {
            double expected = Simulate.vertical(player);
            double slack = config.vertical_tolerance * config.leniency;
            double excess = Math.abs(dy - expected) - slack;
            if (excess > 0) {
                track.vertical_buffer += excess;
                if (track.vertical_buffer > config.vertical_buffer) {
                    punish(player, track, "vertical");
                    track.vertical_buffer = 0;
                }
            } else if (track.vertical_buffer > 0) {
                track.vertical_buffer -= config.vertical_decay;
                if (track.vertical_buffer < 0) {
                    track.vertical_buffer = 0;
                }
            }
        }

        if (config.horizontal_on) {
            if (Math.abs(dy) < config.horizontal_max_vertical
                    && dxz > config.horizontal_min_horizontal) {
                track.horizontal_ticks++;
                if (track.horizontal_ticks > config.horizontal_min_ticks) {
                    punish(player, track, "horizontal");
                    track.horizontal_ticks = 0;
                }
            } else if (track.horizontal_ticks > 0) {
                track.horizontal_ticks--;
            }
        }

        track.setback = from;
    }

    @EventHandler
    public void teleport(PlayerTeleportEvent event) {
        Track track = plugin.state().get(event.getPlayer().getUniqueId());
        track.last_teleport = System.currentTimeMillis();
        track.setback = event.getTo();
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            Track track = plugin.state().get(player.getUniqueId());
            track.last_damage = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        plugin.state().drop(event.getPlayer().getUniqueId());
    }

    private void punish(Player player, Track track, String kind) {
        Config config = plugin.config();
        track.total_flags++;

        if (config.punish_alert) {
            plugin.getLogger().info(player.getName() + " flagged " + kind);
        }
        if (config.punish_setback && track.setback != null) {
            player.teleport(track.setback);
            track.setback_until = System.currentTimeMillis() + 1000;
        }
        if (config.punish_kick_after > 0 && track.total_flags >= config.punish_kick_after) {
            player.kickPlayer("fly check");
        }
    }
}