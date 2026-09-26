package dev.nofly;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class State {

    private final Map<UUID, Track> tracks = new ConcurrentHashMap<>();

    public Track get(UUID id) {
        return tracks.computeIfAbsent(id, key -> new Track());
    }

    public void drop(UUID id) {
        tracks.remove(id);
    }
}