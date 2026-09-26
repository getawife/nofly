package dev.nofly;

public final class simulate {

    private simulate() {
    }

    public static double next_vertical(double previous_vertical) {
        return previous_vertical * 0.98 - 0.08;
    }
}
