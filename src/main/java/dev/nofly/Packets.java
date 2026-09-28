package dev.nofly;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class packets extends PacketListenerAbstract {

    private final no_fly plugin;
    private final move move;

    public packets(no_fly plugin, move move) {
        this.plugin = plugin;
        this.move = move;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) return;

        Object raw = event.getPlayer();
        if (!(raw instanceof Player player)) return;

        WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);

        Vector3d position = null;
        if (packet.hasPositionChanged()) {
            position = packet.getLocation().getPosition();
        }
        float yaw = packet.getLocation().getYaw();
        float pitch = packet.getLocation().getPitch();
        boolean on_ground = packet.isOnGround();

        if (position != null) {
            if (!finite(position.getX()) || !finite(position.getY()) || !finite(position.getZ())) {
                event.setCancelled(true);
                return;
            }
            if (Math.abs(position.getX()) > 3.0e7
                    || Math.abs(position.getZ()) > 3.0e7
                    || Math.abs(position.getY()) > 2.0e7) {
                event.setCancelled(true);
                return;
            }
            if (Math.abs(position.getX() - player.getLocation().getX()) > 64.0
                    || Math.abs(position.getZ() - player.getLocation().getZ()) > 64.0) {
                event.setCancelled(true);
                return;
            }
        }

        final Vector3d final_position = position;
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            move.on_flying(player, final_position, yaw, pitch, on_ground);
        });
    }

    private boolean finite(double value) {
        return Double.isFinite(value);
    }
}