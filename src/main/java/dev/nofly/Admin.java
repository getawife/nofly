package dev.nofly;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public final class admin implements CommandExecutor, TabCompleter {

    private final no_fly plugin;

    public admin(no_fly plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("nofly.admin")) {
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("/nofly reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.config().load();
            plugin.log().reload();
            sender.sendMessage("nofly reloaded");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        return List.of("reload");
    }
}
