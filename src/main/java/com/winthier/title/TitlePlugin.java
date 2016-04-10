package com.winthier.title;

import com.winthier.title.command.*;
import com.winthier.title.listener.*;
import com.winthier.title.sql.Database;
import java.util.List;
import javax.persistence.PersistenceException;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class TitlePlugin extends JavaPlugin {
    public Chat chat;
    public final Database database = new Database(this);
    private final PlayerListener playerListener = new PlayerListener(this);

    @Override
    public void onEnable() {
        if (!setupChat()) {
            throw new RuntimeException("Could not initialize Vault chat.");
        }
        try {
            for (Class<?> clazz : database.getDatabaseClasses()) {
                getDatabase().find(clazz).findRowCount();
            }
        } catch (PersistenceException ex) {
            getLogger().info("Installing database due to first time usage");
            try {
                installDDL();
            } catch (PersistenceException pe) {
                getLogger().warning("Error installing database. Disabling plugin");
                pe.printStackTrace();
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
        getCommand("Title").setExecutor(new TitleCommand(this));
        getCommand("Titles").setExecutor(new TitlesCommand(this));
        playerListener.enable();
    }

    @Override
    public void onDisable() {
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            this.chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        return new Database(this).getDatabaseClasses();
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

    public void updatePlayer(Player player) {
        String name = database.getPlayerTitle(player);
        if (name == null) {
            if (!player.hasPermission("title.custom")) {
                chat.setPlayerPrefix(player, null);
            }
            return;
        }
        if (!database.playerHasTitle(player, name)) {
            getLogger().info(player.getName() + " had locked title " + name + ".");
            database.setPlayerTitle(player, null);
            chat.setPlayerPrefix(player, null);
            return;
        }
        String title = database.getTitle(name);
        if (title == null) {
            getLogger().info(player.getName() + " had unknown title " + name + ".");
            database.setPlayerTitle(player, null);
            chat.setPlayerPrefix(player, null);
            return;
        }
        chat.setPlayerPrefix(player, ChatColor.translateAlternateColorCodes('&', title));
    }
}
