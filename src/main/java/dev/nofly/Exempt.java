package dev.nofly;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public final class exempt {

    private exempt() {
    }

    public static boolean check(Player player, track track, long now, config config) {
        if (player.hasPermission("nofly.bypass")) return true;

        GameMode mode = player.getGameMode();
        if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) return true;

        if (player.isInsideVehicle()) return true;
        if (player.isInWaterOrBubbleColumn()) return true;
        if (player.isInLava()) return true;
        if (player.isClimbing()) return true;
        if (player.isInPowderedSnow()) return true;

        if (player.isRiptiding()) return true;

        if (player.isGliding()) {
            ItemStack chest = player.getInventory().getChestplate();
            if (chest != null && chest.getType() == Material.ELYTRA) return true;
        }

        if (!player.hasGravity()) return true;

        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) return true;
        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) return true;

        if (on_special_block(player)) return true;

        if (now - track.last_teleport < config.teleport_grace_ms) return true;
        if (now - track.last_damage < config.damage_grace_ms) return true;
        if (now - track.last_velocity < config.velocity_grace_ms) return true;
        if (now - track.last_effect_apply < config.effects_grace_ms) return true;

        return false;
    }

    private static boolean on_special_block(Player player) {
        Block below = player.getLocation().subtract(0, 0.15, 0).getBlock();
        Material type = below.getType();
        return type == Material.SLIME_BLOCK
                || type == Material.HONEY_BLOCK
                || type == Material.SCAFFOLDING
                || type == Material.COBWEB
                || type == Material.BUBBLE_COLUMN;
    }
}