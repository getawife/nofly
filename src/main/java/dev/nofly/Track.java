package dev.nofly;

import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Track {

    public Location last;
    public Location setback;
    public double vertical_buffer;
    public double glide_buffer;
    public int horizontal_ticks;
    public int timer_flags;
    public int total_flags;
    public long last_teleport;
    public long last_damage;
    public long last_effect_apply;
    public int effect_ticks_left;
    public long pending_move;
    public final Map<Integer, Long> pending = new ConcurrentHashMap<>();
    public final Map<Integer, Long> recent = new ConcurrentHashMap<>();
}