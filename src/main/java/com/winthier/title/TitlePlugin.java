package com.winthier.title;

import com.winthier.title.sql.Database;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class TitlePlugin extends JavaPlugin {
    private final Database db = new Database(this);
    @Getter static TitlePlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        if (!db.init()) {
            getLogger().warning("Database init failed! Refusing to work.");
            return;
        }
        getCommand("Title").setExecutor(new TitleCommand(this));
        getCommand("Titles").setExecutor(new TitlesCommand(this));
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
        String titleName = db.getPlayerTitle(uuid);
        if (titleName != null) {
            for (Title title: titles) {
                if (title.getName().equals(titleName)) {
                    return title;
                }
            }
        }
        if (titles.isEmpty()) return new Title("?", "?", "?");
        return titles.get(0);
    }

    public Title getPlayerTitle(OfflinePlayer player) {
        return getPlayerTitle(player.getUniqueId());
    }
}
