package com.winthier.title;

import com.cavetale.core.connect.Connect;
import com.cavetale.mytems.Mytems;
import com.winthier.title.sql.PlayerInfo;
import com.winthier.title.sql.SQLPlayerSuffix;
import com.winthier.title.sql.SQLSuffix;
import com.winthier.title.sql.UnlockedInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.util.Vector;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

/**
 * A session is kept alive as long as a player stays logged in.
 */
@RequiredArgsConstructor
public final class Session {
    // Data
    protected final TitlePlugin plugin;
    @Getter protected final UUID uuid;
    @Getter protected final String playerName;
    protected final PlayerInfo playerRow;
    protected final Map<String, UnlockedInfo> unlockedRows = new HashMap<>();
    protected final Map<String, SQLPlayerSuffix> suffixRows = new HashMap<>();
    protected final Map<TitleCategory, UnlockedInfo> unlockedCategories = new EnumMap<>(TitleCategory.class);
    // Cache
    protected Component playerListPrefix = null;
    protected Component playerListSuffix = null;
    protected NamedTextColor color = null;
    protected Vector lastFlyingShine;
    protected Component teamPrefix = empty(); // derived, name tag
    protected Component teamSuffix = empty(); // derived, name tag
    // Animation
    protected boolean animated = false;
    protected TextEffect textEffect;
    protected String rawPlayerName;
    protected Component rawDisplayName;
    protected Component rawPrefix;
    protected Component rawSuffix;
    protected Mytems mytemsPrefix;
    protected Mytems mytemsSuffix;
    protected Component titleTooltip;
    // Timing
    protected long lastUsed;
    @Getter protected Component displayName;
    @Getter protected Component playerListName;

    public Session(final TitlePlugin plugin, final UUID uuid, final String playerName, final PlayerInfo playerRow,
                   final List<UnlockedInfo> unlockedList, final List<SQLPlayerSuffix> suffixList) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.playerName = playerName;
        this.playerRow = playerRow;
        for (UnlockedInfo row : unlockedList) {
            if (row.getTitle().startsWith("#")) {
                String key = row.getTitle().substring(1);
                TitleCategory cat = TitleCategory.ofKey(key);
                if (cat != null) unlockedCategories.put(cat, row);
            } else {
                unlockedRows.put(row.getTitle(), row);
            }
        }
        for (SQLPlayerSuffix row : suffixList) {
            suffixRows.put(row.getSuffix(), row);
        }
        lastUsed = System.currentTimeMillis();
        displayName = text(playerName);
        playerListName = text(playerName);
    }

    public List<Title> getTitles() {
        List<Title> all = plugin.getTitles();
        List<Title> result = new ArrayList<>(all.size());
        for (Title title : all) {
            if (unlockedRows.containsKey(title.getName())
                || unlockedCategories.containsKey(title.parseCategory())
                || title.hasPermission(uuid)) {
                result.add(title);
            }
        }
        Collections.sort(result);
        return result;
    }

    public boolean unlockTitle(Title title) {
        if (unlockedRows.containsKey(title.getName())) return false;
        UnlockedInfo row = new UnlockedInfo(uuid, title.getName());
        unlockedRows.put(title.getName(), row);
        plugin.getDb().insertIgnoreAsync(row, result -> {
                broadcastUpdate();
                if (result == 0) {
                    plugin.getLogger().info("Insert row " + row + ": " + result);
                }
            });
        return true;
    }

    public boolean lockTitle(Title title) {
        UnlockedInfo row = unlockedRows.remove(title.getName());
        if (row == null) return false;
        plugin.getDb().deleteAsync(row, result -> {
                broadcastUpdate();
                if (result == 0) {
                    plugin.getLogger().info("Delete row " + row + ": " + result);
                }
            });
        if (Objects.equals(playerRow.getTitle(), title.getName())) {
            resetTitle();
        }
        return true;
    }

    public boolean setTitle(Title title) {
        List<Title> titles = getTitles();
        if (0 == titles.indexOf(title)) {
            // Default title
            if (playerRow.getTitle() == null) return false;
            playerRow.setTitle(null);
        } else {
            if (Objects.equals(playerRow.getTitle(), title.getName())) return false;
            playerRow.setTitle(title.getName());
        }
        plugin.getDb().updateAsync(playerRow, x -> broadcastUpdate(), "title");
        plugin.updatePlayerName(uuid);
        return true;
    }

    public boolean resetTitle() {
        if (playerRow.getTitle() != null) {
            playerRow.setTitle(null);
            plugin.getDb().updateAsync(playerRow, x -> broadcastUpdate(), "title");
        }
        plugin.updatePlayerName(uuid);
        return true;
    }

    public Title getTitle() {
        String selectedTitleName = playerRow.getTitle();
        if (selectedTitleName != null) {
            Title selectedTitle = plugin.getTitle(selectedTitleName);
            if (selectedTitle != null) return selectedTitle;
        }
        return getTitles().get(0);
    }

    // Nullable
    public Shine getShine() {
        if ("none".equals(playerRow.getShine())) {
            return null;
        }
        Shine selectedShine = playerRow.parseShine();
        if (selectedShine != null) {
            return selectedShine;
        }
        Title title = getTitle();
        return title.parseShine();
    }

    public boolean hasTitle(Title title) {
        if (unlockedRows.containsKey(title.getName())) return true;
        for (Title it : getTitles()) {
            if (it.getName().equals(title.getName())) return true;
        }
        return false;
    }

    public Set<Shine> getShines() {
        Set<Shine> result = EnumSet.noneOf(Shine.class);
        for (Title title : getTitles()) {
            Shine titleShine = title.parseShine();
            if (titleShine != null) result.add(titleShine);
        }
        return result;
    }

    public boolean hasShine(Shine shine) {
        for (Title title : getTitles()) {
            Shine titleShine = title.parseShine();
            if (titleShine != null && shine == titleShine) {
                return true;
            }
        }
        return false;
    }

    public void setShine(Shine shine) {
        if (playerRow.parseShine() == shine) return;
        playerRow.setShine(shine.key);
        plugin.getDb().updateAsync(playerRow, x -> broadcastUpdate(), "shine");
    }

    public void resetShine() {
        if (playerRow.getShine() == null) return;
        playerRow.setShine(null);
        plugin.getDb().updateAsync(playerRow, x -> broadcastUpdate(), "shine");
    }

    public void disableShine() {
        if ("none".equals(playerRow.getShine())) return;
        playerRow.setShine("none");
        plugin.getDb().updateAsync(playerRow, x -> broadcastUpdate(), "shine");
    }

    public SQLSuffix getSuffix() {
        return playerRow.findSuffix();
    }

    // Nullable
    public List<SQLSuffix> getSuffixes() {
        Set<String> set = new HashSet<>();
        for (SQLPlayerSuffix unlocked : suffixRows.values()) {
            String name = unlocked.getSuffix();
            if (name.startsWith("#")) {
                List<SQLSuffix> category = plugin.getSuffixCategories().get(name.substring(1));
                if (category != null) {
                    for (SQLSuffix suffix : category) {
                        set.add(suffix.getName());
                    }
                }
            } else {
                set.add(name);
            }
        }
        for (Title title : getTitles()) {
            String name = title.getSuffix();
            if (name != null) {
                if (name.startsWith("#")) {
                    List<SQLSuffix> category = plugin.getSuffixCategories().get(name.substring(1));
                    if (category != null) {
                        for (SQLSuffix suffix : category) {
                            set.add(suffix.getName());
                        }
                    }
                } else {
                    set.add(name);
                }
            }
        }
        List<SQLSuffix> result = new ArrayList<>(set.size());
        for (String name : set) {
            SQLSuffix suffix = plugin.getSuffixes().get(name);
            if (suffix != null) result.add(suffix);
        }
        Collections.sort(result);
        return result;
    }

    public boolean hasSuffixUnlocked(String suffixName) {
        return suffixRows.containsKey(suffixName);
    }

    public boolean hasSuffix(SQLSuffix suffix) {
        for (SQLSuffix it : getSuffixes()) {
            if (Objects.equals(it.getName(), suffix.getName())) return true;
        }
        return false;
    }

    public boolean unlockSuffix(String suffixName) {
        if (suffixRows.containsKey(suffixName)) return false;
        SQLPlayerSuffix row = new SQLPlayerSuffix(uuid, suffixName);
        plugin.getDb().insertIgnoreAsync(row, result -> {
                if (result == 0) {
                    plugin.getLogger().info("Insert row " + row + ": " + result);
                }
                broadcastUpdate();
            });
        suffixRows.put(suffixName, row);
        return true;
    }

    public boolean lockSuffix(String suffixName) {
        SQLPlayerSuffix row = suffixRows.remove(suffixName);
        if (row == null) return false;
        plugin.getDb().deleteAsync(row, result -> {
                if (result == 0) {
                    plugin.getLogger().info("Delete row " + row + ": " + result);
                }
                broadcastUpdate();
            });
        return true;
    }

    public void setSuffix(SQLSuffix suffix) {
        if (Objects.equals(suffix.getName(), playerRow.getSuffix())) return;
        playerRow.setSuffix(suffix.getName());
        plugin.getDb().updateAsync(playerRow, x -> broadcastUpdate(), "suffix");
        plugin.updatePlayerName(uuid);
    }

    public void resetSuffix() {
        if (playerRow.getSuffix() == null) return;
        playerRow.setSuffix(null);
        plugin.getDb().updateAsync(playerRow, x -> broadcastUpdate(), "suffix");
        plugin.updatePlayerName(uuid);
    }

    /**
     * Check if current settings are valid and reset them if
     * necessary.
     */
    public void updateValidity() {
        Title title = getTitle();
        if (title != null && !hasTitle(title)) {
            plugin.getLogger().info(playerName + " has invalid title selected: " + title.getName());
            resetTitle();
        }
    }

    public boolean unlockCategory(TitleCategory category) {
        if (unlockedCategories.containsKey(category)) return false;
        UnlockedInfo row = new UnlockedInfo(uuid, "#" + category.key);
        unlockedCategories.put(category, row);
        plugin.getDb().insertIgnoreAsync(row, result -> {
                if (result == 0) {
                    plugin.getLogger().info("Insert row " + row + ": " + result);
                }
                broadcastUpdate();
            });
        return true;
    }

    public boolean lockCategory(TitleCategory category) {
        UnlockedInfo row = unlockedCategories.remove(category);
        if (row == null) return false;
        plugin.getDb().deleteAsync(row, result -> {
                if (result == 0) {
                    plugin.getLogger().info("Delete row " + row + ": " + result);
                }
                broadcastUpdate();
            });
        return true;
    }

    public void broadcastUpdate() {
        Connect.get().broadcastMessage("connect:player_update", uuid.toString());
    }
}
