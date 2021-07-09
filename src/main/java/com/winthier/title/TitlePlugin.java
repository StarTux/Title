package com.winthier.title;

import com.winthier.title.sql.Database;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@Getter
public final class TitlePlugin extends JavaPlugin {
    private final Database db = new Database(this);
    @Getter static TitlePlugin instance;
    private Map<UUID, Session> sessions = new HashMap<>();

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
            enter(player);
            updatePlayerName(player);
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            exit(player);
        }
    }

    public void updatePlayerName(Player player) {
        Title title = db.getPlayerTitle(player.getUniqueId());
        Session session = sessions.get(player.getUniqueId());
        if (session == null) session = new Session();
        Component prefix = session.playerListPrefix;
        Component suffix = session.playerListSuffix;
        Shine shine = title.parseShine();
        TextFormat nameColor = title.getNameTextFormat();
        if (prefix == null && suffix == null && nameColor == null && !title.isPrefix()) {
            player.displayName(null);
            player.playerListName(null);
            resetPlayerScoreboards(player);
            return;
        }
        TextComponent.Builder cb = Component.text();
        if (prefix != null) {
            cb.append(prefix);
        }
        Component displayName;
        session.teamPrefix = Component.empty();
        session.teamSuffix = Component.empty();
        if (nameColor == null && !title.isPrefix()) {
            displayName = Component.text(player.getName());
            player.displayName(null);
        } else {
            TextComponent.Builder cb2 = Component.text();
            if (title.isPrefix()) {
                Component titleTag = title.getTitleTag();
                cb2.append(titleTag);
                session.teamPrefix = titleTag;
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
        updatePlayerScoreboards(player, session);
    }

    private static void updatePlayerScoreboards(Player owner, Session session) {
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        // updatePlayerScoreboard(owner, session, main);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Objects.equals(player, owner)) continue;
            Scoreboard scoreboard = player.getScoreboard();
            if (Objects.equals(main, scoreboard)) continue;
            updatePlayerScoreboard(owner, session, scoreboard);
        }
    }

    private static void updatePlayerScoreboard(Player owner, Session session, Scoreboard scoreboard) {
        String teamName = owner.getName().toLowerCase();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) team = scoreboard.registerNewTeam(teamName);
        team.addEntry(owner.getName());
        team.prefix(session.teamPrefix);
        team.suffix(session.teamSuffix);
        team.color(session.teamColor);
    }

    protected void resetPlayerScoreboards(Player owner) {
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        // resetPlayerScoreboard(owner, main);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Objects.equals(player, owner)) continue;
            Scoreboard scoreboard = player.getScoreboard();
            if (Objects.equals(main, scoreboard)) continue;
            resetPlayerScoreboard(owner, scoreboard);
        }
    }

    private void resetPlayerScoreboard(Player owner, Scoreboard scoreboard) {
        Team team = scoreboard.getTeam(owner.getName().toLowerCase());
        if (team != null) team.unregister();
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
        Session session = sessions.get(player.getUniqueId());
        if (session == null) return;
        session.playerListSuffix = suffix;
        updatePlayerName(player);
    }

    public void setPlayerListPrefix(Player player, Component prefix) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null) return;
        session.playerListPrefix = prefix;
        updatePlayerName(player);
    }

    protected void enter(Player player) {
        sessions.put(player.getUniqueId(), new Session());
        Scoreboard scoreboard = player.getScoreboard();
        if (!Objects.equals(scoreboard, Bukkit.getScoreboardManager().getMainScoreboard())) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (Objects.equals(online, player)) continue;
                Session session2 = findSession(online);
                if (session2 != null) {
                    updatePlayerScoreboard(online, session2, scoreboard);
                }
            }
        }
    }

    protected void exit(Player player) {
        player.playerListName(null);
        sessions.remove(player.getUniqueId());
    }

    public Session findSession(Player player) {
        return sessions.get(player.getUniqueId());
    }
}
