package com.winthier.title;

import com.cavetale.core.event.connect.ConnectMessageEvent;
import com.cavetale.core.event.perm.PlayerPermissionUpdateEvent;
import com.cavetale.core.playercache.PlayerCache;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@RequiredArgsConstructor
public final class PlayerListener implements Listener {
    private final TitlePlugin plugin;

    public PlayerListener enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        return this;
    }

    @EventHandler
    protected void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.enter(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.resetPlayerScoreboards(player);
        plugin.exit(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onPlayerPermissionUpdate(PlayerPermissionUpdateEvent event) {
        Player player = event.getPlayer();
        Session session = plugin.findSession(player);
        if (session != null) {
            session.updateValidity();
        }
        plugin.updatePlayerName(player);
        Map<String, Boolean> changes = event.getPermissionChanges();
        List<Title> newTitles = new ArrayList<>();
        for (Title title : plugin.getTitles()) {
            if (!changes.getOrDefault(title.getPermission(), false)) continue;
            newTitles.add(title);
        }
        if (!newTitles.isEmpty()) {
            final int sz = newTitles.size();
            List<Component> messages = new ArrayList<>(sz + 1);
            messages.add(text("Title" + (sz == 1 ? "" : "s") + " unlocked. Click to wear:", GREEN)
                         .hoverEvent(showText(text("/title", GRAY)))
                         .clickEvent(runCommand("/title")));
            for (Title title : newTitles) {
                messages.add(title.getTitleTag(player.getUniqueId()));
            }
            player.sendMessage(textOfChildren(newline(), join(separator(space()), messages), newline()));
        }
    }

    @EventHandler
    private void onConnectMessage(ConnectMessageEvent event) {
        if (event.getChannel().equals("connect:player_update")) {
            final UUID uuid = UUID.fromString(event.getPayload(String.class));
            final String name = PlayerCache.nameForUuid(uuid);
            plugin.getLogger().info("Update received: " + name + " " + uuid);
            plugin.enter(uuid, name);
        }
    }
}
