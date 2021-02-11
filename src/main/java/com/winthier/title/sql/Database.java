package com.winthier.title.sql;

import com.winthier.sql.SQLDatabase;
import com.winthier.title.Title;
import com.winthier.title.TitlePlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;

public final class Database {
    public final TitlePlugin plugin;
    private SQLDatabase db;

    public Database(final TitlePlugin plugin) {
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
        List<? extends Title> list = db.find(TitleInfo.class).findList();
        return (List<Title>) list;
    }

    public List<Title> listTitles(UUID player) {
        List<Title> result = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (UnlockedInfo unlocked : db.find(UnlockedInfo.class).where().eq("player", player).findList()) {
            names.add(unlocked.getTitle());
        }
        Player online = plugin.getServer().getPlayer(player);
        for (Title title : listTitles()) {
            if (names.contains(title.getName())) {
                result.add(title);
            } else {
                if (online != null) {
                    final String permission = "title.unlock." + title.getName().toLowerCase();
                    if (online.isPermissionSet(permission) && online.hasPermission(permission)) {
                        result.add(title);
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

    public Title getTitle(String name) {
        return db.find(TitleInfo.class).where().eq("name", name).findUnique();
    }

    public boolean unlockTitle(UUID uuid, String name) {
        if (db.find(UnlockedInfo.class).where().eq("player", uuid).eq("title", name).findUnique() != null) return false;
        UnlockedInfo info = new UnlockedInfo();
        info.setPlayer(uuid);
        info.setTitle(name);
        db.save(info);
        return true;
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

    public PlayerInfo getPlayerInfo(UUID uuid) {
        return db.find(PlayerInfo.class).where().eq("uuid", uuid).findUnique();
    }

    public String getPlayerTitleName(UUID uuid) {
        PlayerInfo playerInfo = getPlayerInfo(uuid);
        return playerInfo != null ? playerInfo.getTitle() : null;
    }

    public Title getPlayerTitle(UUID uuid) {
        String name = getPlayerTitleName(uuid);
        return name != null ? getTitle(name) : null;
    }

    public boolean playerHasTitle(UUID uuid, String name) {
        for (Title title: listTitles(uuid)) {
            if (title.getName().equals(name)) return true;
        }
        return false;
    }
}
