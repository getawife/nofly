package dev.nofly;

public final class Buffer {

    private double value;
    private final double threshold;
    private final double decay;

    public Buffer(double threshold, double decay) {
        this.threshold = threshold;
        this.decay = decay;
    }

    public boolean add(double amount) {
        value += amount;
        return value >= threshold;
    }

    public void clean() {
        value = Math.max(0, value - decay);
    }

    public void reset() {
        value = 0;
    }

    public double value() {
        return value;
    }
}