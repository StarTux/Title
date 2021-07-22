package com.winthier.title;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
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
        final Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            sender.sendMessage("[title:shine] player expected");
            return true;
        }
        if (args.length == 0) {
            Shine shine = plugin.getPlayerShine(player);
            if (shine == null) {
                player.sendMessage(ChatColor.RED + "You don't have a shine selected!");
                return true;
            }
            ShinePlace.of(player.getEyeLocation(), new Vector(0.0, 2.0, 0.0), 2.0).show(shine);
            player.sendMessage(Component.text()
                               .content("Showing shine: " + shine.humanName)
                               .color(shine.color).build());
            return true;
        }
        if (args.length == 1) {
            Session session = plugin.findSession(player);
            if (session == null) {
                player.sendMessage(Component.text("Please try again later!", NamedTextColor.RED));
                return true;
            }
            Shine shine;
            if (args[0].equals("default")) {
                session.resetShine();
                player.sendMessage(Component.text("Shine unselected", NamedTextColor.AQUA));
                return true;
            } else {
                shine = Shine.ofKey(args[0]);
                if (shine == null) {
                    player.sendMessage(ChatColor.RED + "Unknown shine: " + args[0]);
                    return true;
                }
                session.setShine(shine);
            }
            ShinePlace.of(player.getEyeLocation(), new Vector(0.0, 2.0, 0.0), 2.0).show(shine);
            player.sendMessage(Component.text()
                               .content("Shine selected: " + shine.humanName)
                               .color(shine.color).build());
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        final Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) return null;
        if (args.length == 1) {
            return Stream.concat(Stream.of("default"),
                                 plugin.getPlayerShines(player).stream()
                                 .map(Shine::getKey)
                                 .filter(s -> s.contains(args[0])))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
