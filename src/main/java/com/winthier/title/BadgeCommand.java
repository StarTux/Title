package com.winthier.title;

import com.winthier.title.sql.SQLSuffix;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class BadgeCommand implements TabExecutor {
    private final TitlePlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            sender.sendMessage("[title:badge] player expected");
            return true;
        }
        if (args.length != 1 && args.length != 0) return false;
        Session session = plugin.findSession(player);
        if (session == null) {
            player.sendMessage(Component.text("Please try again later!", NamedTextColor.RED));
            return true;
        }
        List<SQLSuffix> suffixes = session.getSuffixes(player);
        SQLSuffix activeSuffix = session.getSuffix(player);
        if (args.length == 0) {
            if (suffixes.isEmpty()) {
                sender.sendMessage(Component.text("No badges to show", NamedTextColor.RED));
                return true;
            }
            TextComponent.Builder cb = Component.text();
            cb.append(Component.text("Your badges:", NamedTextColor.GREEN));
            for (SQLSuffix suffix : suffixes) {
                cb.append(Component.space());
                boolean active = suffix.is(activeSuffix);
                if (active) cb.append(Component.text("[", NamedTextColor.WHITE));
                Component tooltip = Component
                    .join(JoinConfiguration.separator(Component.newline()),
                          suffix.getComponent(),
                          Component.text("Click here to wear this badge", NamedTextColor.GRAY));
                cb.append(suffix.getComponent()
                          .hoverEvent(HoverEvent.showText(tooltip))
                          .clickEvent(ClickEvent.runCommand("/badge " + suffix.getName())));
                if (active) cb.append(Component.text("]", NamedTextColor.WHITE));
            }
            cb.append(Component.newline());
            cb.append(Component.text().content("[Clear]").color(NamedTextColor.RED)
                      .hoverEvent(HoverEvent.showText(Component.text("Click here to clear your badge", NamedTextColor.RED)))
                      .clickEvent(ClickEvent.runCommand("/badge none")));
            sender.sendMessage(cb.build());
            return true;
        }
        if (args[0].equals("none")) {
            if (session.getSuffix() == null) {
                player.sendMessage(Component.text("You aren't wearing a badge!", NamedTextColor.RED));
                return true;
            }
            session.resetSuffix(player);
            player.sendMessage(Component.text("Badge deactivated", NamedTextColor.GREEN));
            return true;
        }
        SQLSuffix suffix = plugin.getSuffixes().get(args[0]);
        if (suffix == null || !session.hasSuffix(player, suffix)) {
            player.sendMessage(Component.text("You don't have that badge!", NamedTextColor.RED));
            return true;
        }
        if (suffix.is(activeSuffix)) {
            player.sendMessage(Component.text("You're already wearing this badge!", NamedTextColor.RED));
            return true;
        }
        session.setSuffix(player, suffix);
        player.sendMessage(Component.text()
                           .append(Component.text("Badge selected: "))
                           .append(suffix.getComponent()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        final Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) return null;
        if (args.length == 1) {
            return Stream.concat(Stream.of("none"),
                                 plugin.getPlayerSuffixes(player.getUniqueId()).stream()
                                 .map(SQLSuffix::getName)
                                 .filter(s -> s.contains(args[0])))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
