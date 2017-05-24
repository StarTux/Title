package com.winthier.title;

import java.util.UUID;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {
    private final TitlePlugin plugin;

    Vault(TitlePlugin plugin) {
        this.plugin = plugin;
    }

    Permission getPermission() {
        RegisteredServiceProvider<Permission> registration = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (registration == null) return null;
        return registration.getProvider();
    }

    public boolean hasPermission(UUID uuid, String perm) {
        Permission permission = getPermission();
        if (permission == null || !permission.isEnabled()) return false;
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(uuid);
        return permission.playerHas((String)null, player, perm);
    }
}
