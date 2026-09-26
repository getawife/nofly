package dev.nofly;

import org.bukkit.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Track {

    public Location setback;
    public double vertical_buffer;
    public int horizontal_ticks;
    public int timer_flags;
    public int total_flags;
    public long last_teleport;
    public long last_damage;
    public long setback_until;
    public final Map<Integer, Long> pending = new ConcurrentHashMap<>();
}