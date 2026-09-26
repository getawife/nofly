package dev.nofly;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;

public final class effects implements Listener {

    private final no_fly plugin;

    public effects(no_fly plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void potion(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED
                && event.getAction() != EntityPotionEffectEvent.Action.CHANGED) {
            return;
        }
        track track = plugin.state().get(player.getUniqueId());
        track.last_effect_apply = System.currentTimeMillis();
        track.vertical_buffer.reset();
        track.horizontal_buffer.reset();
    }
}
