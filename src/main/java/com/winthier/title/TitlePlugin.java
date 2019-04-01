package com.winthier.title;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import com.winthier.title.sql.Database;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public final class TitlePlugin extends PluginBase {
    private final Database db = new Database(this);
    @Getter static TitlePlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        if (!db.init()) {
            getLogger().warning("Database init failed! Refusing to work.");
            return;
        }
        ((PluginCommand)getCommand("title")).setExecutor(new TitleCommand(this));
        ((PluginCommand)getCommand("titles")).setExecutor(new TitlesCommand(this));
    }

    public static String format(String msg, Object... args) {
        msg = TextFormat.colorize(msg);
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
}
