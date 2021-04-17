package com.winthier.title;

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
        try {
            if (args.length == 0) {
                if (player == null) throw new CommandException("Player expected");
                plugin.send(sender, "");
                String name = plugin.getDb().getPlayerTitleName(player.getUniqueId());
                List<Title> titles = plugin.getDb().listTitles(player.getUniqueId());
                if (titles.isEmpty()) {
                    player.sendMessage(Component.text("No titles to show!", NamedTextColor.RED));
                    return true;
                }
                Title currentTitle = plugin.getDb().getPlayerTitle(player.getUniqueId());
                player.sendMessage(Component.text()
                                   .content(Msg.colorize("&3&lYour Titles &3(Current: &r"))
                                   .append(currentTitle != null ? currentTitle.getTitleComponent() : Component.empty())
                                   .append(Component.text(Msg.colorize("&r&3) &7&oClick to switch")))
                                   .build());
                TextComponent.Builder cb = Component.text();
                for (Title title : titles) {
                    cb.append(Component.text(" "));
                    TextComponent.Builder tooltip = Component.text();
                    tooltip.append(title.getTitleComponent());
                    if (title.getDescription() != null) {
                        tooltip.append(Component.text("\n" + title.formattedDescription()));
                    }
                    if (title.getPlayerListPrefix() != null) {
                        tooltip.append(Component.text("\nPlayer List ", NamedTextColor.GRAY));
                        tooltip.append(Component.text(title.formattedPlayerListPrefix()));
                    }
                    tooltip.append(Component.text().content("\nClick to use this title").color(NamedTextColor.AQUA).decorate(TextDecoration.ITALIC).build());
                    cb.append(Component.text()
                              .append(Component.text("["))
                              .append(title.getTitleComponent())
                              .append(Component.text("]"))
                              .hoverEvent(HoverEvent.showText(tooltip.build()))
                              .clickEvent(ClickEvent.runCommand("/title " + title.getName())));
                }
                sender.sendMessage(cb.build());
                plugin.send(sender, "");
            } else if (args.length == 1) {
                if (player == null) throw new CommandException("Player expected");
                final String name = args[0];
                if ("default".equalsIgnoreCase(name)) {
                    plugin.getDb().setPlayerTitle(player.getUniqueId(), null);
                    plugin.updatePlayerListName(player);
                    plugin.send(player, "&bUsing default title.");
                } else {
                    Title title = plugin.getDb().getTitle(name);
                    if (title == null || !plugin.getDb().playerHasTitle(player.getUniqueId(), title)) {
                        throw new CommandException("You don't have that title.");
                    }
                    plugin.getDb().setPlayerTitle(player.getUniqueId(), title);
                    plugin.updatePlayerListName(player);
                    player.sendMessage(TextComponent.ofChildren(Component.text("Set title to ", NamedTextColor.AQUA),
                                                                title.getTitleComponent()));
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
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String label, String[] args) {
        if (!(sender instanceof Player)) return null;
        Player player = (Player) sender;
        if (args.length == 0) return null;
        if (args.length == 1) {
            return plugin.getDb().listTitles(player.getUniqueId()).stream()
                .map(Title::getName)
                .filter(s -> s.startsWith(args[0]))
                .collect(Collectors.toList());
        }
        return null;
    }
}
