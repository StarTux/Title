package com.winthier.title;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
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
            sender.sendMessage("Player expected");
            return true;
        }
        try {
            if (args.length == 0) {
                if (player == null) throw new CommandException("Player expected");
                plugin.send(sender, "");
                String name = plugin.getDb().getPlayerTitleName(player.getUniqueId());
                List<Title> titles = plugin.getDb().listTitles(player.getUniqueId());
                if (titles.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "No titles to show!");
                    return true;
                }
                Collections.sort(titles);
                Title currentTitle = plugin.getDb().getPlayerTitle(player.getUniqueId());
                player.sendMessage(Msg.builder(plugin.format("&3&lYour Titles &3(Current: &r"))
                                   .append(currentTitle != null ? currentTitle.getTitleComponent() : Msg.text(""))
                                   .append(plugin.format("&r&3) &7&oClick to switch"))
                                   .create());
                ComponentBuilder cb = new ComponentBuilder();
                for (Title title : titles) {
                    cb.append(" ").reset();
                    cb.append("[");
                    cb.append(title.getTitleComponent());
                    ComponentBuilder tooltip = new ComponentBuilder(title.getTitleComponent());
                    if (title.getDescription() != null) {
                        tooltip.append("\n").reset().append(title.formattedDescription());
                    }
                    if (title.getPlayerListPrefix() != null) {
                        tooltip.append("\n").reset().append("Player List ").color(ChatColor.GRAY)
                            .append(title.formattedPlayerListPrefix());
                    }
                    tooltip.append("\n").reset().append("Click to use this title").color(ChatColor.AQUA).italic(true);
                    cb.event(Msg.hover(tooltip.create()));
                    cb.event(Msg.click("/title " + title.getName()));
                    cb.append("]").reset();
                }
                sender.sendMessage(cb.create());
                plugin.send(sender, "");
            } else if (args.length == 1) {
                if (player == null) throw new CommandException("Player expected");
                final String title = args[0];
                if ("default".equalsIgnoreCase(title)) {
                    plugin.getDb().setPlayerTitle(player.getUniqueId(), null);
                    plugin.updatePlayerListName(player);
                    plugin.send(player, "&bUsing default title.");
                } else {
                    if (!plugin.getDb().playerHasTitle(player.getUniqueId(), title)) {
                        throw new CommandException("You don't have that title.");
                    }
                    plugin.getDb().setPlayerTitle(player.getUniqueId(), title);
                    plugin.updatePlayerListName(player);
                    Title result = plugin.getDb().getTitle(title);
                    if (result == null) {
                        plugin.getLogger().warning(player.getName() + " managed to set unknown title " + title + ".");
                        throw new CommandException("You don't have that title.");
                    }
                    plugin.send(player, "&bSet title to &r%s&b.", result.formatted());
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
