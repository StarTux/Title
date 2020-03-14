package com.winthier.title;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class TitleCommand implements TabExecutor {
    public final TitlePlugin plugin;
    Gson gson = new Gson();

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
                String name = plugin.getDb().getPlayerTitle(player.getUniqueId());
                List<Title> titles = plugin.getDb().listTitles(player.getUniqueId());
                Title currentTitle = plugin.getPlayerTitle(player.getUniqueId());
                plugin.send(sender, "&3&lYour Titles &3(Current: &r%s&r&3) &7&oClick to switch", currentTitle.formatted());
                List<Object> message = new ArrayList<>();
                message.add(button("&r[&7Default&r]",
                                   "&7Click to reset your\n&7title to the default",
                                   "/title default"));
                for (Title title: titles) {
                    message.add(" ");
                    message.add(button("&r[" + title.getTitle() + "&r]",
                                       "&7Click to change your\n&7title to " + title.getTitle(),
                                       "/title " + title.getName()));
                }
                tellRaw(player, message);
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

    Object button(String chat, String tooltip, String command) {
        Map<String, Object> map = new HashMap<>();
        map.put("text", plugin.format(chat));
        Map<String, Object> map2 = new HashMap<>();
        map.put("clickEvent", map2);
        map2.put("action", "run_command");
        map2.put("value", command);
        map2 = new HashMap<>();
        map.put("hoverEvent", map2);
        map2.put("action", "show_text");
        map2.put("value", plugin.format(tooltip));
        return map;
    }

    void tellRaw(Player player, Object json) {
        String js;
        try {
            js = gson.toJson(json);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                           "minecraft:tellraw " + player.getName() + " " + js);
    }
}
