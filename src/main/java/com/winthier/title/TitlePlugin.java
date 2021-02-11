package com.winthier.title;

import com.winthier.title.sql.Database;
import com.winthier.title.sql.TitleInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class TitlePlugin extends JavaPlugin implements Listener {
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
        getCommand("Title").setExecutor(new TitleCommand(this));
        getCommand("Titles").setExecutor(new TitlesCommand(this));
        Bukkit.getPluginManager().registerEvents(this, this);
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

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        updatePlayerListName(event.getPlayer());
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
        List<Title> titles = db.listTitles(uuid);
        String titleName = db.getPlayerTitleName(uuid);
        if (titleName != null) {
            for (Title title: titles) {
                if (title.getName().equals(titleName)) {
                    return title;
                }
            }
        }
        if (titles.isEmpty()) return new TitleInfo("?", "?", "?");
        Collections.sort(titles);
        return titles.get(0);
    }

    public Title getPlayerTitle(OfflinePlayer player) {
        return getPlayerTitle(player.getUniqueId());
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
