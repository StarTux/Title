package com.winthier.title;

import com.google.gson.JsonParseException;
import com.winthier.playercache.PlayerCache;
import com.winthier.title.sql.TitleInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class TitlesCommand implements TabExecutor {
    public final TitlePlugin plugin;

    public TitlesCommand(final TitlePlugin plugin) {
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

    private void button(ComponentBuilder cb, Title title) {
        cb.append(title.getTitleComponent()).retain(FormatRetention.NONE);
        BaseComponent[] tooltip = TextComponent
            .fromLegacyText(plugin.format("%s\n&7%s\n&r%s",
                                          title.formatted(), title.getName(), title.formattedDescription()));
        cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip));
        cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/title:titles info " + title.getName()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (args.length == 0) {
                return false;
            } else if ("List".equalsIgnoreCase(args[0]) && args.length == 1) {
                ComponentBuilder cb = new ComponentBuilder();
                cb.append(plugin.format("&eAll titles:"));
                for (Title title: plugin.getDb().listTitles()) {
                    cb.append(" ");
                    button(cb, title);
                }
                sender.sendMessage(cb.create());
            } else if ("List".equalsIgnoreCase(args[0]) && args.length == 2) {
                String playerName = args[1];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                String name = plugin.getDb().getPlayerTitleName(player.getUniqueId());
                ComponentBuilder cb = new ComponentBuilder()
                    .append("Titles of " + playerName + ":").color(ChatColor.YELLOW);
                for (Title title: plugin.getDb().listTitles(player.getUniqueId())) {
                    cb.append(" ").reset();
                    if (name != null && name.equalsIgnoreCase(title.getName())) {
                        cb.append("[").color(ChatColor.WHITE);
                        button(cb, title);
                        cb.append("]").color(ChatColor.WHITE);
                    } else {
                        button(cb, title);
                    }
                }
                sender.sendMessage(cb.create());
            } else if ("info".equalsIgnoreCase(args[0])) {
                if (args.length != 2) return false;
                String name = args[1];
                Title title = plugin.getDb().getTitle(name);
                if (title == null) throw new CommandException("Title not found: " + name);
                sender.sendMessage(Msg.builder("Name:").color(ChatColor.GRAY).append(" ").reset()
                                   .append(title.getName()).insertion(title.getName()).create());
                sender.sendMessage(Msg.builder("Title:").color(ChatColor.GRAY).append(" ").reset()
                                   .append(title.formatted()).insertion(title.getTitle()).create());
                if (title.getTitleJson() != null) {
                    sender.sendMessage(Msg.builder("Component:").color(ChatColor.GRAY).append(" ").reset()
                                       .append(title.getTitleComponent()).retain(FormatRetention.NONE)
                                       .insertion(title.getTitleJson()).create());
                }
                if (title.getPlayerListPrefix() != null) {
                    sender.sendMessage(Msg.builder("Player List:").color(ChatColor.GRAY).append(" ").reset()
                                       .append(title.formattedPlayerListPrefix()).insertion(title.getPlayerListPrefix()).create());
                }
                if (title.getDescription() != null) {
                    sender.sendMessage(Msg.builder("Description:").color(ChatColor.GRAY).append(" ").reset()
                                       .append(title.formattedDescription()).insertion(title.getDescription()).create());
                }
                if (title.getShine() != null) {
                    sender.sendMessage(Msg.builder("Shine:").color(ChatColor.GRAY).append(" ").reset()
                                       .append(title.getShine()).insertion(title.getShine()).create());
                }
                sender.sendMessage(Msg.builder("Priority:").color(ChatColor.GRAY).append(" ").reset()
                                   .append("" + title.getPriority()).create());
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
            } else if (("Desc".equalsIgnoreCase(args[0]) || "Description".equalsIgnoreCase(args[0])) && args.length >= 2) {
                String name = args[1];
                if (args.length == 2) {
                    Title title = plugin.getDb().getTitle(name);
                    if (title == null) throw new CommandException("Title not found: " + name);
                    sender.sendMessage("Description of title '" +  title.getName() + "': " + title.getDescription());
                    return true;
                }
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
                if (player == null) {
                    throw new CommandException("Player not found: " + playerName);
                }
                playerName = player.getName();
                Title title = plugin.getDb().getTitle(titleName);
                if (title == null) {
                    throw new CommandException("Unknown title: " + titleName);
                }
                if (plugin.getDb().unlockTitle(player.getUniqueId(), title)) {
                    sender.sendMessage(Msg.builder("Unlocked title for " + playerName + ": ").color(ChatColor.YELLOW)
                                       .append(title.getTitleComponent()).retain(FormatRetention.NONE).create());
                } else {
                    sender.sendMessage(Msg.builder(playerName + " already had title: ").color(ChatColor.RED)
                                       .append(title.getTitleComponent()).retain(FormatRetention.NONE).create());
                }
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
                if (player == null) {
                    throw new CommandException("Player not found: " + playerName);
                }
                Title title = plugin.getDb().getTitle(titleName);
                if (title == null) {
                    throw new CommandException("Unknown title: " + titleName);
                }
                if (!plugin.getDb().playerHasTitle(player.getUniqueId(), title)) {
                    throw new CommandException("This title is locked.");
                }
                plugin.getDb().setPlayerTitle(player.getUniqueId(), title);
                if (player instanceof Player) {
                    plugin.updatePlayerListName((Player) player);
                }
                sender.sendMessage(Msg.builder("Set title ").color(ChatColor.YELLOW)
                                   .append(title.getTitleComponent()).retain(FormatRetention.NONE).append("").reset()
                                   .append(" for player ").color(ChatColor.YELLOW).append(playerName)
                                   .create());
            } else if ("Has".equalsIgnoreCase(args[0]) && args.length == 3) {
                String playerName = args[1];
                String titleName = args[2];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                Title title = plugin.getDb().getTitle(titleName);
                if (title == null) throw new CommandException("Unknown title: " + titleName);
                if (plugin.getDb().playerHasTitle(player.getUniqueId(), title)) {
                    sender.sendMessage(Msg.builder(player.getName() + " has title: ")
                                       .append(title.getTitleComponent()).retain(FormatRetention.NONE).create());
                } else {
                    sender.sendMessage(Msg.builder(player.getName() + " does not have title: ")
                                       .append(title.getTitleComponent()).retain(FormatRetention.NONE).create());
                }
            } else if ("UnlockSet".equalsIgnoreCase(args[0])) {
                if (args.length < 3) return false;
                String playerName = args[1];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) {
                    throw new CommandException("Player not found: " + playerName);
                }
                for (int i = 2; i < args.length; i += 1) {
                    String titleName = args[i];
                    if (null == plugin.getDb().getTitle(titleName)) throw new CommandException("Unknown title: " + titleName);
                }
                for (int i = 2; i < args.length; i += 1) {
                    String titleName = args[i];
                    Title title = plugin.getDb().getTitle(titleName);
                    if (title == null) {
                        throw new CommandException("Title not found: " + titleName);
                    }
                    boolean res = plugin.getDb().unlockTitle(player.getUniqueId(), title);
                    if (res) {
                        plugin.getDb().setPlayerTitle(player.getUniqueId(), title);
                        if (player instanceof Player) plugin.updatePlayerListName((Player) player);
                        plugin.send(sender, "&aUnlocked and set title %s for player %s.", titleName, playerName);
                        return true;
                    }
                }
                String titleName = args[args.length - 1];
                plugin.send(sender, "&e%s already has title %s.", playerName, titleName);
            } else if ("Search".equalsIgnoreCase(args[0]) && args.length >= 2) {
                StringBuilder sb = new StringBuilder();
                sb.append(args[1]);
                for (int i = 2; i < args.length; i += 1) {
                    sb.append(args[i]);
                }
                String term = sb.toString().toLowerCase();
                List<Title> matches = new ArrayList<>();
                for (Title title : plugin.getDb().listTitles()) {
                    if (title.getName().toLowerCase().contains(term)
                        || title.stripped().toLowerCase().contains(term)
                        || title.strippedDescription().toLowerCase().contains(term)) {
                        matches.add(title);
                    }
                }
                if (matches.isEmpty()) throw new CommandException("No match: " + term);
                ComponentBuilder cb = new ComponentBuilder("" + ChatColor.YELLOW + matches.size() + " titles matching: ");
                for (Title title : matches) {
                    cb.append(" ").reset();
                    button(cb, title);
                }
                sender.sendMessage(cb.create());
            } else if ("Reset".equalsIgnoreCase(args[0]) && args.length == 2) {
                String playerName = args[1];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                plugin.getDb().setPlayerTitle(player.getUniqueId(), null);
                if (player instanceof Player) plugin.updatePlayerListName((Player) player);
                plugin.send(sender, "Reset title of player %s.", playerName);
            } else if ("Reload".equalsIgnoreCase(args[0]) && args.length == 1) {
                plugin.getDb().init();
                plugin.send(sender, "Database reloaded.");
            } else if ("json".equalsIgnoreCase(args[0])) {
                if (args.length < 2) return false;
                String name = args[1];
                TitleInfo title = plugin.getDb().getTitle(name);
                if (title == null) throw new CommandException("Title not found: " + name);
                String json;
                TextComponent text;
                if (args.length == 2) {
                    json = null;
                    text = null;
                } else {
                    json = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    try {
                        text = new TextComponent(new ComponentSerializer().parse(json));
                    } catch (JsonParseException jpe) {
                        throw new CommandException("Bad Json format: " + json);
                    }
                }
                title.setTitleJson(json);
                if (!plugin.getDb().save(title)) {
                    throw new CommandException("Could not save title: " + title.getName());
                } else {
                    if (json == null) {
                        sender.sendMessage(Msg.builder("Json of title " + title.getName() + " reset").create());
                    } else {
                        sender.sendMessage(Msg.builder("Json of title " + title.getName() + " set: ").append(text).create());
                    }
                }
            } else if ("prefix".equalsIgnoreCase(args[0])) {
                if (args.length < 2) return false;
                String name = args[1];
                TitleInfo title = plugin.getDb().getTitle(name);
                if (title == null) throw new CommandException("Title not found: " + name);
                String prefix;
                String text;
                if (args.length == 2) {
                    prefix = null;
                    text = null;
                } else {
                    prefix = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    text = Msg.colorize(prefix);
                }
                title.setPlayerListPrefix(prefix);
                if (!plugin.getDb().save(title)) {
                    throw new CommandException("Could not save title: " + title.getName());
                } else {
                    if (prefix == null) {
                        sender.sendMessage(Msg.builder("Prefix of title " + title.getName() + " reset").create());
                    } else {
                        sender.sendMessage(Msg.builder("Prefix of title " + title.getName() + " set: ").append(text).create());
                    }
                }
            } else if ("shine".equalsIgnoreCase(args[0])) {
                if (args.length != 2 && args.length != 3) return false;
                String name = args[1];
                TitleInfo title = plugin.getDb().getTitle(name);
                if (title == null) throw new CommandException("Title not found: " + name);
                Shine shine;
                BaseComponent[] text;
                if (args.length == 2) {
                    shine = null;
                    text = null;
                } else {
                    try {
                        shine = Shine.valueOf(args[2].toUpperCase());
                    } catch (IllegalArgumentException iae) {
                        throw new CommandException("Shine not found: " + args[2]);
                    }
                    text = Msg.builder(shine.name()).color(shine.color).create();
                }
                title.setShine(shine.name().toLowerCase());
                if (!plugin.getDb().save(title)) {
                    throw new CommandException("Could not save title: " + title.getName());
                } else {
                    if (shine == null) {
                        sender.sendMessage(Msg.builder("Shine of title " + title.getName() + " reset").create());
                    } else {
                        sender.sendMessage(Msg.builder("Shine of title " + title.getName() + " set: ").append(text).create());
                    }
                }
            } else if ("prio".equalsIgnoreCase(args[0])) {
                if (args.length != 3) return false;
                String name = args[1];
                TitleInfo title = plugin.getDb().getTitle(name);
                if (title == null) throw new CommandException("Title not found: " + name);
                int priority;
                try {
                    priority = Integer.parseInt(args[2]);
                } catch (IllegalArgumentException iae) {
                    throw new CommandException("Shine not found: " + args[2]);
                }
                title.setPriority(priority);
                if (!plugin.getDb().save(title)) {
                    throw new CommandException("Could not save title: " + title.getName());
                } else {
                    sender.sendMessage(Msg.builder("Priority of title " + title.getName() + " set to " + priority).create());
                }
            } else {
                return false;
            }
        } catch (CommandException ce) {
            sender.sendMessage("" + ChatColor.RED + ce.getMessage());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return null;
        String cmd = args[0].toLowerCase();
        if (args.length == 1) {
            return Stream.of("list", "info", "listplayers", "ranktitles", "create", "desc",
                             "unlock", "lock", "set", "has", "unlockset", "reset", "reload",
                             "search",
                             "json", "prefix", "shine", "prio")
                .filter(s -> s.contains(cmd))
                .collect(Collectors.toList());
        }
        String arg = args[args.length - 1];
        switch (cmd) {
        case "list":
        case "reset":
            if (args.length == 2) return null; // player
            return Collections.emptyList();
        case "info":
        case "listplayers":
        case "desc":
        case "json":
        case "prefix":
        case "prio":
            if (args.length == 2) return completeTitles(arg);
            return Collections.emptyList();
        case "shine":
            if (args.length == 2) return completeTitles(arg);
            if (args.length == 3) return Stream.of(Shine.values())
                                      .map(Shine::name)
                                      .map(String::toLowerCase)
                                      .filter(s -> s.startsWith(arg))
                                      .collect(Collectors.toList());
            return Collections.emptyList();
        case "has":
        case "lock":
        case "unlock":
        case "set":
            if (args.length == 2) return null; // player
            if (args.length == 3) return completeTitles(arg);
            return Collections.emptyList();
        case "unlockset":
            if (args.length == 2) return null; // player
            return completeTitles(arg);
        case "create":
        case "reload":
        case "ranktitles":
        case "search":
        default:
            return Collections.emptyList();
        }
    }

    List<String> completeTitles(String arg) {
        return plugin.getDb().listTitles().stream()
            .filter(t -> t.getName().contains(arg))
            .map(Title::getName)
            .collect(Collectors.toList());
    }
}
