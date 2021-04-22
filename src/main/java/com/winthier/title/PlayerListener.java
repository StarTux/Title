package com.winthier.title;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
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
    void onPlayerJoin(PlayerJoinEvent event) {
        plugin.updatePlayerName(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getDb().clearCache(event.getPlayer().getUniqueId());
        event.getPlayer().setPlayerListName(null);
    }
}
