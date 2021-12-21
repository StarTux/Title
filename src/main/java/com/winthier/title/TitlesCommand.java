package com.winthier.title;

import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandContext;
import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.font.Emoji;
import com.winthier.playercache.PlayerCache;
import com.winthier.title.html.HtmlExporter;
import com.winthier.title.sql.Database;
import com.winthier.title.sql.SQLSuffix;
import com.winthier.title.sql.UnlockedInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public final class TitlesCommand implements TabExecutor {
    private static final CommandArgCompleter SHINE_COMPLETER = CommandArgCompleter
        .list(Stream.of(Shine.values()).map(s -> s.key).collect(Collectors.toList()));
    public final TitlePlugin plugin;
    private CommandNode rootNode;

    public TitlesCommand enable() {
        rootNode = new CommandNode("titles");
        rootNode.addChild("list").denyTabCompletion()
            .description("List all titles")
            .senderCaller(this::list);
        rootNode.addChild("player").arguments("<player>")
            .completers(PlayerCache.NAME_COMPLETER)
            .description("List player titles")
            .senderCaller(this::player);
        rootNode.addChild("info").arguments("<title>")
            .completers(this::completeTitleArg)
            .description("Print title info")
            .senderCaller(this::info);
        rootNode.addChild("listplayers").arguments("<title>")
            .completers(this::completeTitleArg)
            .description("List players who own a title")
            .senderCaller(this::listPlayers);
        rootNode.addChild("ranktitles").denyTabCompletion()
            .description("Rank titles by ownership")
            .senderCaller(this::rankTitles);
        rootNode.addChild("create").arguments("<title> <format>")
            .completers(this::completeTitleArg)
            .description("Create or change a title")
            .senderCaller(this::create);
        rootNode.addChild("delete").arguments("<title>")
            .completers(this::completeTitleArg)
            .description("Delete title")
            .senderCaller(this::delete);
        rootNode.addChild("desc").arguments("<title> <desc>")
            .alias("description")
            .completers(this::completeTitleArg)
            .description("Get or set title description")
            .senderCaller(this::desc);
        rootNode.addChild("unlock").arguments("<player> <title>")
            .completers(PlayerCache.NAME_COMPLETER, this::completeTitleArg)
            .description("Unlock title for player")
            .senderCaller(this::unlock);
        rootNode.addChild("lock").arguments("<player> <title>")
            .completers(PlayerCache.NAME_COMPLETER, this::completeTitleArg)
            .description("Lock title for player")
            .senderCaller(this::lock);
        rootNode.addChild("set").arguments("<player> <title>")
            .completers(PlayerCache.NAME_COMPLETER, this::completeTitleArg)
            .description("Set player title")
            .senderCaller(this::set);
        rootNode.addChild("reset").arguments("<player>")
            .completers(PlayerCache.NAME_COMPLETER, this::completeTitleArg)
            .description("Reset player title")
            .senderCaller(this::reset);
        rootNode.addChild("has").arguments("<player> <title>")
            .completers(PlayerCache.NAME_COMPLETER, this::completeTitleArg)
            .description("Check if player has title")
            .senderCaller(this::has);
        rootNode.addChild("unlockset").arguments("<player> <title...>")
            .completers(PlayerCache.NAME_COMPLETER, this::completeTitleArg, CommandArgCompleter.REPEAT)
            .description("Unlock progressive titles for player")
            .senderCaller(this::unlockSet);
        rootNode.addChild("search").arguments("<pattern...>")
            .completers(CommandArgCompleter.EMPTY)
            .description("Search for matching titles")
            .senderCaller(this::search);
        rootNode.addChild("reload").denyTabCompletion()
            .description("Reload all data")
            .senderCaller(this::reload);
        rootNode.addChild("json").arguments("<title> [json]")
            .description("Set title json")
            .completers(this::completeTitleArg)
            .senderCaller(this::json);
        rootNode.addChild("namecolor").arguments("<title> [namecolor]")
            .description("Set title name color")
            .completers(this::completeTitleArg)
            .senderCaller(this::nameColor);
        rootNode.addChild("prefix").arguments("<title> true|false")
            .description("Set prefix value")
            .completers(this::completeTitleArg, CommandArgCompleter.list(Arrays.asList("true", "false")))
            .senderCaller(this::prefix);
        rootNode.addChild("shine").arguments("<title> [shine]")
            .description("Set shine")
            .completers(this::completeTitleArg, SHINE_COMPLETER)
            .senderCaller(this::shine);
        rootNode.addChild("priority").arguments("<title> <prio>")
            .alias("prio")
            .description("Set priority")
            .completers(this::completeTitleArg)
            .senderCaller(this::prio);
        rootNode.addChild("category").arguments("<title> [category]")
            .description("Set or reset category")
            .completers(this::completeTitleArg, this::completeCategoryArg)
            .senderCaller(this::category);
        rootNode.addChild("setsuffix").arguments("<title> [suffix]")
            .description("Set or reset suffix")
            .completers(this::completeTitleArg, this::completeSuffixOrCategoryArg)
            .senderCaller(this::setSuffix);
        rootNode.addChild("html").denyTabCompletion()
            .description("Export html")
            .senderCaller(this::html);
        rootNode.addChild("refreshplayers").denyTabCompletion()
            .description("Refresh player names")
            .senderCaller(this::refreshPlayers);
        rootNode.addChild("session").arguments("[player]")
            .description("Session info")
            .senderCaller(this::session);
        // /titles suffix
        CommandNode suffixNode = rootNode.addChild("suffix")
            .description("Suffix commands");
        suffixNode.addChild("list").denyTabCompletion()
            .description("List suffixes")
            .senderCaller(this::suffixList);
        suffixNode.addChild("info").arguments("<name>")
            .description("Suffix information")
            .completers(this::completeSuffixOrCategoryArg)
            .senderCaller(this::suffixInfo);
        suffixNode.addChild("create").arguments("<name> <format>")
            .description("Create or change suffix")
            .completers(this::completeSuffixArg, Emoji.HIDDEN_COMPLETER)
            .senderCaller(this::suffixCreate);
        suffixNode.addChild("category").arguments("<name> [category]")
            .description("Set or reset category")
            .completers(this::completeSuffixArg, this::completeSuffixCategoryArg)
            .senderCaller(this::suffixCategory);
        suffixNode.addChild("unlock").arguments("<suffix> <player>")
            .description("Unlock for player")
            .completers(this::completeSuffixOrCategoryArg, CommandArgCompleter.NULL)
            .senderCaller(this::suffixUnlock);
        suffixNode.addChild("lock").arguments("<suffix> <player>")
            .description("Lock for player")
            .completers(this::completeSuffixOrCategoryArg, CommandArgCompleter.NULL)
            .senderCaller(this::suffixLock);
        suffixNode.addChild("player").arguments("<player>")
            .description("List player suffixes")
            .completers(CommandArgCompleter.NULL)
            .senderCaller(this::suffixPlayer);
        // Shines
        CommandNode shinesNode = rootNode.addChild("shines")
            .description("Shine subcommands");
        shinesNode.addChild("show").arguments("<shine> [distance] [amount]")
            .description("Show a shine")
            .completers(SHINE_COMPLETER)
            .playerCaller(this::shinesShow);
        shinesNode.addChild("disable").denyTabCompletion()
            .description("Disable shines (temporarily)")
            .senderCaller(this::shinesDisable);
        shinesNode.addChild("enable").denyTabCompletion()
            .description("Enable shines")
            .senderCaller(this::shinesEnable);
        // Finis
        plugin.getCommand("titles").setExecutor(this);
        return this;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return rootNode.call(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return rootNode.complete(sender, command, label, args);
    }

    private static Component button(Title title) {
        Component titleComponent = title.getTitleComponent();
        Component tooltip = Component.join(JoinConfiguration.separator(Component.newline()),
                                           Component.text(title.getName(), NamedTextColor.GRAY),
                                           Component.text(title.formattedDescription()));
        return Component.text()
            .append(titleComponent)
            .insertion(title.getName())
            .hoverEvent(HoverEvent.showText(tooltip))
            .clickEvent(ClickEvent.runCommand("/title:titles info " + title.getName()))
            .build();
    }

    List<String> completeTitleArg(CommandContext context, CommandNode node, String arg) {
        String lower = arg.toLowerCase();
        return plugin.getTitles().stream()
            .map(Title::getName)
            .filter(name -> name.toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }

    List<String> completeCategoryArg(CommandContext context, CommandNode node, String arg) {
        return Stream.of(TitleCategory.values())
            .map(e -> e.key)
            .filter(key -> key.contains(arg))
            .collect(Collectors.toList());
    }

    List<String> completeSuffixArg(CommandContext context, CommandNode node, String arg) {
        return plugin.getSuffixes().values().stream()
            .map(SQLSuffix::getName)
            .filter(name -> name.contains(arg))
            .collect(Collectors.toList());
    }

    List<String> completeSuffixCategoryArg(CommandContext context, CommandNode node, String arg) {
        return plugin.getSuffixCategories().keySet().stream()
            .map(s -> "#" + s)
            .filter(key -> key.contains(arg))
            .collect(Collectors.toList());
    }

    List<String> completeSuffixOrCategoryArg(CommandContext context, CommandNode node, String arg) {
        return Stream.concat(plugin.getSuffixes().values().stream().map(SQLSuffix::getName),
                             plugin.getSuffixCategories().keySet().stream().map(s -> "#" + s))
            .filter(key -> key.contains(arg))
            .collect(Collectors.toList());
    }

    Title requireTitle(String titleName) {
        Title title = plugin.getTitle(titleName);
        if (title == null) throw new CommandWarn("Unknown title: " + titleName);
        return title;
    }

    SQLSuffix requireSuffix(String name) {
        SQLSuffix suffix = plugin.getSuffixes().get(name);
        if (suffix == null) throw new CommandWarn("Unknown suffix: " + name);
        return suffix;
    }

    private static PlayerCache requirePlayerCache(String playerName) {
        PlayerCache result = PlayerCache.forName(playerName);
        if (result == null) throw new CommandWarn("Player not found: " + playerName);
        return result;
    }

    boolean list(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        TextComponent.Builder cb = Component.text();
        cb.append(Component.text("All titles:", NamedTextColor.YELLOW));
        for (Title title : plugin.getTitles()) {
            cb.append(Component.text(" "));
            cb.append(button(title));
        }
        sender.sendMessage(cb.build());
        return true;
    }

    boolean player(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        PlayerCache player = requirePlayerCache(args[0]);
        List<Title> titles = plugin.getPlayerTitles(player.uuid);
        Title selectedTitle = plugin.getPlayerTitle(player.uuid);
        TextComponent.Builder cb = Component.text();
        cb.append(Component.text("Titles of " + player.name + ":", NamedTextColor.YELLOW));
        for (Title title: titles) {
            cb.append(Component.text(" "));
            if (selectedTitle != null && selectedTitle.getName().equals(title.getName())) {
                cb.append(Component.text("[", NamedTextColor.WHITE));
                cb.append(button(title));
                cb.append(Component.text("]", NamedTextColor.WHITE));
            } else {
                cb.append(button(title));
            }
        }
        sender.sendMessage(cb.build());
        return true;
    }

    boolean info(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        Title title = requireTitle(args[0]);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text()
                  .append(Component.text("Name: ", NamedTextColor.GRAY))
                  .append(Component.text(title.getName(), NamedTextColor.WHITE))
                  .insertion(title.getName()).build());
        lines.add(Component.text()
                  .append(Component.text("Title: ", NamedTextColor.GRAY))
                  .append(Component.text(title.formatted()))
                  .insertion(title.getTitle()).build());
        String json = title.getTitleJson();
        if (json != null) {
            lines.add(Component.text()
                      .append(Component.text("Component: ", NamedTextColor.GRAY))
                      .append(title.getTitleComponent())
                      .insertion(title.getTitleJson()).build());
        }
        if (json == null) json = "";
        lines.add(Component.text()
                  .append(Component.text("Json: ", NamedTextColor.GRAY))
                  .append(Component.text(json, NamedTextColor.WHITE))
                  .clickEvent(ClickEvent.suggestCommand("/titles json " + title.getName() + " " + json))
                  .insertion(json).build());
        String description = title.getDescription();
        if (description == null) description = "";
        lines.add(Component.text()
                  .append(Component.text("Description: ", NamedTextColor.GRAY))
                  .append(Component.text(description, NamedTextColor.WHITE))
                  .insertion(description)
                  .clickEvent(ClickEvent.suggestCommand("/titles desc " + title.getName() + " " + description))
                  .build());
        String nameColor = title.getNameColor();
        if (nameColor == null) nameColor = "";
        lines.add(Component.text()
                  .append(Component.text("Color: ", NamedTextColor.GRAY))
                  .append(Component.text(nameColor, NamedTextColor.WHITE))
                  .insertion(nameColor)
                  .clickEvent(ClickEvent.suggestCommand("/titles color " + title.getName() + " " + nameColor))
                  .build());
        lines.add(Component.text()
                  .append(Component.text("Prefix: ", NamedTextColor.GRAY))
                  .append(Component.text(title.isPrefix(), NamedTextColor.WHITE))
                  .clickEvent(ClickEvent.suggestCommand("/titles prefix " + title.getName() + " "))
                  .build());
        String shine = title.getShine();
        if (shine == null) shine = "";
        lines.add(Component.text()
                  .append(Component.text("Shine: ", NamedTextColor.GRAY))
                  .append(Component.text(shine, NamedTextColor.WHITE))
                  .insertion(shine).build());
        lines.add(Component.text()
                  .append(Component.text("Priority: ", NamedTextColor.GRAY))
                  .append(Component.text(title.getPriority(), NamedTextColor.WHITE))
                  .insertion("" + title.getPriority()).build());
        lines.add(Component.text()
                  .append(Component.text("Category: ", NamedTextColor.GRAY))
                  .append(title.getCategory() == null
                          ? Component.text("None", NamedTextColor.DARK_GRAY)
                          : Component.text(title.getCategory(), NamedTextColor.WHITE))
                  .build());
        lines.add(Component.text()
                  .append(Component.text("Suffix: ", NamedTextColor.GRAY))
                  .append(title.getSuffix() == null
                          ? Component.text("None", NamedTextColor.DARK_GRAY)
                          : Component.text(title.getSuffix(), NamedTextColor.WHITE))
                  .build());
        sender.sendMessage(Component.join(JoinConfiguration.separator(Component.newline()), lines));
        return true;
    }

    boolean listPlayers(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        Title title = requireTitle(args[0]);
        List<UnlockedInfo> unlocks = Database.listPlayers(title);
        StringBuilder sb = new StringBuilder("Owners of title ").append(title.getName()).append("(").append(unlocks.size()).append(")");
        for (UnlockedInfo row : unlocks) {
            UUID uuid = row.getPlayer();
            String name = PlayerCache.nameForUuid(uuid);
            sb.append(" ").append(name);
        }
        sender.sendMessage(Component.text(sb.toString(), NamedTextColor.YELLOW));
        return true;
    }

    boolean rankTitles(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        class Rank {
            Title title = null;
            int count = 0;
        }
        List<Rank> ranks = new ArrayList<>();
        for (Title title : plugin.getTitles()) {
            Rank rank = new Rank();
            rank.title = title;
            rank.count = Database.listPlayers(title).size();
            ranks.add(rank);
        }
        Collections.sort(ranks, (b, a) -> Integer.compare(a.count, b.count));
        int rankIter = 1;
        sender.sendMessage(Component.text("Ranking of titles by ownership (" + ranks.size() + ")", NamedTextColor.YELLOW));
        for (Rank rank: ranks) {
            sender.sendMessage("" + rankIter++ + ") " + rank.count + " " + rank.title.getName());
        }
        return true;
    }

    boolean create(CommandSender sender, String[] args) {
        if (args.length < 2) return false;
        Title title = plugin.getTitle(args[0]);
        String format = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (title != null) {
            title.setTitle(format);
            if (0 == plugin.getDb().update(title, "title")) {
                throw new CommandWarn("Failed to update title " + title.getName() + "!");
            }
            sender.sendMessage(Component.text()
                               .append(Component.text("Title " + title.getName() + " updated: ", NamedTextColor.YELLOW))
                               .append(title.getTitleTag())
                               .build());
        } else {
            title = new Title(args[0], format, null);
            if (!plugin.addTitle(title)) {
                throw new CommandWarn("Could not save title " + args[0] + "! Different case already exists?");
            }
            sender.sendMessage(Component.text()
                               .append(Component.text("Title " + title.getName() + " created: ", NamedTextColor.YELLOW))
                               .append(title.getTitleComponent())
                               .build());
        }
        return true;
    }

    boolean desc(CommandSender sender, String[] args) {
        if (args.length < 2) return false;
        Title title = requireTitle(args[0]);
        if (args.length == 1) {
            sender.sendMessage("Description of title '" + title.getName() + "': " + title.getDescription());
            return true;
        }
        String description = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        title.setDescription(description);
        plugin.getDb().update(title, "description");
        sender.sendMessage(Component.text()
                           .append(Component.text("Description of " + title.getName() + " updated: ", NamedTextColor.YELLOW))
                           .append(Component.text(title.getDescription(), NamedTextColor.GRAY))
                           .build());
        return true;
    }

    boolean unlock(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        PlayerCache player = requirePlayerCache(args[0]);
        Title title = requireTitle(args[1]);
        if (plugin.unlockPlayerTitle(player.uuid, title)) {
            sender.sendMessage(Component.text()
                               .append(Component.text("Unlocked title for " + player.name + ": ", NamedTextColor.YELLOW))
                               .append(title.getTitleTag())
                               .build());
        } else {
            sender.sendMessage(Component.text()
                               .append(Component.text(player.name + " already had title: ", NamedTextColor.RED))
                               .append(title.getTitleTag())
                               .build());
        }
        return true;
    }

    boolean lock(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        PlayerCache player = requirePlayerCache(args[0]);
        Title title = requireTitle(args[1]);
        if (plugin.lockPlayerTitle(player.uuid, title)) {
            sender.sendMessage(Component.text()
                               .append(Component.text("Locked title for " + player.name + ": ", NamedTextColor.YELLOW))
                               .append(title.getTitleTag())
                               .build());
        } else {
            sender.sendMessage(Component.text()
                               .append(Component.text(player.name + " doesn't have title: ", NamedTextColor.RED))
                               .append(title.getTitleTag())
                               .build());
        }
        return true;
    }

    boolean set(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        PlayerCache player = requirePlayerCache(args[0]);
        Title title = requireTitle(args[1]);
        if (!plugin.playerHasTitle(player.uuid, title)) {
            throw new CommandWarn(player.name + " does not have title " + title.getName());
        }
        if (!plugin.setPlayerTitle(player.uuid, title)) {
            throw new CommandWarn("Setting title " + title.getName() + " for " + player.name + " failed!");
        }
        sender.sendMessage(Component.join(JoinConfiguration.noSeparators(),
                                          Component.text("Set title ", NamedTextColor.YELLOW),
                                          title.getTitleTag(),
                                          Component.text(" for  ", NamedTextColor.YELLOW),
                                          Component.text(player.name)));
        return true;
    }

    boolean reset(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        PlayerCache player = requirePlayerCache(args[0]);
        if (!plugin.resetPlayerTitle(player.uuid)) {
            throw new CommandWarn("Resetting title of " + player.name + " failed!");
        }
        sender.sendMessage(Component.text()
                           .append(Component.text("Reset title of " + player.name + " to ", NamedTextColor.YELLOW))
                           .append(plugin.getPlayerTitle(player.uuid).getTitleComponent())
                           .build());
        return true;
    }

    boolean has(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        PlayerCache player = requirePlayerCache(args[0]);
        Title title = requireTitle(args[1]);
        if (plugin.playerHasTitle(player.uuid, title)) {
            sender.sendMessage(Component.text()
                               .append(Component.text(player.name + " has title: ", NamedTextColor.GREEN))
                               .append(title.getTitleComponent())
                               .build());
        } else {
            sender.sendMessage(Component.text()
                               .append(Component.text(player.getName() + " does not have title: ", NamedTextColor.RED))
                               .append(title.getTitleComponent())
                               .build());
        }
        return true;
    }

    boolean unlockSet(CommandSender sender, String[] args) {
        if (args.length < 2) return false;
        PlayerCache player = requirePlayerCache(args[0]);
        List<Title> titles = new ArrayList<>(args.length - 2);
        for (int i = 1; i < args.length; i += 1) {
            titles.add(requireTitle(args[i]));
        }
        Title title = null;
        boolean success = false;
        for (Title it : titles) {
            title = it;
            success = plugin.unlockPlayerTitle(player.uuid, title);
            if (success) break;
        }
        if (!success) {
            throw new CommandWarn(player.name + " already has title " + title.getName());
        }
        plugin.setPlayerTitle(player.uuid, title);
        sender.sendMessage(Component.text()
                           .append(Component.text("Unlocked and set title ", NamedTextColor.YELLOW))
                           .append(title.getTitleTag())
                           .append(Component.text(" for player ", NamedTextColor.YELLOW))
                           .append(Component.text(player.getName(), NamedTextColor.WHITE))
                           .build());
        Player online = Bukkit.getPlayer(player.uuid);
        if (online != null) {
            online.sendMessage(Component.text()
                               .append(Component.text("Title unlocked: ", NamedTextColor.WHITE))
                               .append(title.getTitleTag())
                               .build());
            net.kyori.adventure.title.Title titleComponent = net.kyori.adventure.title.Title
                .title(title.getTitleComponent(), Component.text("Title unlocked", NamedTextColor.WHITE));
            online.showTitle(titleComponent);
        }
        return true;
    }

    boolean search(CommandSender sender, String[] args) {
        if (args.length == 0) return false;
        String term = String.join(" ", args);
        List<Title> matches = new ArrayList<>();
        for (Title title : plugin.getTitles()) {
            if (title.getName().toLowerCase().contains(term)
                || title.stripped().toLowerCase().contains(term)
                || title.strippedDescription().toLowerCase().contains(term)) {
                matches.add(title);
            }
        }
        if (matches.isEmpty()) throw new CommandWarn("No match: " + term);
        TextComponent.Builder cb = Component.text();
        cb.append(Component.text("" + matches.size() + " titles matching: ", NamedTextColor.YELLOW));
        for (Title title : matches) {
            cb.append(Component.text(" "));
            cb.append(button(title));
        }
        sender.sendMessage(cb.build());
        return true;
    }

    boolean reload(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        sender.sendMessage(Component.text("Reloading database...", NamedTextColor.YELLOW));
        plugin.reloadAllData();
        return true;
    }

    boolean json(CommandSender sender, String[] args) {
        if (args.length < 1) return false;
        Title title = requireTitle(args[0]);
        String json =
            args.length < 2
            ? null
            : String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        title.setTitleJson(json);
        if (title.getTitleComponent().equals(Component.empty())) {
            throw new CommandWarn("Bad Json format: " + json);
        }
        if (0 == plugin.getDb().update(title, "title_json")) {
            throw new CommandWarn("Could not update title: " + title.getName() + "!");
        }
        if (json == null) {
            sender.sendMessage(Component.text("Json of title " + title.getName() + " reset", NamedTextColor.YELLOW));
        } else {
            sender.sendMessage(Component.text()
                               .append(Component.text("Json of title " + title.getName() + " set: ", NamedTextColor.YELLOW))
                               .append(title.getTitleComponent())
                               .build());
        }
        return true;
    }

    boolean nameColor(CommandSender sender, String[] args) {
        if (args.length < 1) return false;
        Title title = requireTitle(args[0]);
        String value = args.length < 2
            ? null
            : String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        title.setNameColor(value);
        if (0 == plugin.getDb().update(title, "name_color")) {
            throw new CommandWarn("Could not save title " + title.getName() + "!");
        }
        if (value == null) {
            sender.sendMessage(Component.text("Color of title " + title.getName() + " reset"));
        } else {
            sender.sendMessage(Component.text("Color of title " + title.getName() + " set: " + value));
        }
        return true;
    }

    boolean prefix(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        Title title = requireTitle(args[0]);
        boolean value;
        try {
            value = Boolean.parseBoolean(args[1]);
        } catch (IllegalArgumentException iae) {
            throw new CommandWarn("Invalid value: " + args[1]);
        }
        title.setPrefix(value);
        if (0 == plugin.getDb().update(title, "prefix")) {
            throw new CommandWarn("Could not save title " + title.getName() + "!");
        }
        sender.sendMessage(Component.text("Prefix of title " + title.getName() + " set: " + value, NamedTextColor.YELLOW));
        return true;
    }

    boolean shine(CommandSender sender, String[] args) {
        if (args.length != 1 && args.length != 2) return false;
            Title title = requireTitle(args[0]);
            Shine shine;
            Component text;
            if (args.length < 2) {
                shine = null;
                text = null;
            } else {
                shine = Shine.ofKey(args[1]);
                if (shine == null) {
                    throw new CommandWarn("Shine not found: " + args[1]);
                }
                text = Component.text(shine.name(), shine.color);
            }
            title.setShine(shine != null ? shine.key : null);
            if (0 == plugin.getDb().update(title, "shine")) {
                throw new CommandWarn("Could not save title: " + title.getName());
            }
            if (shine == null) {
                sender.sendMessage(Component.text("Shine of title " + title.getName() + " reset", NamedTextColor.YELLOW));
            } else {
                sender.sendMessage(Component.text()
                                   .append(Component.text("Shine of title " + title.getName() + " set: ", NamedTextColor.YELLOW))
                                   .append(text)
                                   .build());
            }
            return true;
    }

    boolean prio(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        Title title = requireTitle(args[0]);
        int priority;
        try {
            priority = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException iae) {
            throw new CommandWarn("Illegal priority: " + args[1]);
        }
        title.setPriority(priority);
        plugin.updateTitleList();
        if (0 == plugin.getDb().update(title, "priority")) {
            throw new CommandWarn("Could not update title: " + title.getName());
        }
        sender.sendMessage(Component.text("Priority of title " + title.getName() + " set to "
                                          + priority, NamedTextColor.YELLOW));
        return true;
    }

    boolean category(CommandSender sender, String[] args) {
        if (args.length != 1 && args.length != 2) return false;
        Title title = requireTitle(args[0]);
        String category = args.length >= 2 ? args[1] : null;
        if (Objects.equals(title.getCategory(), category)) {
            throw new CommandWarn(title.getName() + " already has category " + category + "!");
        }
        title.setCategory(category);
        title.setCategoryCache(null);
        plugin.updateTitleList();
        if (0 == plugin.getDb().update(title, "category")) {
            throw new CommandWarn("Could not update title: " + title.getName());
        }
        sender.sendMessage(Component.text("Category of title " + title.getName() + " set to "
                                          + title.getCategory(), NamedTextColor.YELLOW));
        return true;
    }

    boolean setSuffix(CommandSender sender, String[] args) {
        if (args.length != 1 && args.length != 2) return false;
        Title title = requireTitle(args[0]);
        String suffix = args.length >= 2 ? args[1] : null;
        if (Objects.equals(title.getSuffix(), suffix)) {
            throw new CommandWarn(title.getName() + " already has suffix " + suffix + "!");
        }
        title.setSuffix(suffix);
        if (0 == plugin.getDb().update(title, "suffix")) {
            throw new CommandWarn("Could not update title: " + title.getName());
        }
        sender.sendMessage(Component.text().content("Suffix of title " + title.getName() + " set to "
                                                    + title.getSuffix()).build());
        return true;
    }

    boolean html(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        sender.sendMessage(Component.text("Exporting html..."));
        HtmlExporter exporter = new HtmlExporter(plugin, sender);
        exporter.export();
        sender.sendMessage("Image files: " + exporter.getImageFiles());
        return true;
    }

    boolean delete(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        Title title = requireTitle(args[0]);
        if (!plugin.deleteTitle(title)) {
            throw new CommandWarn("Could not delete title " + title.getName() + "!");
        }
        sender.sendMessage(Component.text()
                           .append(Component.text("Title deleted: ", NamedTextColor.YELLOW))
                           .append(title.getTitleComponent())
                           .build());
        return true;
    }

    boolean refreshPlayers(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.updatePlayerName(player);
        }
        sender.sendMessage(Component.text("All player names refreshed", NamedTextColor.YELLOW));
        return true;
    }

    boolean session(CommandSender sender, String[] args) {
        if (args.length == 0) {
            int errors = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (plugin.findSession(player) == null) {
                    sender.sendMessage(Component.text("Player without session: " + player.getName(), NamedTextColor.RED));
                    errors += 1;
                }
            }
            for (Session session : plugin.getSessions().values()) {
                if (session.getPlayer() == null) {
                    sender.sendMessage(Component.text("Session without player: " + session.getPlayerName(), NamedTextColor.RED));
                    errors += 1;
                }
            }
            if (errors > 0) {
                sender.sendMessage(Component.text(errors + " errors!", NamedTextColor.RED));
            } else {
                sender.sendMessage(Component.text("No errors!", NamedTextColor.GREEN));
            }
            return true;
        } else if (args.length == 1) {
            PlayerCache player = requirePlayerCache(args[0]);
            Session session = plugin.getSessions().get(player.uuid);
            if (session == null) {
                sender.sendMessage(Component.text("No session: " + player.name, NamedTextColor.YELLOW));
                return true;
            }
            TextComponent.Builder cb = Component.text();
            cb.append(Component.text("Session info of " + player.name, NamedTextColor.YELLOW));
            cb.append(Component.newline());
            cb.append(Component.text().append(Component.text("UUID: ", NamedTextColor.GRAY))
                      .append(Component.text("" + session.getUuid(), NamedTextColor.WHITE)));
            cb.append(Component.newline());
            cb.append(Component.text().append(Component.text("Title: ", NamedTextColor.GRAY))
                      .append(session.playerRow.getTitle() != null
                              ? Component.text(session.playerRow.getTitle(), NamedTextColor.WHITE)
                              : Component.empty()));
            cb.append(Component.newline());
            cb.append(Component.text().append(Component.text("Shine: ", NamedTextColor.GRAY))
                      .append(session.playerRow.getShine() != null
                              ? Component.text(session.playerRow.getShine(), NamedTextColor.WHITE)
                              : Component.empty()));
            cb.append(Component.newline());
            cb.append(Component.text().append(Component.text("Suffix: ", NamedTextColor.GRAY))
                      .append(session.playerRow.getSuffix() != null
                              ? Component.text(session.playerRow.getSuffix(), NamedTextColor.WHITE)
                              : Component.empty()));
            cb.append(Component.newline());
            cb.append(Component.text().append(Component.text("Player List Prefix: ", NamedTextColor.GRAY))
                      .append(session.playerListPrefix != null ? session.playerListPrefix : Component.empty()));
            cb.append(Component.newline());
            cb.append(Component.text().append(Component.text("Player List Suffix: ", NamedTextColor.GRAY))
                      .append(session.playerListSuffix != null ? session.playerListSuffix : Component.empty()));
            cb.append(Component.newline());
            cb.append(Component.text().append(Component.text("Team Prefix: ", NamedTextColor.GRAY))
                      .append(session.teamPrefix));
            cb.append(Component.newline());
            cb.append(Component.text().append(Component.text("Team Suffix: ", NamedTextColor.GRAY))
                      .append(session.teamSuffix));
            cb.append(Component.newline());
            cb.append(Component.text().append(Component.text("Color: ", NamedTextColor.GRAY))
                      .append(Component.text(NamedTextColor.NAMES.key(session.color), NamedTextColor.WHITE)));
            cb.append(Component.newline());
            cb.append(Component.text().append(Component.text("Last Flying Shine: ", NamedTextColor.GRAY))
                      .append(session.lastFlyingShine != null
                              ? Component.text("" + session.lastFlyingShine.getBlockX()
                                               + " " + session.lastFlyingShine.getBlockY()
                                               + " " + session.lastFlyingShine.getBlockZ(),
                                               NamedTextColor.WHITE)
                              : Component.empty()));
            sender.sendMessage(cb.build());
            return true;
        } else {
            return false;
        }
    }

    boolean suffixList(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        TextComponent.Builder cb = Component.text();
        cb.append(Component.text("Categories:", NamedTextColor.YELLOW));
        for (String cat : plugin.getSuffixCategories().keySet()) {
            cb.append(Component.space());
            cb.append(Component.text()
                      .content("#" + cat).color(NamedTextColor.GRAY)
                      .hoverEvent(HoverEvent.showText(Component.text("/titles suffix info #" + cat)))
                      .clickEvent(ClickEvent.runCommand("/titles suffix info #" + cat))
                      .insertion("#" + cat).build());
        }
        cb.append(Component.newline());
        cb.append(Component.text("Suffixes:", NamedTextColor.YELLOW));
        for (SQLSuffix suffix : plugin.getSuffixes().values()) {
            cb.append(Component.space());
            cb.append(Component.text()
                      .content(suffix.getName()).color(NamedTextColor.WHITE)
                      .hoverEvent(HoverEvent.showText(suffix.getComponent()))
                      .clickEvent(ClickEvent.runCommand("/titles suffix info " + suffix.getName()))
                      .insertion(suffix.getName()).build());
        }
        sender.sendMessage(cb.build());
        return true;
    }

    boolean suffixInfo(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        if (args[0].startsWith("#")) {
            String key = args[0].substring(1);
            List<SQLSuffix> suffixList = plugin.getSuffixCategories().get(key);
            if (suffixList == null) {
                throw new CommandWarn("Suffix category not found: " + key);
            }
            TextComponent.Builder cb = Component.text();
            cb.append(Component.text("Category " + key + " has " + suffixList.size()
                                     + " suffixes:", NamedTextColor.YELLOW));
            for (SQLSuffix suffix : suffixList) {
                cb.append(Component.newline());
                cb.append(Component.text("- " + suffix.getName(), NamedTextColor.WHITE));
                cb.append(Component.space());
                cb.append(suffix.getComponent().insertion(suffix.getFormat()));
            }
            sender.sendMessage(cb.build());
            return true;
        }
        SQLSuffix suffix = requireSuffix(args[0]);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text("Suffix Information", NamedTextColor.YELLOW));
        lines.add(Component.text()
                  .append(Component.text("Name: ", NamedTextColor.GRAY))
                  .append(Component.text(suffix.getName(), NamedTextColor.WHITE).insertion(suffix.getName()))
                  .build());
        lines.add(Component.text()
                  .append(Component.text("Format: ", NamedTextColor.GRAY))
                  .append(Component.text(suffix.getFormat(), NamedTextColor.WHITE).insertion(suffix.getFormat()))
                  .build());
        lines.add(Component.text()
                  .append(Component.text("Priority: ", NamedTextColor.GRAY))
                  .append(Component.text("" + suffix.getPriority(), NamedTextColor.WHITE).insertion("" + suffix.getPriority()))
                  .build());
        lines.add(Component.text()
                  .append(Component.text("Category: ", NamedTextColor.GRAY))
                  .append(suffix.getCategory() == null
                          ? Component.text("None", NamedTextColor.DARK_GRAY)
                          : Component.text(suffix.getCategory(), NamedTextColor.WHITE).insertion("#" + suffix.getCategory()))
                  .build());
        lines.add(Component.text()
                  .append(Component.text("Component: ", NamedTextColor.GRAY))
                  .append(suffix.getComponent())
                  .build());
        lines.add(Component.text()
                  .append(Component.text("Part of Name: ", NamedTextColor.GRAY))
                  .append(suffix.isPartOfName()
                          ? Component.text("Yes", NamedTextColor.GREEN)
                          : Component.text("No", NamedTextColor.DARK_GRAY))
                  .build());
        lines.add(Component.text()
                  .append(Component.text("Invalid: ", NamedTextColor.GRAY))
                  .append(suffix.isInvalid()
                          ? Component.text("Yes", NamedTextColor.RED, TextDecoration.BOLD)
                          : Component.text("No", NamedTextColor.DARK_GRAY))
                  .build());
        sender.sendMessage(Component.join(JoinConfiguration.separator(Component.newline()), lines));
        return true;
    }

    boolean suffixCreate(CommandSender sender, String[] args) {
        if (args.length < 2) return false;
        String name = args[0];
        SQLSuffix suffix = plugin.getSuffixes().get(name);
        String format = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (suffix != null) {
            String old = suffix.getFormat();
            suffix.setFormat(format);
            suffix.unpack();
            if (suffix.isInvalid()) {
                suffix.setFormat(old);
                suffix.unpack();
                throw new CommandWarn("Invalid format: " + format);
            }
            if (0 == plugin.getDb().update(suffix, "format")) {
                throw new CommandWarn("Could not update suffix " + suffix.getName() + "!");
            }
            sender.sendMessage(Component.text()
                               .append(Component.text("Suffix " + suffix.getName() + " updated: ", NamedTextColor.YELLOW))
                               .append(suffix.getComponent())
                               .build());
        } else {
            suffix = new SQLSuffix(name, args[1]);
            suffix.unpack();
            if (suffix.isInvalid()) {
                throw new CommandWarn("Invalid format: " + format);
            }
            if (0 == plugin.getDb().insertIgnore(suffix)) {
                throw new CommandWarn("Could not create suffix " + name + "! Different case already exists?");
            }
            plugin.getSuffixes().put(suffix.getName(), suffix);
            sender.sendMessage(Component.text()
                               .append(Component.text("Suffix " + suffix.getName() + " created: ", NamedTextColor.YELLOW))
                               .append(suffix.getComponent())
                               .build());
        }
        return true;
    }

    boolean suffixCategory(CommandSender sender, String[] args) {
        if (args.length != 1 && args.length != 2) return false;
        SQLSuffix suffix = requireSuffix(args[0]);
        String old = suffix.getCategory();
        String category = args.length >= 2 ? args[1] : null;
        if (category != null && category.startsWith("#")) {
            category = category.substring(1);
        }
        if (Objects.equals(old, category)) {
            throw new CommandWarn("Suffix " + suffix.getName() + " already has category " + category + "!");
        }
        suffix.setCategory(category);
        if (0 == plugin.getDb().update(suffix, "category")) {
            suffix.setCategory(old);
            throw new CommandWarn("Could not update suffix " + suffix.getName() + "!");
        }
        sender.sendMessage(Component.text("Set category of suffix " + suffix.getName() + " to "
                                          + suffix.getCategory(), NamedTextColor.YELLOW));
        return true;
    }

    boolean suffixUnlock(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        String suffixName = args[0];
        if (!suffixName.startsWith("#")) requireSuffix(suffixName);
        PlayerCache player = requirePlayerCache(args[1]);
        if (!plugin.unlockPlayerSuffix(player.uuid, suffixName)) {
            throw new CommandWarn(player.name + " already has suffix " + suffixName + " unlocked!");
        }
        sender.sendMessage(Component.text("Suffix " + suffixName + " unlocked for " + player.name, NamedTextColor.YELLOW));
        return true;
    }

    boolean suffixLock(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        String suffixName = args[0];
        // We allow locking of invalid suffixes!
        PlayerCache player = requirePlayerCache(args[1]);
        if (!plugin.lockPlayerSuffix(player.uuid, suffixName)) {
            throw new CommandWarn(player.name + " does not not have suffix " + suffixName + " unlocked!");
        }
        sender.sendMessage(Component.text("Suffix " + suffixName + " locked for " + player.name, NamedTextColor.YELLOW));
        return true;
    }

    boolean suffixPlayer(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        PlayerCache player = requirePlayerCache(args[0]);
        List<SQLSuffix> list = plugin.getPlayerSuffixes(player.uuid);
        TextComponent.Builder cb = Component.text();
        cb.append(Component.text(player.name + " has " + list.size() + " suffixes:", NamedTextColor.YELLOW));
        for (SQLSuffix suffix : list) {
            cb.append(Component.space());
            cb.append(Component.text().content(suffix.getName()).color(NamedTextColor.YELLOW)
                      .hoverEvent(HoverEvent.showText(Component.text("/titles suffix info " + suffix.getName())))
                      .clickEvent(ClickEvent.runCommand("/titles suffix info " + suffix.getName())));
        }
        sender.sendMessage(cb.build());
        return true;
    }

    protected boolean shinesShow(Player player, String[] args) {
        if (args.length != 1 && args.length != 3) return false;
        Shine shine = Shine.ofKey(args[0]);
        if (shine == null) {
            throw new CommandWarn("Shine not found: " + args[0]);
        }
        if (args.length == 1) {
            ShinePlace.of(player.getEyeLocation(), new Vector(0.0, 2.0, 0.0), 2.0).show(shine);
        } else if (args.length == 3) {
            // /shine <shine> <distance> <times>
            Location loc = player.getEyeLocation();
            Vector vec = loc.getDirection();
            try {
                double dist = Double.parseDouble(args[1]);
                int amount = Integer.parseInt(args[2]);
                player.sendMessage("shine=" + shine + " dist=" + dist + " amount=" + amount);
                Location shineLocation = player.getEyeLocation();
                shineLocation.setDirection(shineLocation.getDirection().multiply(-1));
                Vector shineVector = vec.normalize().multiply(dist);
                for (int i = 0; i < amount; i += 1) {
                    ShinePlace.of(shineLocation.clone(), shineVector, 2.0).show(shine);
                }
            } catch (IllegalArgumentException iae) {
                throw new CommandWarn("Invalid arguments: " + args[1] + ", " + args[2]);
            }
        }
        return true;
    }

    protected boolean shinesDisable(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        plugin.shinesDisabled = true;
        sender.sendMessage(Component.text("Shines disabled!", NamedTextColor.RED));
        return true;
    }

    protected boolean shinesEnable(CommandSender sender, String[] args) {
        if (args.length != 0) return false;
        plugin.shinesDisabled = false;
        sender.sendMessage(Component.text("Shines enabled!", NamedTextColor.AQUA));
        return true;
    }
}
