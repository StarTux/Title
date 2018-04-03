package com.winthier.title.sql;

import com.winthier.sql.SQLDatabase;
import com.winthier.title.Title;
import com.winthier.title.TitlePlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;

public class Database {
    public final TitlePlugin plugin;
    private SQLDatabase db;

    public Database(TitlePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        db = new SQLDatabase(plugin);
        db.registerTables(PlayerInfo.class,
                          TitleInfo.class,
                          UnlockedInfo.class);
        return db.createAllTables();
    }

    public List<Title> listTitles() {
        List<Title> result = new ArrayList<>();
        for (TitleInfo title : db.find(TitleInfo.class).findList()) {
            result.add(title.toTitle());
        }
        return result;
    }

    public List<Title> listTitles(UUID player) {
        List<Title> result = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (UnlockedInfo unlocked : db.find(UnlockedInfo.class).where().eq("player", player).findList()) {
            names.add(unlocked.getTitle());
        }
        Player online = plugin.getServer().getPlayer(player);
        for (TitleInfo title : db.find(TitleInfo.class).findList()) {
            if (names.contains(title.getName())) {
                result.add(title.toTitle());
            } else {
                final String permission = ("title.unlock." + title.getName()).toLowerCase();
                if (online != null) {
                    if (online.isPermissionSet(permission) && online.hasPermission(permission)) {
                        result.add(title.toTitle());
                    }
                } else if (plugin.getVault() != null) {
                    if (plugin.getVault().hasPermission(player, permission)) {
                        result.add(title.toTitle());
                    }
                }
            }
        }
        return result;
    }

    public List<UUID> listPlayers(Title title) {
        List<UUID> result = new ArrayList<>();
        for (UnlockedInfo unlocked: db.find(UnlockedInfo.class).where().eq("title", title.getName()).findList()) {
            result.add(unlocked.player);
        }
        return result;
    }

    public void setTitle(final String name, final String title) {
        TitleInfo info = db.find(TitleInfo.class).where().eq("name", name).findUnique();
        if (info == null) {
            info = new TitleInfo();
            info.setName(name);
        }
        info.setTitle(title);
        db.save(info);
    }

    public boolean setDescription(final String name, final String description) {
        TitleInfo info = db.find(TitleInfo.class).where().eq("name", name).findUnique();
        if (info == null) return false;
        info.setDescription(description);
        db.save(info);
        return true;
    }

    public Title getTitle(final String name) {
        TitleInfo info = db.find(TitleInfo.class).where().eq("name", name).findUnique();
        if (info == null || !info.getName().equals(name)) return null;
        return new Title(info.getName(), info.getTitle(), info.getDescription());
    }

    public Title getTitleByFormat(final String format) {
        TitleInfo info = db.find(TitleInfo.class).where().eq("title", format).findUnique();
        if (info == null) return null;
        return new Title(info.getName(), info.getTitle(), info.getDescription());
    }

    public void unlockTitle(UUID uuid, String name) {
        if (db.find(UnlockedInfo.class).where().eq("player", uuid).eq("title", name).findUnique() != null) return;
        UnlockedInfo info = new UnlockedInfo();
        info.setPlayer(uuid);
        info.setTitle(name);
        db.save(info);
    }

    /**
     * @return true if something was deleted, false if not.
     */
    public boolean lockTitle(UUID uuid, String name) {
        UnlockedInfo info = db.find(UnlockedInfo.class).where().eq("player", uuid).eq("title", name).findUnique();
        if (info == null) return false;
        db.delete(info);
        return true;
    }

    public void setPlayerTitle(UUID uuid, String name) {
        PlayerInfo info = db.find(PlayerInfo.class).where().eq("uuid", uuid).findUnique();
        if (info == null) {
            info = new PlayerInfo();
            info.setUuid(uuid);
        }
        info.setTitle(name);
        db.save(info);
    }

    public String getPlayerTitle(UUID uuid) {
        PlayerInfo info = db.find(PlayerInfo.class).where().eq("uuid", uuid).findUnique();
        if (info == null) return null;
        return info.getTitle();
    }

    public boolean playerHasTitle(UUID uuid, String name) {
        for (Title title: listTitles(uuid)) {
            if (title.getName().equals(name)) return true;
        }
        return false;
    }
}
