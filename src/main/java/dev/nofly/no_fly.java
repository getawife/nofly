package dev.nofly;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public final class no_fly extends JavaPlugin {

    private config config;
    private state state;
    private clock clock;
    private log log;
    private move move;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        config = new config(this);
        state = new state();
        move = new move(this);
        clock = new clock(this, move);
        log = new log(this, getDataFolder().toPath());

        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new packets());

        getServer().getPluginManager().registerEvents(new effects(this), this);
        getServer().getPluginManager().registerEvents(move, this);

        admin admin = new admin(this);
        var command = getCommand("nofly");
        if (command != null) {
            command.setExecutor(admin);
            command.setTabCompleter(admin);
        }

        clock.start();
    }

    @Override
    public void onDisable() {
        if (clock != null) {
            clock.stop();
        }
        if (log != null) {
            log.shutdown();
        }
        PacketEvents.getAPI().terminate();
    }

    public config config() {
        return config;
    }

    public state state() {
        return state;
    }

    public clock clock() {
        return clock;
    }

    public log log() {
        return log;
    }
}
