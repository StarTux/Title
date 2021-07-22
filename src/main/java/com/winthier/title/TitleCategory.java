package com.winthier.title;

import java.util.HashMap;
import java.util.Map;

public enum TitleCategory {
    UNKNOWN;

    private static final Map<String, TitleCategory> KEY_MAP = new HashMap<>();
    public final String key;
    public final int priority;

    TitleCategory(final int priority) {
        this.key = name().toLowerCase();
        this.priority = priority;
    }

    TitleCategory() {
        this(0);
    }

    static {
        for (TitleCategory it : TitleCategory.values()) {
            KEY_MAP.put(it.key, it);
        }
    }

    public static TitleCategory ofKey(String key) {
        return KEY_MAP.get(key);
    }
}
