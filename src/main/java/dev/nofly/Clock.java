package dev.nofly;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class clock {

    private final JavaPlugin plugin;
    private final move move;
    private BukkitTask task;

    public clock(JavaPlugin plugin, move move) {
        this.plugin = plugin;
        this.move = move;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            move.tick(player);
        }
    }
}
