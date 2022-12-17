package com.winthier.title;

import com.cavetale.core.event.connect.ConnectMessageEvent;
import com.cavetale.core.event.perm.PlayerPermissionUpdateEvent;
import com.cavetale.core.playercache.PlayerCache;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
