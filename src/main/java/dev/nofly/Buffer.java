package dev.nofly;

public final class buffer {

    private double value;

    public boolean add(double amount, double threshold) {
        if (!Double.isFinite(amount) || !Double.isFinite(threshold)) {
            return false;
        }
        value += Math.max(0.0, amount);
        return value >= threshold;
    }

    public void decay(double amount) {
        if (!Double.isFinite(amount)) {
            return;
        }
        value = Math.max(0.0, value - Math.max(0.0, amount));
    }

    public void reset() {
        value = 0.0;
    }

    public double value() {
        return value;
    }
}
