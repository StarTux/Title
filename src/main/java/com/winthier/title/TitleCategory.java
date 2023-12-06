package com.winthier.title;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import static com.cavetale.core.util.CamelCase.toCamelCase;

@Getter
public enum TitleCategory {
    EXTRA_RANK(TitleGroup.RANK),
    PLAYER_RANK(TitleGroup.RANK),
    STAFF_RANK(TitleGroup.RANK),
    TIER(TitleGroup.RANK),
    // Events
    BINGO(TitleGroup.EVENT),
    BUILD(TitleGroup.EVENT),
    BUILD_MY_THING(TitleGroup.EVENT, "Build my Thing"),
    CAPTURE_THE_FLAG(TitleGroup.EVENT, "Capture the Flag"),
    CAVEPAINT(TitleGroup.EVENT),
    COLORFALL(TitleGroup.EVENT),
    DUNGEON_RUSH(TitleGroup.EVENT),
    ENDERBALL(TitleGroup.EVENT),
    EXTREME_GRASS_GROWING(TitleGroup.EVENT, "Extreme Grass Growing", "EGG"),
    FISHING(TitleGroup.EVENT),
    HIDE_AND_SEEK(TitleGroup.EVENT, "Hide and Seek"),
    KING_OF_THE_LADDER(TitleGroup.EVENT, "King of the Ladder", "KOTL"),
    KING_OF_THE_RING(TitleGroup.EVENT, "King of the Ring", "KOTR"),
    MOB_ARENA(TitleGroup.EVENT),
    OVERBOARD(TitleGroup.EVENT),
    BATTLE_BOATS(TitleGroup.EVENT),
    PARKOUR(TitleGroup.EVENT),
    PIT_OF_DOOM(TitleGroup.EVENT, "Pit of Doom"),
    PVP_ARENA(TitleGroup.EVENT, "PvP Arena"),
    QUIZ(TitleGroup.EVENT),
    RACE(TitleGroup.EVENT),
    RAID(TitleGroup.EVENT),
    RED_LIGHT_GREEN_LIGHT(TitleGroup.EVENT, "Red Light Green Light", "RLGL"),
    SPLEEF(TitleGroup.EVENT),
    SURVIVAL_GAMES(TitleGroup.EVENT),
    SWITCHCARTS(TitleGroup.EVENT),
    TETRIS(TitleGroup.EVENT),
    TREASURE_HUNT(TitleGroup.EVENT),
    ULTRA_HARDCORE(TitleGroup.EVENT),
    VERTIGO(TitleGroup.EVENT),
    WINDICATOR(TitleGroup.EVENT),
    // Seasonal
    EASTER(TitleGroup.SEASONAL),
    HALLOWEEN(TitleGroup.SEASONAL),
    MAYPOLE(TitleGroup.SEASONAL),
    VALENTINE(TitleGroup.SEASONAL),
    XMAS(TitleGroup.SEASONAL),
    // Store
    SHINE(TitleGroup.STORE),
    FLAG(TitleGroup.STORE),
    PRIDE_FLAG(TitleGroup.STORE),
    COUNTRY_FLAG(TitleGroup.STORE),
    FUN_FLAG(TitleGroup.STORE),
    BUTTERFLY(TitleGroup.STORE),
    COIN(TitleGroup.STORE),
    DONATION(TitleGroup.STORE),
    // ?
    LEGACY(TitleGroup.OTHER),
    ACHIEVEMENT(TitleGroup.OTHER),
    UNKNOWN(TitleGroup.UNKNOWN),
    HIDDEN(TitleGroup.UNKNOWN),
    ;

    private static final Map<String, TitleCategory> KEY_MAP = new HashMap<>();
    public final String key;
    public final TitleGroup group;
    public final String displayName;
    public final String shorthand;

    TitleCategory(final TitleGroup group, final String displayName, final String shorthand) {
        this.key = name().toLowerCase();
        this.group = group;
        this.displayName = displayName;
        this.shorthand = shorthand;
    }

    TitleCategory(final TitleGroup group, final String displayName) {
        this(group, displayName, displayName);
    }

    TitleCategory(final TitleGroup group) {
        this.key = name().toLowerCase();
        this.group = group;
        this.displayName = toCamelCase(" ", this);
        this.shorthand = displayName;
    }

    static {
        for (TitleCategory it : TitleCategory.values()) {
            KEY_MAP.put(it.key, it);
        }
    }

    public static TitleCategory ofKey(String key) {
        TitleCategory result = KEY_MAP.get(key);
        return result != null ? result : UNKNOWN;
    }

    public List<Title> getTitles() {
        List<Title> result = new ArrayList<>();
        for (Title title : TitlePlugin.getInstance().getTitles()) {
            if (title.parseCategory() == this) {
                result.add(title);
            }
        }
        return result;
    }
}
