package com.winthier.title;

import com.google.gson.JsonParseException;
import com.winthier.playercache.PlayerCache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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

    private Component button(Title title) {
        Component titleComponent = title.getTitleComponent();
        Component tooltip = TextComponent
            .ofChildren(titleComponent,
                        Component.text('\n' + title.getName(), NamedTextColor.GRAY),
                        Component.text('\n' + title.formattedDescription()));
        return Component.text()
            .append(titleComponent)
            .hoverEvent(HoverEvent.showText(tooltip))
            .clickEvent(ClickEvent.runCommand("/title:titles info " + title.getName()))
            .build();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (args.length == 0) {
                return false;
            } else if ("List".equalsIgnoreCase(args[0]) && args.length == 1) {
                TextComponent.Builder cb = Component.text();
                cb.append(Component.text("All titles:", NamedTextColor.YELLOW));
                for (Title title: plugin.getDb().listTitles()) {
                    cb.append(Component.text(" "));
                    cb.append(button(title));
                }
                sender.sendMessage(cb.build());
            } else if ("List".equalsIgnoreCase(args[0]) && args.length == 2) {
                String playerName = args[1];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                String name = plugin.getDb().getPlayerTitleName(player.getUniqueId());
                TextComponent.Builder cb = Component.text();
                cb.append(Component.text("Titles of " + playerName + ":", NamedTextColor.YELLOW));
                for (Title title: plugin.getDb().listTitles(player.getUniqueId())) {
                    cb.append(Component.text(" "));
                    if (name != null && name.equalsIgnoreCase(title.getName())) {
                        cb.append(Component.text("[", NamedTextColor.WHITE));
                        cb.append(button(title));
                        cb.append(Component.text("]", NamedTextColor.WHITE));
                    } else {
                        cb.append(button(title));
                    }
                }
                sender.sendMessage(cb.build());
            } else if ("info".equalsIgnoreCase(args[0])) {
                if (args.length != 2) return false;
                String name = args[1];
                Title title = plugin.getDb().getTitle(name);
                if (title == null) throw new CommandException("Title not found: " + name);
                sender.sendMessage(Component.text()
                                   .append(Component.text("Name: ", NamedTextColor.GRAY))
                                   .append(Component.text(title.getName(), NamedTextColor.WHITE))
                                   .insertion(title.getName()).build());
                sender.sendMessage(Component.text()
                                   .append(Component.text("Title: ", NamedTextColor.GRAY))
                                   .append(Component.text(title.formatted()))
                                   .insertion(title.getTitle()).build());
                if (title.getTitleJson() != null) {
                    sender.sendMessage(Component.text()
                                       .append(Component.text("Component: ", NamedTextColor.GRAY))
                                       .append(title.getTitleComponent())
                                       .insertion(title.getTitleJson()).build());
                }
                if (title.getDescription() != null) {
                    sender.sendMessage(Component.text()
                                       .append(Component.text("Description: ", NamedTextColor.GRAY))
                                       .append(Component.text(title.formattedDescription(), NamedTextColor.WHITE))
                                       .insertion(title.getDescription()).build());
                }
                if (title.getShine() != null) {
                    sender.sendMessage(Component.text()
                                       .append(Component.text("Shine:", NamedTextColor.GRAY))
                                       .append(Component.text(title.getShine(), NamedTextColor.WHITE))
                                       .insertion(title.getShine()).build());
                }
                sender.sendMessage(Component.text()
                                   .append(Component.text("Priority: ", NamedTextColor.GRAY))
                                   .append(Component.text(title.getPriority(), NamedTextColor.WHITE))
                                   .insertion("" + title.getPriority()).build());
            } else if ("ListPlayers".equalsIgnoreCase(args[0]) && args.length == 2) {
                String titleName = args[1];
                Title title = plugin.getDb().getTitle(titleName);
                List<UUID> players = plugin.getDb().listPlayers(title);
                StringBuilder sb = new StringBuilder("Owners of title ").append(title.getName()).append("(").append(players.size()).append(")");
                for (UUID uuid: players) {
                    String name = PlayerCache.nameForUuid(uuid);
                    if (name == null) name = uuid.toString();
                    sb.append(" ").append(name);
                }
                sender.sendMessage(Component.text(sb.toString(), NamedTextColor.YELLOW));
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
                sender.sendMessage(Component.text("Ranking of titles by ownership (" + ranks.size() + ")", NamedTextColor.YELLOW));
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
                    sender.sendMessage(Component.text().content("Unlocked title for " + playerName + ": ").color(NamedTextColor.YELLOW)
                                       .append(title.getTitleComponent()).build());
                } else {
                    sender.sendMessage(Component.text().content(playerName + " already had title: ").color(NamedTextColor.RED)
                                       .append(title.getTitleComponent()).build());
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
                    plugin.updatePlayerName((Player) player);
                }
                sender.sendMessage(TextComponent.ofChildren(Component.text("Set title ", NamedTextColor.YELLOW),
                                                            title.getTitleComponent(),
                                                            Component.text(" for player ", NamedTextColor.YELLOW),
                                                            Component.text(playerName)));
            } else if ("Has".equalsIgnoreCase(args[0]) && args.length == 3) {
                String playerName = args[1];
                String titleName = args[2];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                Title title = plugin.getDb().getTitle(titleName);
                if (title == null) throw new CommandException("Unknown title: " + titleName);
                if (plugin.getDb().playerHasTitle(player.getUniqueId(), title)) {
                    sender.sendMessage(Component.text().content(player.getName() + " has title: ")
                                       .append(title.getTitleComponent()).build());
                } else {
                    sender.sendMessage(Component.text().content(player.getName() + " does not have title: ")
                                       .append(title.getTitleComponent()).build());
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
                        if (player instanceof Player) plugin.updatePlayerName((Player) player);
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
                TextComponent.Builder cb = Component.text();
                cb.append(Component.text("" + matches.size() + " titles matching: ", NamedTextColor.YELLOW));
                for (Title title : matches) {
                    cb.append(Component.text(" "));
                    cb.append(button(title));
                }
                sender.sendMessage(cb.build());
            } else if ("Reset".equalsIgnoreCase(args[0]) && args.length == 2) {
                String playerName = args[1];
                OfflinePlayer player = findPlayer(playerName);
                if (player == null) throw new CommandException("Player not found: " + playerName);
                plugin.getDb().setPlayerTitle(player.getUniqueId(), null);
                if (player instanceof Player) plugin.updatePlayerName((Player) player);
                plugin.send(sender, "Reset title of player %s.", playerName);
            } else if ("Reload".equalsIgnoreCase(args[0]) && args.length == 1) {
                plugin.getDb().init();
                plugin.send(sender, "Database reloaded.");
            } else if ("json".equalsIgnoreCase(args[0])) {
                if (args.length < 2) return false;
                String name = args[1];
                Title title = plugin.getDb().getTitle(name);
                if (title == null) throw new CommandException("Title not found: " + name);
                String json;
                Component text;
                if (args.length == 2) {
                    json = null;
                    text = null;
                } else {
                    json = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    try {
                        text = Msg.parseComponent(json);
                    } catch (JsonParseException jpe) {
                        throw new CommandException("Bad Json format: " + json);
                    }
                }
                title.setTitleJson(json);
                if (!plugin.getDb().save(title)) {
                    throw new CommandException("Could not save title: " + title.getName());
                } else {
                    if (json == null) {
                        sender.sendMessage(Component.text().content("Json of title " + title.getName() + " reset").build());
                    } else {
                        sender.sendMessage(Component.text().content("Json of title " + title.getName() + " set: ").append(text).build());
                    }
                }
            } else if ("color".equalsIgnoreCase(args[0])) {
                if (args.length < 2) return false;
                String name = args[1];
                Title title = plugin.getDb().getTitle(name);
                if (title == null) throw new CommandException("Title not found: " + name);
                String value = args.length >= 1 ? args[1] : null;
                title.setNameColor(value);
                if (!plugin.getDb().save(title)) {
                    throw new CommandException("Could not save title: " + title.getName());
                } else {
                    if (value == null) {
                        sender.sendMessage(Component.text("Color of title " + title.getName() + " reset"));
                    } else {
                        sender.sendMessage(Component.text("Color of title " + title.getName() + " set: " + value));
                    }
                }
            } else if ("prefix".equalsIgnoreCase(args[0])) {
                if (args.length < 2) return false;
                String name = args[1];
                Title title = plugin.getDb().getTitle(name);
                if (title == null) throw new CommandException("Title not found: " + name);
                boolean value = args.length >= 1 ? true : false;
                title.setPrefix(value);
                if (!plugin.getDb().save(title)) {
                    throw new CommandException("Could not save title: " + title.getName());
                } else {
                    sender.sendMessage(Component.text("Prefix of title " + title.getName() + " set: " + value));
                }
            } else if ("shine".equalsIgnoreCase(args[0])) {
                if (args.length != 2 && args.length != 3) return false;
                String name = args[1];
                Title title = plugin.getDb().getTitle(name);
                if (title == null) throw new CommandException("Title not found: " + name);
                Shine shine;
                Component text;
                if (args.length == 2) {
                    shine = null;
                    text = null;
                } else {
                    try {
                        shine = Shine.valueOf(args[2].toUpperCase());
                    } catch (IllegalArgumentException iae) {
                        throw new CommandException("Shine not found: " + args[2]);
                    }
                    text = Component.text(shine.name(), shine.color);
                }
                title.setShine(shine.name().toLowerCase());
                if (!plugin.getDb().save(title)) {
                    throw new CommandException("Could not save title: " + title.getName());
                } else {
                    if (shine == null) {
                        sender.sendMessage(Component.text().content("Shine of title " + title.getName() + " reset").build());
                    } else {
                        sender.sendMessage(Component.text().content("Shine of title " + title.getName() + " set: ").append(text).build());
                    }
                }
            } else if ("prio".equalsIgnoreCase(args[0])) {
                if (args.length != 3) return false;
                String name = args[1];
                Title title = plugin.getDb().getTitle(name);
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
                    sender.sendMessage(Component.text().content("Priority of title " + title.getName() + " set to " + priority).build());
                }
            } else {
                return false;
            }
        } catch (CommandException ce) {
            sender.sendMessage(Component.text(ce.getMessage(), NamedTextColor.RED));
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
                             "json", "color", "prefix", "shine", "prio")
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
        case "color":
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
