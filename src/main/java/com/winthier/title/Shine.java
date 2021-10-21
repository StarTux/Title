package com.winthier.title;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.kyori.adventure.text.format.TextColor;

@Getter
public enum Shine {
    HEART(0xFF69B4),
    STAR(0xFFD700),
    PI(0x00BFFF), // deepskyblue
    SHAMROCK(0x228B22), // forestgreen
    PRIDE(0xDB7093), // palevioletred
    EGG(0x6495ED), // cornflowerblue
    BUNNY(0xFFB6C1), // lightpink
    COIN(0xFFFF00), // yellow
    EARTH(0x4169E1), // royalblue
    YINYANG(0x808080),
    EAGLE(0xBA6426), // brownish
    GOAT(0xFFCD79), // gold
    PUMPKIN(0xFF8000), // orange
    SKULL(0xFFBBFF); // neon purple

    public static final Map<String, Shine> KEY_MAP = new HashMap<>();
    public final String key;
    public final String humanName;
    public final TextColor color;

    Shine(final int hex) {
        this.key = name().toLowerCase();
        this.color = TextColor.color(hex);
        this.humanName = Msg.toCamelCase(this);
    }

    static {
        for (Shine shine : Shine.values()) {
            KEY_MAP.put(shine.key, shine);
        }
    }

    public static Shine ofKey(String key) {
        return KEY_MAP.get(key);
    }
}
