package com.winthier.title;

import com.winthier.title.command.TitleCommand;
import com.winthier.title.command.TitlesCommand;
import com.winthier.title.sql.Database;
import java.util.List;
import java.util.UUID;
import javax.persistence.PersistenceException;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class TitlePlugin extends JavaPlugin {
    private final Database db = new Database(this);
    @Getter static TitlePlugin instance;
    private Vault vault;

    @Override
    public void onEnable() {
        instance = this;
        if (!db.init()) {
            getLogger().warning("Database init failed! Refusing to work.");
            return;
        }
        getCommand("Title").setExecutor(new TitleCommand(this));
        getCommand("Titles").setExecutor(new TitlesCommand(this));
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            vault = new Vault(this);
            getLogger().info("Vault detected!");
        } else {
            getLogger().info("Vault NOT detected!");
        }
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

    private Title getDefaultPlayerTitle(UUID uuid) {
        List<Title> titles = db.listTitles(uuid);
        if (titles.isEmpty()) new Title("default", "", "");
        return titles.get(0);
    }

    public Title getPlayerTitle(UUID uuid) {
        String titleName = db.getPlayerTitle(uuid);
        if (titleName == null) return getDefaultPlayerTitle(uuid);
        Title title = db.getTitle(titleName);
        if (title == null) return getDefaultPlayerTitle(uuid);
        return title;
    }

    public Title getPlayerTitle(OfflinePlayer player) {
        return getPlayerTitle(player.getUniqueId());
    }
}
