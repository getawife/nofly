package dev.nofly;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Config {

    private final JavaPlugin plugin;

    public double leniency;

    public boolean vertical_on;
    public double vertical_tolerance;
    public double vertical_buffer;
    public double vertical_decay;

    public boolean horizontal_on;
    public int horizontal_min_ticks;
    public double horizontal_max_vertical;
    public double horizontal_min_horizontal;

    public boolean glide_on;
    public double glide_speed_tolerance;
    public double glide_buffer;
    public double glide_decay;

    public boolean timer_on;
    public long timer_min_ms;
    public int timer_buffer;

    public long effects_max_tracked_ms;

    public boolean punish_alert;
    public String punish_log_file;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        load();
    }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration file = plugin.getConfig();

        leniency = file.getDouble("leniency", 1.15);

        vertical_on = file.getBoolean("vertical.enabled", true);
        vertical_tolerance = file.getDouble("vertical.tolerance", 0.08);
        vertical_buffer = file.getDouble("vertical.buffer", 2.0);
        vertical_decay = file.getDouble("vertical.decay", 0.5);

        horizontal_on = file.getBoolean("horizontal.enabled", true);
        horizontal_min_ticks = file.getInt("horizontal.min_ticks", 10);
        horizontal_max_vertical = file.getDouble("horizontal.max_vertical", 0.01);
        horizontal_min_horizontal = file.getDouble("horizontal.min_horizontal", 0.1);

        glide_on = file.getBoolean("glide.enabled", true);
        glide_speed_tolerance = file.getDouble("glide.speed_tolerance", 0.12);
        glide_buffer = file.getDouble("glide.buffer", 2.5);
        glide_decay = file.getDouble("glide.decay", 0.5);

        timer_on = file.getBoolean("timer.enabled", true);
        timer_min_ms = file.getLong("timer.min_ms", 40);
        timer_buffer = file.getInt("timer.buffer", 20);

        effects_max_tracked_ms = file.getLong("effects.max_tracked_ms", 600000L);

        punish_alert = file.getBoolean("punish.alert", true);
        punish_log_file = file.getString("punish.log_file", "flags.log");
    }
}