package dev.nofly;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class config {

    private final JavaPlugin plugin;

    public double leniency;
    public double max_offset_per_tick;
    public double ping_scale;

    public boolean vertical_on;
    public double vertical_tolerance;
    public int vertical_startup_ticks;
    public int vertical_hover_ticks;
    public int vertical_ascent_ticks;
    public int vertical_stalled_ticks;
    public double vertical_stalled_min;
    public double vertical_stalled_max;
    public double vertical_stalled_delta;
    public double vertical_ascent_min;
    public double vertical_buffer;
    public double vertical_decay;

    public boolean horizontal_on;
    public int horizontal_min_ticks;
    public double horizontal_max_vertical;
    public double horizontal_min_horizontal;
    public double horizontal_buffer;
    public double horizontal_decay;

    public boolean prediction_on;
    public double prediction_tolerance;
    public double prediction_buffer;
    public double prediction_decay;

    public boolean ground_check_on;
    public int ground_check_vl;

    public boolean glide_on;

    public long effects_grace_ms;
    public long damage_grace_ms;
    public long velocity_grace_ms;
    public long teleport_grace_ms;
    public long setback_grace_ms;
    public long join_grace_ms;
    public boolean setback_on;

    public boolean punish_alert;
    public boolean punish_kick;
    public int punish_kick_vl;
    public String punish_log_file;

    public config(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        load();
    }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration file = plugin.getConfig();

        leniency = clamp(file.getDouble("leniency", 1.10), 0.5, 3.0);
        max_offset_per_tick = clamp(file.getDouble("max_offset_per_tick", 4.0), 0.5, 20.0);
        ping_scale = clamp(file.getDouble("ping_scale", 0.004), 0.0, 0.05);

        vertical_on = file.getBoolean("vertical.enabled", true);
        vertical_tolerance = clamp(file.getDouble("vertical.tolerance", 0.02), 0.001, 0.5);
        vertical_startup_ticks = clamp(file.getInt("vertical.startup_ticks", 2), 1, 6);
        vertical_hover_ticks = clamp(file.getInt("vertical.hover_ticks", 6), 3, 20);
        vertical_ascent_ticks = clamp(file.getInt("vertical.ascent_ticks", 12), 5, 20);
        vertical_stalled_ticks = clamp(file.getInt("vertical.stalled_ticks", 10), 5, 20);
        vertical_stalled_min = clamp(file.getDouble("vertical.stalled_min", 0.08), 0.04, 0.5);
        vertical_stalled_max = clamp(file.getDouble("vertical.stalled_max", 0.5), 0.2, 1.0);
        vertical_stalled_delta = clamp(file.getDouble("vertical.stalled_delta", 0.015), 0.005, 0.05);
        vertical_ascent_min = clamp(file.getDouble("vertical.ascent_min", 0.08), 0.02, 0.3);
        vertical_buffer = clamp(file.getDouble("vertical.buffer", 3.0), 1.0, 10.0);
        vertical_decay = clamp(file.getDouble("vertical.decay", 0.4), 0.05, 2.0);

        horizontal_on = file.getBoolean("horizontal.enabled", true);
        horizontal_min_ticks = clamp(file.getInt("horizontal.min_ticks", 10), 5, 40);
        horizontal_max_vertical = clamp(file.getDouble("horizontal.max_vertical", 0.03), 0.01, 0.15);
        horizontal_min_horizontal = clamp(file.getDouble("horizontal.min_horizontal", 0.12), 0.05, 0.5);
        horizontal_buffer = clamp(file.getDouble("horizontal.buffer", 3.0), 1.0, 10.0);
        horizontal_decay = clamp(file.getDouble("horizontal.decay", 0.3), 0.05, 2.0);

        prediction_on = file.getBoolean("prediction.enabled", true);
        prediction_tolerance = clamp(file.getDouble("prediction.tolerance", 0.03), 0.005, 1.0);
        prediction_buffer = clamp(file.getDouble("prediction.buffer", 4.0), 1.0, 20.0);
        prediction_decay = clamp(file.getDouble("prediction.decay", 0.2), 0.01, 2.0);

        ground_check_on = file.getBoolean("ground.enabled", true);
        ground_check_vl = clamp(file.getInt("ground.vl", 4), 1, 20);

        glide_on = file.getBoolean("glide.enabled", true);

        effects_grace_ms = clamp(file.getLong("effects.grace_ms", 1200L), 200L, 10000L);
        damage_grace_ms = clamp(file.getLong("damage_grace_ms", 500L), 0L, 10000L);
        velocity_grace_ms = clamp(file.getLong("velocity_grace_ms", 500L), 100L, 5000L);
        teleport_grace_ms = clamp(file.getLong("teleport_grace_ms", 800L), 250L, 10000L);
        setback_grace_ms = clamp(file.getLong("setback_grace_ms", 500L), 100L, 5000L);
        join_grace_ms = clamp(file.getLong("join_grace_ms", 3000L), 0L, 30000L);
        setback_on = file.getBoolean("setback.enabled", true);

        punish_alert = file.getBoolean("punish.alert", true);
        punish_kick = file.getBoolean("punish.kick", false);
        punish_kick_vl = clamp(file.getInt("punish.kick_vl", 60), 5, 1000);
        punish_log_file = file.getString("punish.log_file", "flags.log");
        if (punish_log_file == null || punish_log_file.isBlank()) punish_log_file = "flags.log";
    }

    private double clamp(double value, double min, double max) {
        if (!Double.isFinite(value)) return min;
        return Math.max(min, Math.min(max, value));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }
}