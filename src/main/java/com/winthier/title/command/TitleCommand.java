package com.winthier.title.command;

import com.winthier.title.Title;
import com.winthier.title.TitlePlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONValue;

public class TitleCommand implements CommandExecutor {
    public final TitlePlugin plugin;

    public TitleCommand(TitlePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        final Player player = sender instanceof Player ? (Player)sender : null;
        if (player == null) {
            sender.sendMessage("Player expected");
            return true;
        }
        try {
            if (args.length == 0) {
                if (player == null) throw new CommandException("Player expected");
                plugin.send(sender, "");
                String name = plugin.database.getPlayerTitle(player);
                Map<String, String> titles = plugin.database.listTitles(player);
                plugin.send(sender, "&3&lYour Titles &3(Current: &r%s&r&3) &7&oClick to switch", plugin.chat.getPlayerPrefix(player));
                List<Object> message = new ArrayList<>();
                message.add(button("&r[&7Default&r]",
                                   "&7Click to reset your\n&7title to the default",
                                   "/title default"));
                for (Map.Entry<String, String> entry : titles.entrySet()) {
                    message.add(" ");
                    message.add(button("&r["+entry.getValue()+"&r]",
                                       "&7Click to change your\n&7title to "+entry.getValue(),
                                       "/title "+entry.getKey()));
                }
                tellRaw(player, message);
                plugin.send(sender, "");
            } else if (args.length == 1) {
                if (player == null) throw new CommandException("Player expected");
                final String title = args[0];
                if ("default".equalsIgnoreCase(title)) {
                    plugin.database.setPlayerTitle(player, null);
                    plugin.send(player, "&bUsing default title.");
                } else {
                    if (!plugin.database.playerHasTitle(player, title)) {
                        throw new CommandException("You don't have that title.");
                    }
                    plugin.database.setPlayerTitle(player, title);
                    Title result = plugin.database.getTitle(title);
                    if (result == null) {
                        plugin.getLogger().warning(player.getName() + " managed to set unknown title " + title + ".");
                        throw new CommandException("You don't have that title.");
                    }
                    plugin.send(player, "&bSet title to &r%s&b.", result.formatted());
                }
                plugin.updatePlayer(player);
            } else {
                return false;
            }
        } catch (CommandException ce) {
            sender.sendMessage("" + ChatColor.RED + ce.getMessage());
        }
        return true;
    }

    Object button(String chat, String tooltip, String command)
    {
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

    static void tellRaw(Player player, Object json) {
        String js;
        try {
            js = JSONValue.toJSONString(json);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "minecraft:tellraw " + player.getName() + " " + js);
    }
}
