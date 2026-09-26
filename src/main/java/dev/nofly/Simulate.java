package dev.nofly;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public final class Simulate {

    private static final double gravity = 0.08;
    private static final double drag = 0.98;
    private static final double fluid_drag = 0.8;
    private static final double levitation = 0.05;
    private static final double slow_fall = 0.5;

    private Simulate() {
    }

    public static double vertical(Player player) {
        if (player.isOnGround()) {
            return 0;
        }
        if (player.isGliding()) {
            return 0;
        }
        if (player.isInWater() || player.isInLava()) {
            return (player.getVelocity().getY() - gravity) * fluid_drag;
        }
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) {
            return levitation;
        }
        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
            return (player.getVelocity().getY() - gravity) * slow_fall;
        }
        return (player.getVelocity().getY() - gravity) * drag;
    }

    public static double friction(Block block) {
        Material type = block.getType();
        if (type == Material.BLUE_ICE) {
            return 0.989;
        }
        if (type == Material.ICE || type == Material.PACKED_ICE) {
            return 0.98;
        }
        if (type == Material.SLIME_BLOCK) {
            return 0.8;
        }
        return 0.6;
    }
}