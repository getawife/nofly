package dev.nofly;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public final class Clock {

    private final JavaPlugin plugin;
    private BukkitTask task;
    private int counter = 1;

    public Clock(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    private void tick() {
        if (!NoFly.get().config().timer_on) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            int id = counter++;
            Track track = NoFly.get().state().get(player.getUniqueId());
            track.pending.put(id, System.nanoTime());
            WrapperPlayServerWindowConfirmation packet =
                    new WrapperPlayServerWindowConfirmation(0, (short) id, false);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        }
    }

    public void confirm(UUID id, int action) {
        Track track = NoFly.get().state().get(id);
        Long sent = track.pending.remove(action);
        if (sent == null) {
            return;
        }
        long elapsed = (System.nanoTime() - sent) / 1_000_000L;
        if (elapsed < NoFly.get().config().timer_min_ms) {
            track.timer_flags++;
        } else if (track.timer_flags > 0) {
            track.timer_flags--;
        }
        if (track.timer_flags > NoFly.get().config().timer_buffer) {
            NoFly.get().log().write_raw(id, "timer", track.timer_flags);
            track.timer_flags = 0;
        }
    }
}