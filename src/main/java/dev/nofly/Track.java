package dev.nofly;

import org.bukkit.Location;

public final class track {

    public Location last;
    public Location setback;
    public final buffer vertical_buffer = new buffer();
    public final buffer horizontal_buffer = new buffer();
    public int air_ticks;
    public int still_ticks;
    public int ascent_ticks;
    public int stalled_ticks;
    public double last_vertical;
    public int total_flags;
    public long last_teleport;
    public long last_damage;
    public long last_velocity;
    public long last_effect_apply;
    public long setback_until;
    public long last_sample_nanos;
    public long launch_grace_until;
    public boolean initialized;
}
