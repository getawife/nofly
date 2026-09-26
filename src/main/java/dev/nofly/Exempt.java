package dev.nofly;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public final class exempt {

    private exempt() {
    }

    public static boolean check(Player player, track track, long now, config config) {
        if (player.hasPermission("nofly.bypass")) {
            return true;
        }
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return true;
        }
        if (player.isFlying() || player.isGliding() || player.isRiptiding()) {
            return true;
        }
        if (player.isInsideVehicle() || player.isInWaterOrBubbleColumn() || player.isInLava() || player.isClimbing()) {
            return true;
        }
        if (!player.hasGravity()) {
            return true;
        }
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)
                || player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
            return true;
        }
        if (player.isInPowderedSnow()) {
            return true;
        }
        if (now - track.last_teleport < config.teleport_grace_ms) {
            return true;
        }
        if (now - track.last_damage < config.damage_grace_ms) {
            return true;
        }
        if (now - track.last_velocity < config.velocity_grace_ms) {
            return true;
        }
        if (now - track.last_effect_apply < config.effects_grace_ms) {
            return true;
        }
        return now < track.setback_until;
    }
}
