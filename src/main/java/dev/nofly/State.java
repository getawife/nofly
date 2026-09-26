package dev.nofly;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class state {

    private final Map<UUID, track> tracks = new ConcurrentHashMap<>();

    public track get(UUID id) {
        return tracks.computeIfAbsent(id, key -> new track());
    }

    public void drop(UUID id) {
        tracks.remove(id);
    }
}
