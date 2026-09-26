package dev.nofly;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public final class Move implements Listener {

    private final NoFly plugin;

    public Move(NoFly plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void teleport(PlayerTeleportEvent event) {
        Track track = plugin.state().get(event.getPlayer().getUniqueId());
        track.last_teleport = System.currentTimeMillis();
        track.setback = event.getTo();
        track.last = event.getTo();
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
        UUID id = event.getPlayer().getUniqueId();
        plugin.state().drop(id);
        plugin.log().forget(id);
    }
}