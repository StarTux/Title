package com.winthier.title;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
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
            sender.sendMessage("[title:title] player expected");
            return true;
        }
        Title title = plugin.getDb().getCachedTitle(player.getUniqueId());
        if (title == null || title.getShine() == null) {
            player.sendMessage(ChatColor.RED + "You don't have a shine!");
            return true;
        }
        Shine shine;
        try {
            shine = Shine.valueOf(title.getShine().toUpperCase());
        } catch (IllegalArgumentException iae) {
            player.sendMessage(ChatColor.RED + "You don't have a shine!");
            return true;
        }
        ShinePlace.of(player.getEyeLocation(), new Vector(0.0, 2.0, 0.0), 2.0).show(shine);
        player.sendMessage(Msg.builder("Showing shine: " + shine.humanName).color(shine.color).create());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return Collections.emptyList();
    }
}
