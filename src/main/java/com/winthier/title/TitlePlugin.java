package com.winthier.title;

import com.cavetale.core.connect.Connect;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

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
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        db = new SQLDatabase(this);
        db.registerTables(List.of(PlayerInfo.class,
                                  Title.class,
                                  UnlockedInfo.class,
                                  SQLSuffix.class,
                                  SQLPlayerSuffix.class));
        if (!db.createAllTables()) {
            throw new IllegalStateException("Database init failed!");
        }
        new TitleCommand(this).enable();
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
        Bukkit.getScheduler().runTaskTimer(this, this::tickTitles, 0L, 1L);
        new MenuListener().enable();
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

    protected static Component tierForPlayerList(UUID uuid) {
        return join(noSeparators(), text("["), Glyph.toComponent("" + Perm.get().getLevel(uuid)), text("]"))
            .color(GRAY)
            .decoration(TextDecoration.ITALIC, false);
    }

    public void updatePlayerName(final UUID uuid) {
        Session session = findSession(uuid);
        if (session != null) session.update(Bukkit.getPlayer(uuid));
    }

    public void updatePlayerName(Player player) {
        Session session = findSession(player);
        if (session != null) session.update(player);
    }

    private void tickTitles() {
        final long now = System.currentTimeMillis();
        final long then = now - 5000L;
        Set<UUID> online = Connect.get().getOnlinePlayers();
        for (UUID uuid : online) findSession(uuid); // update lastUsed
        for (Session session : List.copyOf(sessions.values())) {
            Player player = Bukkit.getPlayer(session.uuid);
            if (player != null || online.contains(session.uuid)) {
                session.lastUsed = now;
            } else if (player == null && session.lastUsed < then && !online.contains(session.uuid)) {
                getLogger().info("Discard session: " + session.playerName);
                sessions.remove(session.uuid);
                continue;
            }
            if (session.animated) {
                session.process(player);
            }
        }
    }

    protected void updatePlayerScoreboards(Player owner, Session session) {
        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        // updatePlayerScoreboard(owner, session, main);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = player.getScoreboard();
            if (Objects.equals(main, scoreboard)) continue;
            updatePlayerScoreboard(owner, session, scoreboard);
        }
    }

    private void updatePlayerScoreboard(Player owner, Session session, Scoreboard scoreboard) {
        String teamName = owner.getName().toLowerCase();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) team = scoreboard.registerNewTeam(teamName);
        team.addEntry(owner.getName());
        // Team prefix and suffix for the player tag
        team.prefix(session.nameTagPrefix);
        team.suffix(session.nameTagSuffix);
        try {
            team.color(session.teamColor);
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
        Session session = findSession(player.getUniqueId());
        if (session == null) return;
        if (Objects.equals(session.playerListSuffix, suffix)) return;
        session.playerListSuffix = suffix;
        Bukkit.getScheduler().runTask(this, () -> updatePlayerName(player));
    }

    public void setPlayerListPrefix(Player player, Component prefix) {
        Session session = findSession(player.getUniqueId());
        if (session == null) return;
        if (Objects.equals(session.playerListPrefix, prefix)) return;
        session.playerListPrefix = prefix;
        Bukkit.getScheduler().runTask(this, () -> updatePlayerName(player));
    }

    public void setColor(Player player, NamedTextColor color) {
        Session session = findSession(player.getUniqueId());
        if (session == null) return;
        if (Objects.equals(session.teamColor, color)) return;
        session.teamColor = color;
        Bukkit.getScheduler().runTask(this, () -> updatePlayerName(player));
    }

    protected void enter(final Player player) {
        enter(player.getUniqueId(), player.getName());
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

    protected void enter(final UUID uuid, final String name) {
        db.scheduleAsyncTask(() -> {
                db.insertIgnore(new PlayerInfo(uuid));
                PlayerInfo playerRow = db.find(PlayerInfo.class).eq("uuid", uuid).findUnique();
                if (playerRow == null) {
                    throw new IllegalStateException(name + ": playerRow=null");
                }
                List<UnlockedInfo> unlockedRows = db.find(UnlockedInfo.class).eq("player", uuid).findList();
                List<SQLPlayerSuffix> suffixRows = db.find(SQLPlayerSuffix.class).eq("player", uuid).findList();
                Bukkit.getScheduler().runTask(this, () -> {
                        sessions.put(uuid, new Session(this, uuid, name, playerRow, unlockedRows, suffixRows));
                        updatePlayerName(uuid);
                    });
            });
    }

    protected void exit(Player player) {
        player.playerListName(null);
    }

    public Session findSession(Player player) {
        return findSession(player.getUniqueId());
    }

    public Session findSession(UUID uuid) {
        Session session = sessions.get(uuid);
        if (session != null) {
            session.lastUsed = System.currentTimeMillis();
        } else {
            enter(uuid, PlayerCache.nameForUuid(uuid));
        }
        return session;
    }

    public Session sessionOf(PlayerCache player) {
        Session existingSession = findSession(player.uuid);
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
        return session.getTitles();
    }

    public Title getPlayerTitle(Player player) {
        Session session = findSession(player);
        if (session == null) return null;
        return session.getTitle();
    }

    public Title getTitle(String name) {
        return nameTitleMap.get(name);
    }

    public Shine getPlayerShine(Player player) {
        Session session = findSession(player);
        if (session == null) return null;
        return session.getShine();
    }

    public Collection<Shine> getPlayerShines(Player player) {
        Session session = findSession(player);
        if (session == null) return Collections.emptyList();
        return session.getShines();
    }

    public boolean playerHasShine(Player player, Shine shine) {
        Session session = findSession(player);
        if (session == null) return false;
        return session.hasShine(shine);
    }

    public List<Title> unlocks2Titles(List<UnlockedInfo> rows) {
        List<Title> result = new ArrayList<>(rows.size());
        Set<TitleCategory> categories = EnumSet.noneOf(TitleCategory.class);
        for (UnlockedInfo row : rows) {
            if (row.getTitle().startsWith("#")) {
                String key = row.getTitle().substring(1);
                TitleCategory cat = TitleCategory.ofKey(key);
                if (cat == null) {
                    getLogger().warning("convertUnlocks: Title category not found: " + row);
                    continue;
                }
                categories.add(cat);
            } else {
                Title title = getTitle(row.getTitle());
                if (title == null) {
                    getLogger().warning("convertUnlocks: Title not found: " + row);
                    continue;
                }
                result.add(title);
            }
        }
        if (!categories.isEmpty()) {
            for (Title title : titles) {
                if (categories.contains(title.parseCategory())) {
                    result.add(title);
                }
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
        Session session = findSession(uuid);
        if (session != null) return session.unlockTitle(title);
        return Database.unlockTitle(uuid, title);
    }

    public boolean lockPlayerTitle(UUID uuid, Title title) {
        Session session = findSession(uuid);
        if (session != null) return session.lockTitle(title);
        boolean result = Database.lockTitle(uuid, title);
        if (result) broadcastUpdate(uuid);
        return result;
    }

    public boolean playerHasTitle(UUID uuid, Title title) {
        Session session = findSession(uuid);
        if (session != null) return session.hasTitle(title);
        boolean result = Database.playerHasTitle(uuid, title);
        if (result) broadcastUpdate(uuid);
        return result;
    }

    /**
     * Set the player title. If the player is offline, this will not
     * check if the player has the title unlocked!
     */
    public boolean setPlayerTitle(UUID uuid, @NonNull Title title) {
        Session session = findSession(uuid);
        if (session != null) return session.setTitle(title);
        boolean result = 0 != db.update(PlayerInfo.class)
            .where(w -> w.eq("uuid", uuid))
            .set("title", title.getName())
            .sync();
        if (result) broadcastUpdate(uuid);
        return result;
    }

    public boolean resetPlayerTitle(UUID uuid) {
        Session session = findSession(uuid);
        if (session != null) return session.resetTitle();
        boolean result = 0 != db.update(PlayerInfo.class)
            .where(w -> w.eq("uuid", uuid))
            .set("title", null)
            .sync();
        if (result) broadcastUpdate(uuid);
        return result;
    }

    public SQLSuffix getPlayerSuffix(Player player) {
        Session session = findSession(player.getUniqueId());
        return session != null
            ? session.getSuffix()
            : null;
    }

    public List<SQLSuffix> getPlayerSuffixes(UUID uuid) {
        Session session = findSession(uuid);
        return session != null
            ? session.getSuffixes()
            : Collections.emptyList();
    }

    public boolean unlockPlayerSuffix(UUID uuid, String suffixName) {
        Session session = findSession(uuid);
        if (session != null) return session.unlockSuffix(suffixName);
        boolean result = 0 != db.insert(new SQLPlayerSuffix(uuid, suffixName));
        if (result) broadcastUpdate(uuid);
        return result;
    }

    public boolean lockPlayerSuffix(UUID uuid, String suffixName) {
        Session session = findSession(uuid);
        if (session != null) return session.lockSuffix(suffixName);
        boolean result = 0 != db.find(SQLPlayerSuffix.class).eq("player", uuid).delete();
        if (result) broadcastUpdate(uuid);
        return result;
    }

    public boolean unlockPlayerCategory(UUID uuid, TitleCategory category) {
        Session session = findSession(uuid);
        if (session != null) return session.unlockCategory(category);
        boolean result = Database.unlockCategory(uuid, category);
        if (result) broadcastUpdate(uuid);
        return result;
    }

    public boolean lockPlayerCategory(UUID uuid, TitleCategory category) {
        Session session = findSession(uuid);
        if (session != null) return session.lockCategory(category);
        boolean result = Database.lockCategory(uuid, category);
        if (result) broadcastUpdate(uuid);
        return result;
    }

    public static Component getPlayerDisplayName(UUID uuid) {
        Session session = instance.findSession(uuid);
        return session != null
            ? session.getDisplayName()
            : text(PlayerCache.nameForUuid(uuid));
    }

    public static Component getPlayerListName(UUID uuid) {
        Session session = instance.findSession(uuid);
        return session != null
            ? session.getPlayerListName()
            : text(PlayerCache.nameForUuid(uuid));
    }

    private void broadcastUpdate(UUID uuid) {
        Connect.get().broadcastMessage("connect:player_update", uuid.toString());
    }
}
