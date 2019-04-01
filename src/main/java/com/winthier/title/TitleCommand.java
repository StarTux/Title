package com.winthier.title;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandExecutor;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import java.util.List;

public final class TitleCommand implements CommandExecutor {
    public final TitlePlugin plugin;

    public TitleCommand(TitlePlugin plugin) {
        this.plugin = plugin;
    }

    static class CommandException extends Exception {
        CommandException(String msg) {
            super(msg);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) {
            sender.sendMessage("Player expected");
            return true;
        }
        try {
            if (args.length == 0) {
                if (player == null) throw new CommandException("Player expected");
                plugin.send(sender, "");
                String name = plugin.getDb().getPlayerTitle(player.getUniqueId());
                List<Title> titles = plugin.getDb().listTitles(player.getUniqueId());
                Title currentTitle = plugin.getPlayerTitle(player.getUniqueId());
                plugin.send(sender, "&3&lYour Titles &3(Current: &r%s&r&3)", currentTitle.formatted());
                StringBuilder sb = new StringBuilder();
                sb.append(TextFormat.colorize("&r[&7Default&r]"));
                for (Title title: titles) {
                    sb.append(" ");
                    sb.append(TextFormat.colorize("&r[" + title.getTitle() + "&r]"));
                }
                player.sendMessage(sb.toString());
                plugin.send(sender, "");
            } else if (args.length == 1) {
                if (player == null) throw new CommandException("Player expected");
                final String title = args[0];
                if ("default".equalsIgnoreCase(title)) {
                    plugin.getDb().setPlayerTitle(player.getUniqueId(), null);
                    plugin.send(player, "&bUsing default title.");
                } else {
                    if (!plugin.getDb().playerHasTitle(player.getUniqueId(), title)) {
                        throw new CommandException("You don't have that title.");
                    }
                    plugin.getDb().setPlayerTitle(player.getUniqueId(), title);
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
            sender.sendMessage("" + TextFormat.RED + ce.getMessage());
        }
        return true;
    }
}
