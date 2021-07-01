package com.winthier.title;

import com.winthier.title.sql.Database;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextFormat;
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
    private Map<UUID, Component> playerListSuffixes = new HashMap<>();
    private Map<UUID, Component> playerListPrefixes = new HashMap<>();
    private MytemsHandler mytemsHandler;

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
        getCommand("gradient").setExecutor(new GradientCommand(this));
        new PlayerListener(this).enable();
        new ShineListener(this).enable();
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerName(player);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Mytems")) {
            getLogger().info("Mytems plugin found");
            mytemsHandler = new MytemsHandler();
        } else {
            getLogger().warning("Mytems plugin not found!");
            mytemsHandler = null;
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setPlayerListName(null);
        }
    }

    public void updatePlayerName(Player player) {
        Title title = db.getPlayerTitle(player.getUniqueId());
        Component prefix = playerListPrefixes.get(player.getUniqueId());
        Component suffix = playerListSuffixes.get(player.getUniqueId());
        Shine shine = title.parseShine();
        TextFormat nameColor = title.getNameTextFormat();
        if (prefix == null && suffix == null && nameColor == null && !title.isPrefix()) {
            player.displayName(null);
            player.setPlayerListName(null);
            return;
        }
        TextComponent.Builder cb = Component.text();
        if (prefix != null) {
            cb.append(prefix);
        }
        Component displayName;
        if (nameColor == null && !title.isPrefix()) {
            displayName = Component.text(player.getName());
            player.displayName(null);
        } else {
            TextComponent.Builder cb2 = Component.text();
            if (title.isPrefix()) {
                cb2.append(title.getTitleTag());
            }
            if (nameColor instanceof TextColor) {
                cb2.append(Component.text(player.getName(), (TextColor) nameColor));
            } else if (nameColor instanceof TextEffect) {
                TextEffect textEffect = (TextEffect) nameColor;
                cb2.append(textEffect.format(player.getName()));
            } else {
                cb2.append(Component.text(player.getName()));
            }
            displayName = cb2.build();
            player.displayName(displayName);
        }
        cb.append(displayName);
        if (suffix != null) {
            cb.append(suffix);
        }
        player.playerListName(cb.build());
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

    public void setPlayerListSuffix(Player player, Component suffix) {
        if (suffix == null) {
            playerListSuffixes.remove(player.getUniqueId());
        } else {
            playerListSuffixes.put(player.getUniqueId(), suffix);
        }
        updatePlayerName(player);
    }

    public void setPlayerListPrefix(Player player, Component prefix) {
        if (prefix == null) {
            playerListPrefixes.remove(player.getUniqueId());
        } else {
            playerListPrefixes.put(player.getUniqueId(), prefix);
        }
        updatePlayerName(player);
    }
}
