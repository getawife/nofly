package dev.nofly;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerCommand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class Packets implements PacketListener {

    private final NoFly plugin;

    public Packets(NoFly plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_COMMAND) {
            glide(event);
        } else if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            confirm(event);
        }
    }

    private void glide(PacketReceiveEvent event) {
        if (!plugin.config().glide_on) {
            return;
        }
        WrapperPlayClientPlayerCommand packet = new WrapperPlayClientPlayerCommand(event);
        if (packet.getAction() != 8) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        boolean grounded = player.isOnGround();
        boolean no_elytra = player.getInventory().getChestplate() == null
                || player.getInventory().getChestplate().getType() != Material.ELYTRA;
        boolean rising = player.getVelocity().getY() >= 0;
        if (grounded || no_elytra || rising) {
            event.setCancelled(true);
            plugin.getLogger().info(player.getName() + " flagged glide");
        }
    }

    private void confirm(PacketReceiveEvent event) {
        WrapperPlayClientWindowConfirmation packet = new WrapperPlayClientWindowConfirmation(event);
        UUID id = event.getUser().getUUID();
        plugin.clock().confirm(id, packet.getActionId());
    }
}