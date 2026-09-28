package dev.nofly;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public final class admin implements CommandExecutor, TabCompleter {

    private final no_fly plugin;

    public admin(no_fly plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nofly.admin")) return true;

        if (args.length == 0) {
            sender.sendMessage("/nofly reload");
            sender.sendMessage("/nofly info <player>");
            sender.sendMessage("/nofly reset <player>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.config().load();
            plugin.log().reload();
            sender.sendMessage("nofly reloaded");
            return true;
        }

        if (args[0].equalsIgnoreCase("info") && args.length >= 2) {
            Player target = plugin.getServer().getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("player not found");
                return true;
            }
            track track = plugin.state().get(target.getUniqueId());
            sender.sendMessage(target.getName()
                    + " vl=" + String.format("%.2f", track.vl)
                    + " flags=" + track.total_flags
                    + " ping=" + target.getPing() + "ms");
            sender.sendMessage("air=" + track.air_ticks
                    + " ground=" + track.ground_ticks
                    + " still=" + track.still_ticks
                    + " ascent=" + track.ascent_ticks
                    + " stalled=" + track.stalled_ticks);
            sender.sendMessage("prediction_vl=" + track.prediction_vl
                    + " vertical_vl=" + track.vertical_vl
                    + " horizontal_vl=" + track.horizontal_vl
                    + " ground_spoof_vl=" + track.ground_spoof_vl);
            return true;
        }

        if (args[0].equalsIgnoreCase("reset") && args.length >= 2) {
            Player target = plugin.getServer().getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage("player not found");
                return true;
            }
            track track = plugin.state().get(target.getUniqueId());
            track.vl = 0.0;
            track.total_flags = 0;
            track.prediction_vl = 0;
            track.vertical_vl = 0;
            track.horizontal_vl = 0;
            track.ground_spoof_vl = 0;
            track.vertical_buffer.reset();
            track.horizontal_buffer.reset();
            track.prediction_buffer.reset();
            sender.sendMessage("reset " + target.getName());
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("nofly.admin")) return List.of();
        if (args.length == 1) return List.of("reload", "info", "reset");
        if (args.length == 2 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("reset"))) {
            return null;
        }
        return List.of();
    }
}