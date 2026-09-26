package dev.nofly;

import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.UUID;

public final class Log {

    private final Path file;

    public Log(Path dataFolder) {
        this.file = dataFolder.resolve("flags.log");
        try {
            Files.createDirectories(dataFolder);
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
        } catch (IOException error) {
            throw new RuntimeException(error);
        }
    }

    public void write(Player player, String kind, int total) {
        write_raw(player.getUniqueId(), kind, total);
        if (NoFly.get().config().punish_alert) {
            NoFly.get().getLogger().info(player.getName() + " " + kind + " " + total);
        }
    }

    public void write_raw(UUID id, String kind, int total) {
        String line = Instant.now() + " " + id + " " + kind + " " + total + System.lineSeparator();
        try {
            Files.writeString(file, line, StandardOpenOption.APPEND);
        } catch (IOException error) {
            NoFly.get().getLogger().warning("log write failed: " + error.getMessage());
        }
    }
}