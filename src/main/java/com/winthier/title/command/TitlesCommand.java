package com.winthier.title.command;

import com.winthier.playercache.PlayerCache;
import com.winthier.title.TitlePlugin;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TitlesCommand implements CommandExecutor {
    public final TitlePlugin plugin;

    public TitlesCommand(TitlePlugin plugin) {
        this.plugin = plugin;
    }

    OfflinePlayer findPlayer(String name) {
        OfflinePlayer result;
        result = plugin.getServer().getPlayerExact(name);
        if (result != null) return result;
        UUID uuid = PlayerCache.uuidForName(name);
        if (uuid != null) return plugin.getServer().getOfflinePlayer(uuid);
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        try {
            if (args.length == 0) {
                return false;
            } else if ("List".equalsIgnoreCase(args[0]) && args.length == 1) {
                StringBuilder sb = new StringBuilder(plugin.format("&eAll titles:"));
                for (Map.Entry<String, String> entry : plugin.database.listTitles().entrySet()) {
                    sb.append("\n").append(plugin.format("&6%s&r: %s (%s&r)", entry.getKey(), entry.getValue(), ChatColor.translateAlternateColorCodes('&', entry.getValue())));
                }
                sender.sendMessage(sb.toString());
            } else if ("List".equalsIgnoreCase(args[0]) && args.length == 2) {
                String playerName = args[1];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                String name = plugin.database.getPlayerTitle(player);
                StringBuilder sb = new StringBuilder().append(ChatColor.YELLOW).append("Titles of ").append(playerName).append(":");
                for (Map.Entry<String, String> entry : plugin.database.listTitles(player).entrySet()) {
                    sb.append("\n").append(ChatColor.YELLOW);
                    if (name != null && name.equalsIgnoreCase(entry.getKey())) {
                        sb.append("+");
                    } else {
                        sb.append("-");
                    }
                    sb.append(plugin.format(" &6%s&r: %s (%s&r)", entry.getKey(), entry.getValue(), ChatColor.translateAlternateColorCodes('&', entry.getValue())));
                }
                sender.sendMessage(sb.toString());
            } else if ("Create".equalsIgnoreCase(args[0]) && args.length >= 3) {
                String name = args[1];
                StringBuilder sb = new StringBuilder(args[2]);
                for (int i = 3; i < args.length; ++i) {
                    sb.append(" ").append(args[i]);
                }
                String title = sb.toString();
                plugin.database.setTitle(name, title);
                plugin.send(sender, "&eTitle %s created: " + title, name);
            } else if (("Desc".equalsIgnoreCase(args[0])|| "Description".equalsIgnoreCase(args[0])) && args.length >= 3) {
                String name = args[1];
                StringBuilder sb = new StringBuilder(args[2]);
                for (int i = 3; i < args.length; ++i) {
                    sb.append(" ").append(args[i]);
                }
                String description = sb.toString();
                if (plugin.database.setDescription(name, description)) {
                    plugin.send(sender, "&eSet description for title %s:&r %s", name, description);
                } else {
                    plugin.send(sender, "&cTitle not found: %s", name);
                }
            } else if ("Unlock".equalsIgnoreCase(args[0]) && args.length == 3) {
                String playerName = args[1];
                String titleName = args[2];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                if (null == plugin.database.getTitle(titleName)) throw new CommandException("Unknown title: " + titleName);
                plugin.database.unlockTitle(player, titleName);

                plugin.send(sender, "&eUnlocked title %s for player %s (%s).", titleName, playerName, player.getUniqueId());
            } else if ("Lock".equalsIgnoreCase(args[0]) && args.length == 3) {
                String playerName = args[1];
                String titleName = args[2];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                if (null == plugin.database.getTitle(titleName)) throw new CommandException("Unknown title: " + titleName);
                if (!plugin.database.lockTitle(player, titleName)) throw new CommandException("Player never had this title.");
                plugin.send(sender, "&eLocked title %s for player %s (%s).", titleName, playerName, player.getUniqueId());
            } else if ("Set".equalsIgnoreCase(args[0]) && args.length == 3) {
                String playerName = args[1];
                String titleName = args[2];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                if (null == plugin.database.getTitle(titleName)) throw new CommandException("Unknown title: " + titleName);
                if (!plugin.database.playerHasTitle(player, titleName)) throw new CommandException("This title is locked.");
                plugin.database.setPlayerTitle(player, titleName);
                plugin.send(sender, "&eSet title %s for player %s (%s).", titleName, playerName, player.getUniqueId());
                if (player.isOnline()) plugin.updatePlayer(player.getPlayer());
            } else if ("Reset".equalsIgnoreCase(args[0]) && args.length == 2) {
                String playerName = args[1];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                plugin.database.setPlayerTitle(player, null);
                plugin.send(sender, "Reset title of player %s (%s).", playerName, player.getUniqueId());
                if (player.isOnline()) plugin.updatePlayer(player.getPlayer());
            } else if ("Update".equalsIgnoreCase(args[0]) && args.length == 1) {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    plugin.updatePlayer(player);
                }
                plugin.send(sender, "&ePlayer titles updated.");
            } else {
                return false;
            }
        } catch (CommandException ce) {
            sender.sendMessage("" + ChatColor.RED + ce.getMessage());
        }
        return true;
    }
}
