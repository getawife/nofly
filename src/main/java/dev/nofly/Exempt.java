package dev.nofly;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public final class Exempt {

    private Exempt() {
    }

    public static boolean check(Player player, Track track, long now) {
        if (player.hasPermission("nofly.bypass")) {
            return true;
        }
        if (player.isFlying() || player.getAllowFlight()) {
            return true;
        }
        if (player.isGliding()) {
            return true;
        }
        if (player.isInsideVehicle()) {
            return true;
        }
        if (player.isInWater() || player.isInLava()) {
            return true;
        }
        if (player.isOnGround()) {
            return true;
        }
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) {
            return true;
        }
        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
            return true;
        }
        if (now - track.last_teleport < 1000) {
            return true;
        }
        if (now - track.last_damage < 2000) {
            return true;
        }
        if (now < track.setback_until) {
            return true;
        }
        return false;
    }
}