package com.winthier.title;

import com.cavetale.core.perm.Perm;
import com.cavetale.core.playercache.PlayerCache;
import com.cavetale.mytems.item.font.Glyph;
import com.winthier.sql.SQLDatabase;
import com.winthier.title.sql.Database;
import com.winthier.title.sql.PlayerInfo;
import com.winthier.title.sql.SQLPlayerSuffix;
import com.winthier.title.sql.SQLSuffix;
import com.winthier.title.sql.UnlockedInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * Plugin class.
 * This class provides several methods which will work on both online
 * and offline players, provided an UUID is required as the
 * argument. Thus it bridges Database and Session methods.
 */
@Getter
public final class TitlePlugin extends JavaPlugin {
    @Getter protected static TitlePlugin instance;
    private SQLDatabase db;
    // Cache
    private Map<UUID, Session> sessions = new HashMap<>();
    private final List<Title> titles = new ArrayList<>();
    private final Map<String, SQLSuffix> suffixes = new HashMap<>();
    private final Map<String, List<SQLSuffix>> suffixCategories = new HashMap<>();
    private final Map<String, Title> nameTitleMap = new HashMap<>();
    protected boolean shinesDisabled = false;

    @Override
    public void onEnable() {
        instance = this;
        db = new SQLDatabase(this);
        db.registerTables(List.of(PlayerInfo.class,
                                  Title.class,
                                  UnlockedInfo.class,
                                  SQLSuffix.class,
                                  SQLPlayerSuffix.class));
        if (!db.createAllTables()) {
            throw new IllegalStateException("Database init failed!");
        }
        getCommand("title").setExecutor(new TitleCommand(this));
        new TitlesCommand(this).enable();
        getCommand("shine").setExecutor(new ShineCommand(this));
        getCommand("badge").setExecutor(new BadgeCommand(this));
        getCommand("gradient").setExecutor(new GradientCommand(this));
        new PlayerListener(this).enable();
        new ShineListener(this).enable();
        titles.addAll(Database.listTitles());
        for (Title title : titles) {
            if (title.parseCategory() == TitleCategory.UNKNOWN) {
                getLogger().warning("Title without category: " + title.getName()
                                    + ", " + title.getCategory());
            }
            nameTitleMap.put(title.getName(), title);
        }
        Collections.sort(titles);
        loadSuffixesAsync();
        // Update title list every 10 seconds
        Bukkit.getScheduler().runTaskTimer(this, () -> {
                db.find(Title.class).findListAsync(list -> {
                        titles.clear();
                        nameTitleMap.clear();
                        titles.addAll(list);
                        for (Title title : titles) nameTitleMap.put(title.getName(), title);
                        Collections.sort(titles);
                    });
                loadSuffixesAsync();
            }, 200L, 200L);
        for (Player player : Bukkit.getOnlinePlayers()) {
            enter(player);
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            exit(player);
        }
    }

    protected void reloadAllData() {
        titles.clear();
        nameTitleMap.clear();
        titles.addAll(Database.listTitles());
        for (Title title : titles) nameTitleMap.put(title.getName(), title);
        Collections.sort(titles);
        for (Player player : Bukkit.getOnlinePlayers()) {
            enter(player);
        }
        loadSuffixesAsync();
    }

    private void loadSuffixesAsync() {
        db.find(SQLSuffix.class).findListAsync(list -> {
                suffixes.clear();
                suffixCategories.clear();
                for (SQLSuffix suffix : list) {
                    suffix.unpack();
                    suffixes.put(suffix.getName(), suffix);
                    if (suffix.getCategory() != null) {
                        suffixCategories.computeIfAbsent(suffix.getCategory(), n -> new ArrayList<>())
                            .add(suffix);
                    }
                }
            });
    }

    private static Component tierForPlayerList(Player player) {
        return join(noSeparators(), text("["), Glyph.toComponent("" + Perm.get().getLevel(player.getUniqueId())), text("]"))
            .color(GRAY)
            .decoration(TextDecoration.ITALIC, false);
    }

    public void updatePlayerName(Player player) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null) return;
        Title title = session.getTitle(player);
        TextFormat nameColor = title.getNameTextFormat();
        SQLSuffix suffix = session.getSuffix(player);
        session.teamPrefix = Component.empty();
        session.teamSuffix = Component.empty();
        if (session.playerListPrefix == null && session.playerListSuffix == null && session.color == null
            && nameColor == null && !title.isPrefix() && suffix == null) {
            player.displayName(null);
            player.playerListName(join(noSeparators(), tierForPlayerList(player), text(player.getName())));
            resetPlayerScoreboards(player);
            return;
        }
        TextComponent.Builder playerListBuilder = Component.text();
        if (session.playerListPrefix != null) {
            playerListBuilder.append(session.playerListPrefix);
        } else if (!title.isPrefix()) {
            playerListBuilder.append(tierForPlayerList(player));
        }
        Component displayName;
        if (nameColor == null && !title.isPrefix() && suffix == null) {
            displayName = Component.text(player.getName());
            player.displayName(null);
        } else {
            TextComponent.Builder displayNameBuilder = Component.text();
            if (title.isPrefix()) {
                Component titleTag = title.getTitleTag(player.getUniqueId());
                displayNameBuilder.append(titleTag);
                session.teamPrefix = titleTag;
            }
            String playerName = suffix != null && suffix.isPartOfName()
                ? player.getName() + suffix.getCharacter()
                : player.getName();
            if (nameColor instanceof TextColor) {
                displayNameBuilder.append(Component.text(playerName, (TextColor) nameColor));
            } else if (nameColor instanceof TextEffect) {
                TextEffect textEffect = (TextEffect) nameColor;
                displayNameBuilder.append(textEffect.format(playerName));
            } else {
                displayNameBuilder.append(Component.text(playerName));
            }
            if (suffix != null && !suffix.isPartOfName()) {
                displayNameBuilder.append(suffix.getComponent());
            }
            displayName = displayNameBuilder.build();
            player.displayName(displayName);
        }
        playerListBuilder.append(Component.text().append(displayName).decoration(TextDecoration.ITALIC, false));
        if (session.playerListSuffix != null) {
            playerListBuilder.append(session.playerListSuffix);
        }
        if (suffix != null || session.playerListSuffix != null) {
            TextComponent.Builder teamSuffixBuilder = Component.text();
            if (suffix != null) teamSuffixBuilder.append(suffix.getComponent());
            if (session.playerListSuffix != null) teamSuffixBuilder.append(session.playerListSuffix);
            session.teamSuffix = teamSuffixBuilder.build();
        }
        player.playerListName(playerListBuilder.build());
        updatePlayerScoreboards(player, session);
    }

    public void updatePlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) updatePlayerName(player);
    }

    private static void updatePlayerScoreboards(Player owner, Session session) {
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        // updatePlayerScoreboard(owner, session, main);
        for (Player player : Bukkit.getOnlinePlayers()) {
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
        try {
            team.color(session.color);
        } catch (NullPointerException npe) { }
    }

    protected void resetPlayerScoreboards(Player owner) {
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        // resetPlayerScoreboard(owner, main);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = player.getScoreboard();
            if (Objects.equals(main, scoreboard)) continue;
            resetPlayerScoreboard(owner, scoreboard);
        }
    }

    private void resetPlayerScoreboard(Player owner, Scoreboard scoreboard) {
        Team team = scoreboard.getTeam(owner.getName().toLowerCase());
        if (team != null) team.unregister();
    }

    public void setPlayerListSuffix(Player player, Component suffix) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null) return;
        if (Objects.equals(session.playerListSuffix, suffix)) return;
        session.playerListSuffix = suffix;
        Bukkit.getScheduler().runTask(this, () -> updatePlayerName(player));
    }

    public void setPlayerListPrefix(Player player, Component prefix) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null) return;
        if (Objects.equals(session.playerListPrefix, prefix)) return;
        session.playerListPrefix = prefix;
        Bukkit.getScheduler().runTask(this, () -> updatePlayerName(player));
    }

    public void setColor(Player player, NamedTextColor color) {
        Session session = sessions.get(player.getUniqueId());
        if (session == null) return;
        if (Objects.equals(session.color, color)) return;
        session.color = color;
        Bukkit.getScheduler().runTask(this, () -> updatePlayerName(player));
    }

    protected void enter(final Player player) {
        UUID uuid = player.getUniqueId();
        db.scheduleAsyncTask(() -> {
                db.insertIgnore(new PlayerInfo(uuid));
                PlayerInfo playerRow = db.find(PlayerInfo.class).eq("uuid", uuid).findUnique();
                if (playerRow == null) {
                    throw new IllegalStateException(player.getName() + ": playerRow=null");
                }
                List<UnlockedInfo> unlockedRows = db.find(UnlockedInfo.class).eq("player", uuid).findList();
                List<SQLPlayerSuffix> suffixRows = db.find(SQLPlayerSuffix.class).eq("player", uuid).findList();
                Bukkit.getScheduler().runTask(this, () -> {
                        if (!player.isOnline()) return;
                        sessions.put(uuid, new Session(this, uuid, player.getName(), playerRow, unlockedRows, suffixRows));
                        updatePlayerName(player);
                    });
            });
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

    public Session sessionOf(PlayerCache player) {
        Session existingSession = sessions.get(player.uuid);
        if (existingSession != null) return existingSession;
        PlayerInfo playerRow = db.find(PlayerInfo.class).eq("uuid", player.uuid).findUnique();
        if (playerRow == null) playerRow = new PlayerInfo(player.uuid);
        List<UnlockedInfo> unlockedRows = db.find(UnlockedInfo.class).eq("player", player.uuid).findList();
        List<SQLPlayerSuffix> suffixRows = db.find(SQLPlayerSuffix.class).eq("player", player.uuid).findList();
        return new Session(this, player.uuid, player.name, playerRow, unlockedRows, suffixRows);
    }

    protected boolean addTitle(Title title) {
        if (0 == db.insertIgnore(title)) return false;
        titles.add(title);
        Collections.sort(titles);
        nameTitleMap.put(title.getName(), title);
        return true;
    }

    protected boolean deleteTitle(Title title) {
        if (-1 == Database.deleteTitle(title)) return false;
        titles.remove(title);
        nameTitleMap.remove(title.getName());
        for (Session session : sessions.values()) {
            session.lockTitle(title);
        }
        return true;
    }

    protected void updateTitleList() {
        Collections.sort(titles);
    }

    public List<Title> getPlayerTitles(Player player) {
        Session session = findSession(player);
        if (session == null) return Collections.emptyList();
        return session.getTitles(player);
    }

    public Title getPlayerTitle(Player player) {
        Session session = findSession(player);
        if (session == null) return null;
        return session.getTitle(player);
    }

    public Title getTitle(String name) {
        return nameTitleMap.get(name);
    }

    public Shine getPlayerShine(Player player) {
        Session session = findSession(player);
        if (session == null) return null;
        return session.getShine(player);
    }

    public Collection<Shine> getPlayerShines(Player player) {
        Session session = findSession(player);
        if (session == null) return Collections.emptyList();
        return session.getShines(player);
    }

    public boolean playerHasShine(Player player, Shine shine) {
        Session session = findSession(player);
        if (session == null) return false;
        return session.hasShine(shine);
    }

    public List<Title> unlocks2Titles(List<UnlockedInfo> rows) {
        List<Title> result = new ArrayList<>(rows.size());
        for (UnlockedInfo row : rows) {
            Title title = getTitle(row.getTitle());
            if (title != null) {
                result.add(title);
            } else {
                getLogger().warning("convertUnlocks: Title not found: " + row);
                continue;
            }
        }
        return result;
    }

    /**
     * List player titles.
     * If the player is online, defer to their session.
     * Otherwise list unlocks, without permission unlocks.
     */
    public List<Title> getPlayerTitles(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null
            ? getPlayerTitles(player)
            : unlocks2Titles(Database.listUnlocks(uuid));
    }

    /**
     * Return the selected title of the player.
     * If the player is online, defer to their session.
     * Otherwise, disregard whether they actually have the title
     * unlocked.
     * May return null.
     */
    public Title getPlayerTitle(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) return getPlayerTitle(player);
        PlayerInfo info = Database.getPlayerInfo(uuid);
        if (info == null) return null;
        return getTitle(info.getTitle());
    }

    public boolean unlockPlayerTitle(UUID uuid, Title title) {
        Session session = sessions.get(uuid);
        if (session != null) return session.unlockTitle(title);
        return Database.unlockTitle(uuid, title);
    }

    public boolean lockPlayerTitle(UUID uuid, Title title) {
        Session session = sessions.get(uuid);
        if (session != null) return session.lockTitle(title);
        return Database.lockTitle(uuid, title);
    }

    public boolean playerHasTitle(UUID uuid, Title title) {
        Session session = sessions.get(uuid);
        if (session != null) return session.hasTitle(title);
        return Database.playerHasTitle(uuid, title);
    }

    /**
     * Set the player title. If the player is offline, this will not
     * check if the player has the title unlocked!
     */
    public boolean setPlayerTitle(UUID uuid, @NonNull Title title) {
        Session session = sessions.get(uuid);
        if (session != null) return session.setTitle(title);
        return 0 != db.update(PlayerInfo.class)
            .where(w -> w.eq("uuid", uuid))
            .set("title", title.getName())
            .sync();
    }

    public boolean resetPlayerTitle(UUID uuid) {
        Session session = sessions.get(uuid);
        if (session != null) return session.resetTitle();
        return 0 != db.update(PlayerInfo.class)
            .where(w -> w.eq("uuid", uuid))
            .set("title", null)
            .sync();
    }

    public SQLSuffix getPlayerSuffix(Player player) {
        Session session = sessions.get(player.getUniqueId());
        return session != null
            ? session.getSuffix(player)
            : null;
    }

    public List<SQLSuffix> getPlayerSuffixes(UUID uuid) {
        Session session = sessions.get(uuid);
        return session != null
            ? session.getSuffixes()
            : Collections.emptyList();
    }

    public boolean unlockPlayerSuffix(UUID uuid, String suffixName) {
        Session session = sessions.get(uuid);
        return session != null
            ? session.unlockSuffix(suffixName)
            : 0 != db.insert(new SQLPlayerSuffix(uuid, suffixName));
    }

    public boolean lockPlayerSuffix(UUID uuid, String suffixName) {
        Session session = sessions.get(uuid);
        return session != null
            ? session.lockSuffix(suffixName)
            : 0 != db.find(SQLPlayerSuffix.class).eq("player", uuid).delete();
    }
}
