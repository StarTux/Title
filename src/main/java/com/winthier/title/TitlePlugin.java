package com.winthier.title;

import com.winthier.title.sql.Database;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class TitlePlugin extends JavaPlugin {
    private final Database db = new Database(this);
    @Getter static TitlePlugin instance;
    private Map<UUID, String> playerListSuffixes = new HashMap<>();
    private Map<UUID, String> playerListPrefixes = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        if (!db.init()) {
            getLogger().warning("Database init failed! Refusing to work.");
            return;
        }
        getCommand("title").setExecutor(new TitleCommand(this));
        getCommand("titles").setExecutor(new TitlesCommand(this));
        getCommand("shine").setExecutor(new ShineCommand(this));
        new PlayerListener(this).enable();
        new ShineListener(this).enable();
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerListName(player);
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setPlayerListName(null);
        }
    }

    public void updatePlayerListName(Player player) {
        Title title = db.getPlayerTitle(player.getUniqueId());
        String titlePrefix = title != null ? title.getPlayerListPrefix() : null;
        String prefix = playerListPrefixes.get(player.getUniqueId());
        String suffix = playerListSuffixes.get(player.getUniqueId());
        if (titlePrefix == null && prefix == null && suffix == null) {
            player.setPlayerListName(null);
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append(prefix);
        } else if (titlePrefix != null) {
            sb.append(format(titlePrefix));
        }
        sb.append(player.getDisplayName());
        if (suffix != null) sb.append(suffix);
        player.setPlayerListName(sb.toString());
    }

    public static String format(String msg, Object... args) {
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        if (args.length > 0) msg = String.format(msg, args);
        return msg;
    }

    public static void send(CommandSender sender, String msg, Object... args) {
        msg = format(msg, args);
        sender.sendMessage(msg);
    }

    public Title getPlayerTitle(UUID uuid) {
        return db.getPlayerTitle(uuid);
    }

    public Title getPlayerTitle(OfflinePlayer player) {
        return db.getPlayerTitle(player.getUniqueId());
    }

    public void setPlayerListSuffix(Player player, String suffix) {
        if (suffix == null) {
            playerListSuffixes.remove(player.getUniqueId());
        } else {
            playerListSuffixes.put(player.getUniqueId(), suffix);
        }
        updatePlayerListName(player);
    }

    public void setPlayerListPrefix(Player player, String prefix) {
        if (prefix == null) {
            playerListPrefixes.remove(player.getUniqueId());
        } else {
            playerListPrefixes.put(player.getUniqueId(), prefix);
        }
        updatePlayerListName(player);
    }
}
