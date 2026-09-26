package dev.nofly;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public final class NoFly extends JavaPlugin {

    private static NoFly instance;

    private Config config;
    private State state;
    private Clock clock;
    private Log log;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        instance = this;
        config = new Config(this);
        state = new State();
        clock = new Clock(this);
        log = new Log(this, getDataFolder().toPath());

        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new Packets(this));

        getServer().getPluginManager().registerEvents(new Effects(this), this);
        getServer().getPluginManager().registerEvents(new Move(this), this);
        getServer().getPluginManager().registerEvents(new Admin(this), this);

        getCommand("nofly").setExecutor(new Admin(this));
        getCommand("nofly").setTabCompleter(new Admin(this));

        clock.start();
    }

    @Override
    public void onDisable() {
    clock.stop();
    log.shutdown();
    PacketEvents.getAPI().terminate();
    }
    public static NoFly get() {
        return instance;
    }

    public Config config() {
        return config;
    }

    public State state() {
        return state;
    }

    public Clock clock() {
        return clock;
    }

    public Log log() {
        return log;
    }
}