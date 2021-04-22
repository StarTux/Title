package com.winthier.title;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public final class ShineCommand implements TabExecutor {
    private final TitlePlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 0 && args.length != 1 && args.length != 3) return false;
        final Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            sender.sendMessage("[title:title] player expected");
            return true;
        }
        Shine shine;
        if (args.length >= 1) {
            if (!player.hasPermission("title.shine.any")) {
                player.sendMessage(ChatColor.RED + "You don't have permission!");
                return true;
            }
            try {
                shine = Shine.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException iae) {
                player.sendMessage(ChatColor.RED + "Shine not found: " + args[0]);
                return true;
            }
            if (args.length == 3) {
                // /shine <shine> <distance> <times>
                Location loc = player.getEyeLocation();
                Vector vec = loc.getDirection();
                try {
                    double dist = Double.parseDouble(args[1]);
                    int amount = Integer.parseInt(args[2]);
                    player.sendMessage("shine=" + shine + " dist=" + dist + " amount=" + amount);
                    for (int i = 0; i < amount; i += 1) {
                        ShinePlace.of(player.getEyeLocation(), vec.normalize().multiply(dist), 2.0).show(shine);
                    }
                } catch (IllegalArgumentException iae) {
                    player.sendMessage(ChatColor.RED + "Invalid arguments: " + args[1] + ", " + args[2]);
                }
                return true;
            }
        } else {
            Title title = plugin.getDb().getCachedTitle(player.getUniqueId());
            if (title == null || title.getShine() == null) {
                player.sendMessage(ChatColor.RED + "You don't have a shine!");
                return true;
            }
            try {
                shine = Shine.valueOf(title.getShine().toUpperCase());
            } catch (IllegalArgumentException iae) {
                player.sendMessage(ChatColor.RED + "You don't have a shine!");
                return true;
            }
        }
        ShinePlace.of(player.getEyeLocation(), new Vector(0.0, 2.0, 0.0), 2.0).show(shine);
        player.sendMessage(Component.text().content("Showing shine: " + shine.humanName).color(shine.color).build());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("title.shine.any")) {
            return Stream.of(Shine.values())
                .map(Shine::name)
                .map(String::toLowerCase)
                .filter(s -> s.startsWith(args[0]))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
