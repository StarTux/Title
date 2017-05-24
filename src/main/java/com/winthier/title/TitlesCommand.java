package com.winthier.title;

import com.winthier.playercache.PlayerCache;
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
                for (Title title: plugin.getDb().listTitles()) {
                    sb.append("\n").append(plugin.format("&6%s&r: %s (%s&r)", title.getName(), title.getTitle(), title.formatted()));
                }
                sender.sendMessage(sb.toString());
            } else if ("List".equalsIgnoreCase(args[0]) && args.length == 2) {
                String playerName = args[1];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                String name = plugin.getDb().getPlayerTitle(player.getUniqueId());
                StringBuilder sb = new StringBuilder().append(ChatColor.YELLOW).append("Titles of ").append(playerName).append(":");
                for (Title title: plugin.getDb().listTitles(player.getUniqueId())) {
                    sb.append("\n").append(ChatColor.YELLOW);
                    if (name != null && name.equalsIgnoreCase(title.getName())) {
                        sb.append("+");
                    } else {
                        sb.append("-");
                    }
                    sb.append(plugin.format(" &6%s&r: %s (%s&r)", title.getName(), title.getTitle(), title.formatted()));
                }
                sender.sendMessage(sb.toString());
            } else if ("Create".equalsIgnoreCase(args[0]) && args.length >= 3) {
                String name = args[1];
                StringBuilder sb = new StringBuilder(args[2]);
                for (int i = 3; i < args.length; ++i) {
                    sb.append(" ").append(args[i]);
                }
                String title = sb.toString();
                plugin.getDb().setTitle(name, title);
                plugin.send(sender, "&eTitle %s created: " + title, name);
            } else if (("Desc".equalsIgnoreCase(args[0])|| "Description".equalsIgnoreCase(args[0])) && args.length >= 3) {
                String name = args[1];
                StringBuilder sb = new StringBuilder(args[2]);
                for (int i = 3; i < args.length; ++i) {
                    sb.append(" ").append(args[i]);
                }
                String description = sb.toString();
                if (plugin.getDb().setDescription(name, description)) {
                    plugin.send(sender, "&eSet description for title %s:&r %s", name, description);
                } else {
                    plugin.send(sender, "&cTitle not found: %s", name);
                }
            } else if ("Unlock".equalsIgnoreCase(args[0]) && args.length == 3) {
                String playerName = args[1];
                String titleName = args[2];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                if (null == plugin.getDb().getTitle(titleName)) throw new CommandException("Unknown title: " + titleName);
                plugin.getDb().unlockTitle(player.getUniqueId(), titleName);

                plugin.send(sender, "&eUnlocked title %s for player %s.", titleName, playerName);
            } else if ("Lock".equalsIgnoreCase(args[0]) && args.length == 3) {
                String playerName = args[1];
                String titleName = args[2];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                if (null == plugin.getDb().getTitle(titleName)) throw new CommandException("Unknown title: " + titleName);
                if (!plugin.getDb().lockTitle(player.getUniqueId(), titleName)) throw new CommandException("Player never had this title.");
                plugin.send(sender, "&eLocked title %s for player %s.", titleName, playerName);
            } else if ("Set".equalsIgnoreCase(args[0]) && args.length == 3) {
                String playerName = args[1];
                String titleName = args[2];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                if (null == plugin.getDb().getTitle(titleName)) throw new CommandException("Unknown title: " + titleName);
                if (!plugin.getDb().playerHasTitle(player.getUniqueId(), titleName)) throw new CommandException("This title is locked.");
                plugin.getDb().setPlayerTitle(player.getUniqueId(), titleName);
                plugin.send(sender, "&eSet title %s for player %s.", titleName, playerName);
            } else if ("UnlockSet".equalsIgnoreCase(args[0]) && args.length == 3) {
                String playerName = args[1];
                String titleName = args[2];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                if (null == plugin.getDb().getTitle(titleName)) throw new CommandException("Unknown title: " + titleName);
                plugin.getDb().unlockTitle(player.getUniqueId(), titleName);
                plugin.getDb().setPlayerTitle(player.getUniqueId(), titleName);
                plugin.send(sender, "&eUnlocked and set title %s for player %s.", titleName, playerName);
            } else if ("Reset".equalsIgnoreCase(args[0]) && args.length == 2) {
                String playerName = args[1];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                plugin.getDb().setPlayerTitle(player.getUniqueId(), null);
                plugin.send(sender, "Reset title of player %s.", playerName);
            } else {
                return false;
            }
        } catch (CommandException ce) {
            sender.sendMessage("" + ChatColor.RED + ce.getMessage());
        }
        return true;
    }
}
