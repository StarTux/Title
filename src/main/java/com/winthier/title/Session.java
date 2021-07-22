package com.winthier.title;

import com.winthier.title.sql.PlayerInfo;
import com.winthier.title.sql.SQLPlayerSuffix;
import com.winthier.title.sql.SQLSuffix;
import com.winthier.title.sql.UnlockedInfo;
import java.util.ArrayList;
import java.util.Collections;
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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
    // Cache
    protected Component playerListPrefix = null;
    protected Component playerListSuffix = null;
    protected Vector lastFlyingShine;
    protected Component teamPrefix = Component.empty();
    protected Component teamSuffix = Component.empty();
    protected NamedTextColor teamColor = NamedTextColor.WHITE;

    public Session(final TitlePlugin plugin, final Player player, final PlayerInfo playerRow,
                   final List<UnlockedInfo> unlockedList, final List<SQLPlayerSuffix> suffixList) {
        this.plugin = plugin;
        this.uuid = player.getUniqueId();
        this.playerName = player.getName();
        this.playerRow = playerRow;
        for (UnlockedInfo row : unlockedList) {
            unlockedRows.put(row.getTitle(), row);
        }
        for (SQLPlayerSuffix row : suffixList) {
            suffixRows.put(row.getSuffix(), row);
        }
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public List<Title> getTitles() {
        return getTitles(getPlayer());
    }

    public List<Title> getTitles(Player player) {
        List<Title> all = plugin.getTitles();
        List<Title> result = new ArrayList<>(all.size());
        for (Title title : all) {
            if (unlockedRows.containsKey(title.getName()) || title.hasPermission(player)) {
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
                if (result != 0) return;
                plugin.getLogger().info("Insert row " + row + ": " + result);
            });
        return true;
    }

    public boolean lockTitle(Title title) {
        UnlockedInfo row = unlockedRows.remove(title.getName());
        if (row == null) return false;
        plugin.getDb().deleteAsync(row, result -> {
                if (result != 0) return;
                plugin.getLogger().info("Delete row " + row + ": " + result);
            });
        if (Objects.equals(playerRow.getTitle(), title.getName())) {
            resetTitle();
        }
        return true;
    }

    public boolean setTitle(Title title) {
        return setTitle(getPlayer(), title);
    }

    public boolean setTitle(Player player, Title title) {
        List<Title> titles = getTitles(player);
        if (0 == titles.indexOf(title)) {
            // Default title
            if (playerRow.getTitle() == null) return false;
            playerRow.setTitle(null);
        } else {
            if (Objects.equals(playerRow.getTitle(), title.getName())) return false;
            playerRow.setTitle(title.getName());
        }
        plugin.getDb().updateAsync(playerRow, null, "title");
        plugin.updatePlayerName(player);
        return true;
    }

    public boolean resetTitle() {
        return resetTitle(getPlayer());
    }

    public boolean resetTitle(Player player) {
        if (playerRow.getTitle() == null) return false;
        playerRow.setTitle(null);
        plugin.getDb().updateAsync(playerRow, null, "title");
        plugin.updatePlayerName(player);
        return true;
    }

    public Title getTitle() {
        return getTitle(getPlayer());
    }

    public Title getTitle(Player player) {
        String selectedTitleName = playerRow.getTitle();
        if (selectedTitleName != null && unlockedRows.containsKey(selectedTitleName)) {
            Title selectedTitle = plugin.getTitle(selectedTitleName);
            if (selectedTitle != null) return selectedTitle;
        }
        return getTitles(player).get(0);
    }

    public Shine getShine() {
        return getShine(getPlayer());
    }

    // Nullable
    public Shine getShine(Player player) {
        Shine selectedShine = playerRow.parseShine();
        if (selectedShine != null) {
            if (!hasShine(player, selectedShine)) {
                resetShine();
            } else {
                return selectedShine;
            }
        }
        Title title = getTitle(player);
        return title.parseShine();
    }

    public boolean hasTitle(Title title) {
        return hasTitle(getPlayer(), title);
    }

    public boolean hasTitle(Player player, Title title) {
        if (unlockedRows.containsKey(title.getName())) return true;
        for (Title it : getTitles()) {
            if (it.getName().equals(title.getName())) return true;
        }
        return false;
    }

    public Set<Shine> getShines() {
        return getShines(getPlayer());
    }

    public Set<Shine> getShines(Player player) {
        Set<Shine> result = EnumSet.noneOf(Shine.class);
        for (Title title : getTitles(player)) {
            Shine titleShine = title.parseShine();
            if (titleShine != null) result.add(titleShine);
        }
        return result;
    }

    public boolean hasShine(Shine shine) {
        return hasShine(getPlayer(), shine);
    }

    public boolean hasShine(Player player, Shine shine) {
        for (Title title : getTitles(player)) {
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
        plugin.getDb().update(playerRow, "shine");
    }

    public void resetShine() {
        if (playerRow.getShine() == null) return;
        playerRow.setShine(null);
        plugin.getDb().update(playerRow, "shine");
    }

    public SQLSuffix getSuffix() {
        return getSuffix(getPlayer());
    }

    // Nullable
    public SQLSuffix getSuffix(Player player) {
        return playerRow.findSuffix();
    }

    public List<SQLSuffix> getSuffixes() {
        return getSuffixes(getPlayer());
    }

    public List<SQLSuffix> getSuffixes(Player player) {
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
        for (Title title : getTitles(player)) {
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

    public boolean hasSuffix(Player player, SQLSuffix suffix) {
        for (SQLSuffix it : getSuffixes(player)) {
            if (Objects.equals(it.getName(), suffix.getName())) return true;
        }
        return false;
    }

    public boolean unlockSuffix(String suffixName) {
        return unlockSuffix(getPlayer(), suffixName);
    }

    public boolean unlockSuffix(Player player, String suffixName) {
        if (suffixRows.containsKey(suffixName)) return false;
        SQLPlayerSuffix row = new SQLPlayerSuffix(player.getUniqueId(), suffixName);
        plugin.getDb().insertIgnoreAsync(row, result -> {
                if (result == 0) {
                    plugin.getLogger().info("Insert row " + row + ": " + result);
                }
            });
        suffixRows.put(suffixName, row);
        return true;
    }

    public boolean lockSuffix(String suffixName) {
        return lockSuffix(getPlayer(), suffixName);
    }

    public boolean lockSuffix(Player player, String suffixName) {
        SQLPlayerSuffix row = suffixRows.remove(suffixName);
        if (row == null) return false;
        plugin.getDb().deleteAsync(row, result -> {
                if (result == 0) {
                    plugin.getLogger().info("Delete row " + row + ": " + result);
                }
            });
        return true;
    }

    public void setSuffix(Player player, SQLSuffix suffix) {
        if (Objects.equals(suffix.getName(), playerRow.getSuffix())) return;
        playerRow.setSuffix(suffix.getName());
        plugin.getDb().updateAsync(playerRow, null, "suffix");
        plugin.updatePlayerName(player);
    }

    public void resetSuffix(Player player) {
        if (playerRow.getSuffix() == null) return;
        playerRow.setSuffix(null);
        plugin.getDb().updateAsync(playerRow, null, "suffix");
        plugin.updatePlayerName(player);
    }
}
