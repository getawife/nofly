package dev.nofly;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Log {

    private static final long alert_gap_ms = 3000;
    private static final long flush_ticks = 100;

    private final Path file;
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();
    private final Map<UUID, Long> last_alert = new ConcurrentHashMap<>();
    private final BukkitTask flusher;

    public Log(JavaPlugin plugin, Path dataFolder) {
        this.file = dataFolder.resolve("flags.log");
        try {
            Files.createDirectories(dataFolder);
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
        } catch (IOException error) {
            throw new RuntimeException(error);
        }
        flusher = Bukkit.getScheduler()
                .runTaskTimerAsynchronously(plugin, this::flush, flush_ticks, flush_ticks);
    }

    public void write(Player player, String kind, int total) {
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();

        queue.add(Instant.now() + " " + id + " " + player.getName() + " " + kind + " " + total);

        Long prev = last_alert.get(id);
        if (prev != null && now - prev < alert_gap_ms) {
            return;
        }
        last_alert.put(id, now);

        if (!NoFly.get().config().punish_alert) {
            return;
        }

        String console = player.getName() + " flagged " + kind + " (" + total + ")";
        NoFly.get().getLogger().info(console);

        Component message = build(player, kind, total);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("nofly.alerts")) {
                online.sendMessage(message);
            }
        }
    }

    public void write_raw(UUID id, String kind, int total) {
        queue.add(Instant.now() + " " + id + " " + kind + " " + total);
    }

    public void forget(UUID id) {
        last_alert.remove(id);
    }

    public void shutdown() {
        flusher.cancel();
        flush();
    }

    private Component build(Player player, String kind, int total) {
        Component name = Component.text(player.getName(), NamedTextColor.RED)
                .clickEvent(ClickEvent.runCommand("/tp " + player.getName()))
                .hoverEvent(HoverEvent.showText(
                        Component.text("teleport to " + player.getName())));
        return Component.text("[nofly] ", NamedTextColor.DARK_GRAY)
                .append(name)
                .append(Component.text(" flagged ", NamedTextColor.GRAY))
                .append(Component.text(kind, NamedTextColor.YELLOW))
                .append(Component.text(" (" + total + ")", NamedTextColor.DARK_GRAY));
    }

    private void flush() {
        if (queue.isEmpty()) {
            return;
        }
        StringBuilder batch = new StringBuilder();
        String line;
        while ((line = queue.poll()) != null) {
            batch.append(line).append(System.lineSeparator());
        }
        try {
            Files.writeString(file, batch.toString(), StandardOpenOption.APPEND);
        } catch (IOException error) {
            NoFly.get().getLogger().warning("log flush failed: " + error.getMessage());
        }
    }
}