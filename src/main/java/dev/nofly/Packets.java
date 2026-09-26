package dev.nofly;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

public final class packets extends PacketListenerAbstract {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            return;
        }

        WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);
        if (!packet.hasPositionChanged()) {
            return;
        }

        Vector3d position = packet.getLocation().getPosition();
        if (position == null || !finite(position.getX()) || !finite(position.getY()) || !finite(position.getZ())) {
            event.setCancelled(true);
            return;
        }

        if (Math.abs(position.getX()) > 3.0e7
                || Math.abs(position.getZ()) > 3.0e7
                || Math.abs(position.getY()) > 2.0e7) {
            event.setCancelled(true);
        }
    }

    private boolean finite(double value) {
        return Double.isFinite(value);
    }
}
