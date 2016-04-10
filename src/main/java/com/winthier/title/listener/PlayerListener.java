package com.winthier.title.listener;

import com.winthier.title.TitlePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    public final TitlePlugin plugin;

    public PlayerListener(TitlePlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.updatePlayer(event.getPlayer());
    }
}
