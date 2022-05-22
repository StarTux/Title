package com.winthier.title;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class TitleCommand implements TabExecutor {
    public final TitlePlugin plugin;

    public TitleCommand(final TitlePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            sender.sendMessage("[title:title] player expected");
            return true;
        }
        if (args.length > 1) return false;
        try {
            return args.length == 0
                ? list(player)
                : select(player, args[0]);
        } catch (CommandException ce) {
            sender.sendMessage(Component.text(ce.getMessage(), NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String label, String[] args) {
        if (!(sender instanceof Player)) return null;
        Player player = (Player) sender;
        if (args.length == 0) return null;
        if (args.length == 1) {
            return plugin.getPlayerTitles(player).stream()
                .map(Title::getName)
                .filter(s -> s.startsWith(args[0]))
                .collect(Collectors.toList());
        }
        return null;
    }

    boolean list(Player player) {
        player.sendMessage("");
        List<Title> titles = plugin.getPlayerTitles(player);
        if (titles.isEmpty()) {
            player.sendMessage(Component.text("No titles to show!", NamedTextColor.RED));
            return true;
        }
        Title currentTitle = plugin.getPlayerTitle(player);
        player.sendMessage(Component.text()
                           .append(Component.text("Your Titles", NamedTextColor.DARK_AQUA, TextDecoration.BOLD))
                           .append(Component.space())
                           .append(Component.text("(Current:", NamedTextColor.DARK_AQUA))
                           .append(Component.space())
                           .append(currentTitle != null ? currentTitle.getTitleComponent() : Component.empty())
                           .append(Component.text(")", NamedTextColor.DARK_AQUA))
                           .append(Component.space())
                           .append(Component.text("Click to switch", NamedTextColor.GRAY, TextDecoration.ITALIC))
                           .build());
        TextComponent.Builder cb = Component.text();
        for (Title title : titles) {
            cb.append(Component.text(" "));
            TextComponent.Builder tooltip = Component.text();
            tooltip.append(title.getTitleComponent());
            if (title.getDescription() != null) {
                tooltip.append(Component.text("\n" + title.formattedDescription()));
            }
            tooltip.append(Component.text("\nClick to use this title", NamedTextColor.AQUA, TextDecoration.ITALIC));
            cb.append(Component.text()
                      .append(Component.text("["))
                      .append(title.getTitleComponent())
                      .append(Component.text("]"))
                      .hoverEvent(HoverEvent.showText(tooltip.build()))
                      .clickEvent(ClickEvent.runCommand("/title " + title.getName())));
        }
        player.sendMessage(cb.build());
        player.sendMessage("");
        PluginPlayerEvent.Name.LIST_PLAYER_TITLES.call(plugin, player);
        return true;
    }

    boolean select(Player player, String titleName) {
        Session session = plugin.findSession(player);
        if (session == null) {
            throw new CommandException("Session not found. Please try again later!");
        }
        if ("default".equalsIgnoreCase(titleName)) {
            session.resetTitle(player);
            player.sendMessage(Component.text("Using default title", NamedTextColor.AQUA));
            return true;
        }
        Title title = plugin.getTitle(titleName);
        if (title == null || !session.hasTitle(player, title)) {
            throw new CommandException("You don't have that title.");
        }
        List<Title> titles = session.getTitles(player);
        if (titles.indexOf(title) == 0) {
            session.resetTitle(player);
        } else {
            session.setTitle(player, title);
        }
        player.sendMessage(Component.text()
                           .append(Component.text("Set title to ", NamedTextColor.AQUA))
                           .append(title.getTitleTag()));
        PluginPlayerEvent.Name.SELECT_PLAYER_TITLE.make(plugin, player)
            .detail(Detail.NAME, title.getName()).callEvent();
        return true;
    }
}
