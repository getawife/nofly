package dev.nofly;

public final class track {

    public double last_x;
    public double last_y;
    public double last_z;
    public double last_vel_x;
    public double last_vel_y;
    public double last_vel_z;
    public boolean has_last;

    public double setback_x;
    public double setback_y;
    public double setback_z;
    public boolean has_setback;

    public boolean last_on_ground;
    public boolean on_ground;

    public int air_ticks;
    public int ground_ticks;
    public int still_ticks;
    public int ascent_ticks;
    public int stalled_ticks;

    public double last_vertical;
    public double last_horizontal;

    public int total_flags;
    public double vl;

    public long last_teleport;
    public long last_damage;
    public long last_velocity;
    public long last_effect_apply;
    public long last_join;
    public long setback_until;
    public long launch_grace_until;
    public long server_velocity_until;

    public int ground_spoof_vl;
    public int prediction_vl;
    public int vertical_vl;
    public int horizontal_vl;

    public final buffer vertical_buffer = new buffer();
    public final buffer horizontal_buffer = new buffer();
    public final buffer prediction_buffer = new buffer();

    public boolean initialized;
}