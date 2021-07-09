package com.winthier.title.sql;

import com.winthier.sql.SQLDatabase;
import com.winthier.title.Title;
import com.winthier.title.TitlePlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Database {
    public final TitlePlugin plugin;
    private SQLDatabase db;
    private List<Title> titlesCache = new ArrayList<>();
    private Map<UUID, Title> playerTitleCache = new HashMap<>();
    private Map<UUID, List<Title>> playerUnlockedCache = new HashMap<>();

    public Database(final TitlePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        db = new SQLDatabase(plugin);
        db.registerTables(PlayerInfo.class,
                          Title.class,
                          UnlockedInfo.class);
        return db.createAllTables();
    }

    public List<Title> listTitles() {
        List<Title> list = db.find(Title.class).findList();
        Collections.sort(list);
        titlesCache = (List<Title>) list;
        return (List<Title>) list;
    }

    public List<Title> listTitles(UUID player) {
        List<Title> result = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (UnlockedInfo unlocked : db.find(UnlockedInfo.class).where().eq("player", player).findList()) {
            names.add(unlocked.getTitle());
        }
        Player online = Bukkit.getPlayer(player);
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
        Collections.sort(result);
        playerUnlockedCache.put(player, result);
        return result;
    }

    public List<UUID> listPlayers(Title title) {
        List<UUID> result = new ArrayList<>();
        for (UnlockedInfo unlocked: db.find(UnlockedInfo.class).where().eq("title", title.getName()).findList()) {
            result.add(unlocked.getPlayer());
        }
        return result;
    }

    public void setTitle(final String name, final String title) {
        Title info = db.find(Title.class).where().eq("name", name).findUnique();
        if (info == null) {
            info = new Title();
            info.setName(name);
            info.setTitle(title);
            db.insert(info);
        } else {
            info.setTitle(title);
            db.update(info, "title");
        }
    }

    public boolean setDescription(final String name, final String description) {
        Title info = db.find(Title.class).where().eq("name", name).findUnique();
        if (info == null) return false;
        info.setDescription(description);
        db.update(info, "description");
        return true;
    }

    public Title getTitle(String name) {
        return db.find(Title.class).where().eq("name", name).findUnique();
    }

    public boolean unlockTitle(UUID uuid, Title title) {
        UnlockedInfo info = new UnlockedInfo(uuid, title.getName());
        return 0 != db.insertIgnore(info);
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

    public void setPlayerTitle(UUID uuid, Title title) {
        PlayerInfo info = db.find(PlayerInfo.class).where().eq("uuid", uuid).findUnique();
        if (info == null) {
            info = new PlayerInfo();
            info.setUuid(uuid);
            info.setTitle(title != null ? title.getName() : null);
            db.insert(info);
        } else {
            info.setTitle(title != null ? title.getName() : null);
            db.update(info, "title");
        }
        if (title != null) {
            playerTitleCache.put(uuid, title);
        } else {
            playerTitleCache.remove(uuid);
        }
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
        Title title = name != null ? getTitle(name) : null;
        if (title == null) {
            List<Title> titles = listTitles(uuid);
            title = !titles.isEmpty()
                ? titles.get(0)
                : new Title("?", "?", "?");
        }
        playerTitleCache.put(uuid, title);
        return title;
    }

    public boolean playerHasTitle(UUID uuid, Title title) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            final String permission = "title.unlock." + title.getName().toLowerCase();
            if (online.isPermissionSet(permission) && online.hasPermission(permission)) {
                return true;
            }
        }
        return db.find(UnlockedInfo.class)
            .eq("player", uuid)
            .eq("title", title.getName())
            .findUnique()
            != null;
    }

    public int deleteTitle(Title title) {
        int count = db.delete(title);
        if (count == 0) return -1;
        int unlocks = db.find(UnlockedInfo.class)
            .eq("title", title.getName())
            .delete();
        int players = db.update(PlayerInfo.class)
            .set("title", null)
            .where(w -> w.eq("title", title.getName()))
            .sync();
        plugin.getLogger().info("Deleted title " + title.getName()
                                + ", unlocks=" + unlocks
                                + ", players=" + players);
        return unlocks;
    }

    public void clearCache(UUID uuid) {
        playerTitleCache.remove(uuid);
        playerUnlockedCache.remove(uuid);
    }

    public Title getCachedTitle(UUID uuid) {
        return playerTitleCache.get(uuid);
    }

    public boolean save(Title title) {
        return 1 == db.update(title);
    }
}
