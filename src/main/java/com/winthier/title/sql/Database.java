package com.winthier.title.sql;

import com.winthier.sql.SQLDatabase;
import com.winthier.title.Title;
import com.winthier.title.TitleCategory;
import com.winthier.title.TitlePlugin;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Utility class for database operations.
 */
public final class Database {
    private Database() { }

    private static TitlePlugin plugin() {
        return TitlePlugin.getInstance();
    }

    private static SQLDatabase db() {
        return TitlePlugin.getInstance().getDb();
    }

    public static List<Title> listTitles() {
        List<Title> list = db().find(Title.class).findList();
        Collections.sort(list);
        return (List<Title>) list;
    }

    public static List<UnlockedInfo> listUnlocks(UUID player) {
        return db().find(UnlockedInfo.class).where().eq("player", player).findList();
    }

    public static List<UnlockedInfo> listPlayers(Title title) {
        return db().find(UnlockedInfo.class).where().eq("title", title.getName()).findList();
    }

    public static boolean unlockTitle(UUID uuid, Title title) {
        UnlockedInfo info = new UnlockedInfo(uuid, title.getName());
        return 0 != db().insertIgnore(info);
    }

    /**
     * @return true if something was deleted, false if not.
     */
    public static boolean lockTitle(UUID uuid, Title title) {
        return 0 != db().find(UnlockedInfo.class).where().eq("player", uuid).eq("title", title.getName()).delete();
    }

    public static boolean unlockCategory(UUID uuid, TitleCategory category) {
        UnlockedInfo info = new UnlockedInfo(uuid, "#" + category.key);
        return 0 != db().insertIgnore(info);
    }

    public static boolean lockCategory(UUID uuid, TitleCategory category) {
        return 0 != db().find(UnlockedInfo.class).where().eq("player", uuid).eq("title", "#" + category.key).delete();
    }

    public static PlayerInfo getPlayerInfo(UUID uuid) {
        return db().find(PlayerInfo.class).where().eq("uuid", uuid).findUnique();
    }

    public static boolean playerHasTitle(UUID uuid, Title title) {
        Player online = Bukkit.getPlayer(uuid);
        if (online != null) {
            final String permission = "title.unlock." + title.getName().toLowerCase();
            if (online.isPermissionSet(permission) && online.hasPermission(permission)) {
                return true;
            }
        }
        return db().find(UnlockedInfo.class)
            .eq("player", uuid)
            .eq("title", title.getName())
            .findUnique()
            != null;
    }

    public static int deleteTitle(Title title) {
        int count = db().delete(title);
        if (count == 0) return -1;
        int unlocks = db().find(UnlockedInfo.class)
            .eq("title", title.getName())
            .delete();
        int players = db().update(PlayerInfo.class)
            .set("title", null)
            .where(w -> w.eq("title", title.getName()))
            .sync();
        plugin().getLogger().info("Deleted title " + title.getName()
                                + ", unlocks=" + unlocks
                                + ", players=" + players);
        return unlocks;
    }

    public static boolean save(Title title) {
        return 1 == db().update(title);
    }
}
