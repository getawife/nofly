package dev.nofly;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class log {

    private final no_fly plugin;
    private final Path data_folder;
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();
    private final Map<UUID, Long> last_alert = new ConcurrentHashMap<>();
    private volatile Path file;
    private final BukkitTask flusher;

    public log(no_fly plugin, Path data_folder) {
        this.plugin = plugin;
        this.data_folder = data_folder;
        reload();
        flusher = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::flush, 100L, 100L);
    }

    public void write(Player player, String kind, int total) {
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        queue.add(Instant.now() + " " + id + " " + player.getName() + " " + kind + " " + total);

        Long previous = last_alert.get(id);
        if (previous != null && now - previous < 3000L) {
            return;
        }
        last_alert.put(id, now);

        if (!plugin.config().punish_alert) {
            return;
        }

        plugin.getLogger().info(player.getName() + " flagged " + kind + " (" + total + ")");
        Component message = build(player, kind, total);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("nofly.alerts")) {
                online.sendMessage(message);
            }
        }
    }

    public void reload() {
        flush();
        Path next;
        try {
            Path configured = Path.of(plugin.config().punish_log_file).getFileName();
            next = configured == null || configured.toString().isBlank() || configured.toString().equals(".")
                    || configured.toString().equals("..")
                    ? data_folder.resolve("flags.log")
                    : data_folder.resolve(configured).normalize();
        } catch (InvalidPathException error) {
            next = data_folder.resolve("flags.log");
        }
        file = next;
        ensure_file(next);
    }

    public void shutdown() {
        flusher.cancel();
        flush();
    }

    public void forget(UUID id) {
        last_alert.remove(id);
    }

    private Component build(Player player, String kind, int total) {
        Component name = Component.text(player.getName(), NamedTextColor.RED)
                .clickEvent(ClickEvent.runCommand("/tp " + player.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("teleport to " + player.getName())));
        return Component.text("[nofly] ", NamedTextColor.DARK_GRAY)
                .append(name)
                .append(Component.text(" flagged ", NamedTextColor.GRAY))
                .append(Component.text(kind, NamedTextColor.YELLOW))
                .append(Component.text(" (" + total + ")", NamedTextColor.DARK_GRAY));
    }

    private void ensure_file(Path target) {
        try {
            Files.createDirectories(data_folder);
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(target)) {
                Files.createFile(target);
            }
        } catch (IOException error) {
            throw new IllegalStateException("unable to create log file", error);
        }
    }

    private void flush() {
        if (queue.isEmpty()) {
            return;
        }
        Path target = file;
        if (target == null) {
            return;
        }
        StringBuilder batch = new StringBuilder();
        String line;
        while ((line = queue.poll()) != null) {
            batch.append(line).append(System.lineSeparator());
        }
        if (batch.isEmpty()) {
            return;
        }
        try {
            Files.writeString(target, batch.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException error) {
            plugin.getLogger().warning("log flush failed: " + error.getMessage());
        }
    }
}
