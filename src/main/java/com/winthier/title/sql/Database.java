package com.winthier.title.sql;

import com.winthier.title.Title;
import com.winthier.title.TitlePlugin;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.OfflinePlayer;

public class Database {
    public final TitlePlugin plugin;

    public Database(TitlePlugin plugin) {
        this.plugin = plugin;
    }

    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(PlayerInfo.class);
        list.add(TitleInfo.class);
        list.add(UnlockedInfo.class);
        return list;
    }

    public Map<String, String> listTitles() {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (TitleInfo title : plugin.getDatabase().find(TitleInfo.class).findList()) {
            result.put(title.getName(), title.getTitle());
        }
        return result;
    }

    public Map<String, String> listTitles(OfflinePlayer player) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        List<String> names = new ArrayList<String>();
        for (UnlockedInfo unlocked : plugin.getDatabase().find(UnlockedInfo.class).where().eq("player", player.getUniqueId()).findList()) {
            names.add(unlocked.getTitle());
        }
        for (TitleInfo title : plugin.getDatabase().find(TitleInfo.class).where().in("name", names).findList()) {
            result.put(title.getName(), title.getTitle());
        }
        return result;
    }

    public void setTitle(final String name, final String title) {
        TitleInfo info = plugin.getDatabase().find(TitleInfo.class).where().eq("name", name).findUnique();
        if (info == null) {
            info = new TitleInfo();
            info.setName(name);
        }
        info.setTitle(title);
        plugin.getDatabase().save(info);
    }

    public boolean setDescription(final String name, final String description) {
        TitleInfo info = plugin.getDatabase().find(TitleInfo.class).where().eq("name", name).findUnique();
        if (info == null) return false;
        info.setDescription(description);
        plugin.getDatabase().save(info);
        return true;
    }

    public Title getTitle(final String name) {
        TitleInfo info = plugin.getDatabase().find(TitleInfo.class).where().eq("name", name).findUnique();
        if (info == null) return null;
        return new Title(info.getName(), info.getTitle(), info.getDescription());
    }

    public Title getTitleByFormat(final String format) {
        TitleInfo info = plugin.getDatabase().find(TitleInfo.class).where().eq("title", format).findUnique();
        if (info == null) return null;
        return new Title(info.getName(), info.getTitle(), info.getDescription());
    }

    public void unlockTitle(OfflinePlayer player, String name) {
        if (playerHasTitle(player, name)) return;
        UnlockedInfo info = new UnlockedInfo();
        info.setPlayer(player.getUniqueId());
        info.setTitle(name);
        plugin.getDatabase().save(info);
    }

    /**
     * @return true if something was deleted, false if not.
     */
    public boolean lockTitle(OfflinePlayer player, String name) {
        UnlockedInfo info = plugin.getDatabase().find(UnlockedInfo.class).where().eq("player", player.getUniqueId()).eq("title", name).findUnique();
        if (info == null) return false;
        plugin.getDatabase().delete(info);
        return true;
    }

    public void setPlayerTitle(OfflinePlayer player, String name) {
        PlayerInfo info = plugin.getDatabase().find(PlayerInfo.class).where().eq("uuid", player.getUniqueId()).findUnique();
        if (info == null) {
            info = new PlayerInfo();
            info.setUuid(player.getUniqueId());
        }
        info.setTitle(name);
        plugin.getDatabase().save(info);
    }

    public String getPlayerTitle(UUID uuid) {
        PlayerInfo info = plugin.getDatabase().find(PlayerInfo.class).where().eq("uuid", uuid).findUnique();
        if (info == null) return null;
        return info.getTitle();
    }

    public String getPlayerTitle(OfflinePlayer player) {
        return getPlayerTitle(player.getUniqueId());
    }

    public boolean playerHasTitle(OfflinePlayer player, String name) {
        int count = plugin.getDatabase().find(UnlockedInfo.class).where().eq("player", player.getUniqueId()).eq("title", name).findRowCount();
        if (count == 0) return false;
        return true;
    }
}
