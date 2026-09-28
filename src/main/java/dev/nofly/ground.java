package dev.nofly;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class ground {

    private ground() {
    }

    public static boolean verify(Player player, double x, double y, double z) {
        World world = player.getWorld();
        if (world == null) return false;
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) return false;

        int block_x = (int) Math.floor(x);
        int block_z = (int) Math.floor(z);
        int block_y = (int) Math.floor(y - 0.02);

        if (block_y < world.getMinHeight() - 1 || block_y > world.getMaxHeight()) return false;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block block = world.getBlockAt(block_x + dx, block_y, block_z + dz);
                if (is_solid(block.getType())) return true;
            }
        }
        return false;
    }

    public static boolean is_safe(Player player, double x, double y, double z) {
        World world = player.getWorld();
        if (world == null) return false;
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) return false;

        int block_x = (int) Math.floor(x);
        int block_z = (int) Math.floor(z);
        int block_y = (int) Math.floor(y);

        if (block_y <= world.getMinHeight()) return false;

        Block below = world.getBlockAt(block_x, block_y - 1, block_z);
        Block feet = world.getBlockAt(block_x, block_y, block_z);
        Block head = world.getBlockAt(block_x, block_y + 1, block_z);

        if (!is_solid(below.getType())) return false;
        if (is_dangerous(feet.getType())) return false;
        if (is_dangerous(head.getType())) return false;
        if (is_solid(feet.getType())) return false;
        if (is_solid(head.getType())) return false;
        return true;
    }

    private static boolean is_solid(Material material) {
        if (material == null) return false;
        if (material.isAir()) return false;
        return material.isSolid();
    }

    private static boolean is_dangerous(Material material) {
        if (material == null) return false;
        return material == Material.LAVA
                || material == Material.FIRE
                || material == Material.SOUL_FIRE
                || material == Material.MAGMA_BLOCK
                || material == Material.CACTUS
                || material == Material.SWEET_BERRY_BUSH
                || material == Material.POWDER_SNOW
                || material == Material.WITHER_ROSE;
    }
}