package com.winthier.title;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.winthier.playercache.PlayerCache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class TitlesCommand implements CommandExecutor {
    public final TitlePlugin plugin;

    public TitlesCommand(TitlePlugin plugin) {
        this.plugin = plugin;
    }

    UUID findPlayer(String name) {
        Player player = plugin.getServer().getPlayerExact(name);
        if (player != null) return player.getUniqueId();
        return PlayerCache.uuidForName(name);
    }

    static class CommandException extends Exception {
        CommandException(String msg) {
            super(msg);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            return onCommand(sender, args);
        } catch (CommandException ce) {
            sender.sendMessage("" + TextFormat.RED + ce.getMessage());
            return true;
        }
    }

    private boolean onCommand(CommandSender sender, String[] args) throws CommandException {
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
            UUID uuid = findPlayer(playerName);
            if (uuid == null) throw new CommandException("Player not found: " + playerName);
            String name = plugin.getDb().getPlayerTitle(uuid);
            StringBuilder sb = new StringBuilder().append(TextFormat.YELLOW).append("Titles of ").append(playerName).append(":");
            for (Title title: plugin.getDb().listTitles(uuid)) {
                sb.append("\n").append(TextFormat.YELLOW);
                if (name != null && name.equalsIgnoreCase(title.getName())) {
                    sb.append("+");
                } else {
                    sb.append("-");
                }
                sb.append(plugin.format(" &6%s&r: %s (%s&r)", title.getName(), title.getTitle(), title.formatted()));
            }
            sender.sendMessage(sb.toString());
        } else if ("ListPlayers".equalsIgnoreCase(args[0]) && args.length == 2) {
            String titleName = args[1];
            Title title = plugin.getDb().getTitle(titleName);
            List<UUID> players = plugin.getDb().listPlayers(title);
            StringBuilder sb = new StringBuilder("Owners of title \"").append(title.getName()).append("(").append(players.size()).append(")");
            for (UUID uuid: players) {
                String name = PlayerCache.nameForUuid(uuid);
                if (name == null) name = uuid.toString();
                sb.append(" ").append(name);
            }
            sender.sendMessage(sb.toString());
        } else if ("RankTitles".equalsIgnoreCase(args[0]) && args.length == 1) {
            class Rank {
                Title title = null;
                int count = 0;
            }
            List<Rank> ranks = new ArrayList<>();
            for (Title title: plugin.getDb().listTitles()) {
                Rank rank = new Rank();
                rank.title = title;
                rank.count = plugin.getDb().listPlayers(title).size();
                ranks.add(rank);
            }
            Collections.sort(ranks, (b, a) -> Integer.compare(a.count, b.count));
            int rankIter = 1;
            sender.sendMessage("Ranking of titles by ownership (" + ranks.size() + ")");
            for (Rank rank: ranks) {
                sender.sendMessage("" + rankIter++ + ") " + rank.count + " " + rank.title.getName());
            }
        } else if ("Create".equalsIgnoreCase(args[0]) && args.length >= 3) {
            String name = args[1];
            StringBuilder sb = new StringBuilder(args[2]);
            for (int i = 3; i < args.length; ++i) {
                sb.append(" ").append(args[i]);
            }
            String title = sb.toString();
            plugin.getDb().setTitle(name, title);
            plugin.send(sender, "&eTitle %s created: " + title, name);
        } else if (("Desc".equalsIgnoreCase(args[0]) || "Description".equalsIgnoreCase(args[0])) && args.length >= 3) {
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
            UUID uuid = findPlayer(playerName);
            if (uuid == null) throw new CommandException("Player not found: " + playerName);
            if (null == plugin.getDb().getTitle(titleName)) throw new CommandException("Unknown title: " + titleName);
            plugin.getDb().unlockTitle(uuid, titleName);
            plugin.send(sender, "&eUnlocked title %s for player %s.", titleName, playerName);
        } else if ("Lock".equalsIgnoreCase(args[0]) && args.length == 3) {
            String playerName = args[1];
            String titleName = args[2];
            UUID uuid = findPlayer(playerName);
            if (uuid == null) throw new CommandException("Player not found: " + playerName);
            if (null == plugin.getDb().getTitle(titleName)) throw new CommandException("Unknown title: " + titleName);
            if (!plugin.getDb().lockTitle(uuid, titleName)) throw new CommandException("Player never had this title.");
            plugin.send(sender, "&eLocked title %s for player %s.", titleName, playerName);
        } else if ("Set".equalsIgnoreCase(args[0]) && args.length == 3) {
            String playerName = args[1];
            String titleName = args[2];
            UUID uuid = findPlayer(playerName);
            if (uuid == null) throw new CommandException("Player not found: " + playerName);
            if (null == plugin.getDb().getTitle(titleName)) throw new CommandException("Unknown title: " + titleName);
            if (!plugin.getDb().playerHasTitle(uuid, titleName)) throw new CommandException("This title is locked.");
            plugin.getDb().setPlayerTitle(uuid, titleName);
            plugin.send(sender, "&eSet title %s for player %s.", titleName, playerName);
        } else if ("UnlockSet".equalsIgnoreCase(args[0]) && args.length == 3) {
            String playerName = args[1];
            String titleName = args[2];
            UUID uuid = findPlayer(playerName);
            if (uuid == null) throw new CommandException("Player not found: " + playerName);
            if (null == plugin.getDb().getTitle(titleName)) throw new CommandException("Unknown title: " + titleName);
            plugin.getDb().unlockTitle(uuid, titleName);
            plugin.getDb().setPlayerTitle(uuid, titleName);
            plugin.send(sender, "&eUnlocked and set title %s for player %s.", titleName, playerName);
        } else if ("Reset".equalsIgnoreCase(args[0]) && args.length == 2) {
            String playerName = args[1];
            UUID uuid = findPlayer(playerName);
            if (uuid == null) throw new CommandException("Player not found: " + playerName);
            plugin.getDb().setPlayerTitle(uuid, null);
            plugin.send(sender, "Reset title of player %s.", playerName);
        } else if ("Reload".equalsIgnoreCase(args[0]) && args.length == 1) {
            plugin.getDb().init();
            plugin.send(sender, "Database reloaded.");
        }
        return false;
    }
}
