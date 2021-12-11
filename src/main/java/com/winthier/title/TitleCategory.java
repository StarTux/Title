package com.winthier.title;

import java.util.HashMap;
import java.util.Map;

public enum TitleCategory {
    EXTRA_RANK(Group.RANK),
    PLAYER_RANK(Group.RANK),
    STAFF_RANK(Group.RANK),
    // Events
    BINGO(Group.EVENT),
    BUILD(Group.EVENT),
    BUILD_MY_THING(Group.EVENT),
    CAPTURE_THE_FLAG(Group.EVENT),
    CAVEPAINT(Group.EVENT),
    COLORFALL(Group.EVENT),
    DUNGEON_RUSH(Group.EVENT),
    ENDERBALL(Group.EVENT),
    EXTREME_GRASS_GROWING(Group.EVENT),
    FISHING(Group.EVENT),
    HIDE_AND_SEEK(Group.EVENT),
    KING_OF_THE_LADDER(Group.EVENT),
    KING_OF_THE_RING(Group.EVENT),
    MOB_ARENA(Group.EVENT),
    OVERBOARD(Group.EVENT),
    BATTLE_BOATS(Group.EVENT),
    PARKOUR(Group.EVENT),
    PIT_OF_DOOM(Group.EVENT),
    PVP_ARENA(Group.EVENT),
    QUIZ(Group.EVENT),
    RACE(Group.EVENT),
    RAID(Group.EVENT),
    RED_LIGHT_GREEN_LIGHT(Group.EVENT),
    SPLEEF(Group.EVENT),
    SURVIVAL_GAMES(Group.EVENT),
    SWITCHCARTS(Group.EVENT),
    TREASURE_HUNT(Group.EVENT),
    ULTRA_HARDCORE(Group.EVENT),
    VERTIGO(Group.EVENT),
    WINDICATOR(Group.EVENT),
    // Seasonal
    EASTER(Group.SEASONAL),
    HALLOWEEN(Group.SEASONAL),
    MAYPOLE(Group.SEASONAL),
    VALENTINE(Group.SEASONAL),
    XMAS(Group.SEASONAL),
    // Store
    SHINE(Group.STORE),
    FLAG(Group.STORE),
    DONATION(Group.STORE),
    // ?
    LEGACY(Group.OTHER),
    ACHIEVEMENT(Group.OTHER),
    UNKNOWN;

    public enum Group {
        RANK,
        STORE,
        SEASONAL,
        EVENT,
        OTHER,
        UNKNOWN;
    }

    private static final Map<String, TitleCategory> KEY_MAP = new HashMap<>();
    public final String key;
    public final Group group;
    public final int priority;

    TitleCategory(final Group group, final int priority) {
        this.key = name().toLowerCase();
        this.group = group;
        this.priority = priority;
    }

    TitleCategory(final Group group) {
        this(group, 0);
    }

    TitleCategory() {
        this(Group.UNKNOWN, 0);
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
}
