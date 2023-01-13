package com.winthier.title;

import com.cavetale.mytems.Mytems;
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
    SKULL(0xFFBBFF), // neon purple
    MOON(0x80ff00),
    COPPER_COIN(0xe77c56, Mytems.COPPER_COIN),
    SILVER_COIN(0xd8d8d8, Mytems.SILVER_COIN),
    GOLDEN_COIN(0xfdf55f, Mytems.GOLDEN_COIN),
    GOLDEN_HOOP(0xffff18, Mytems.GOLDEN_HOOP),
    DIAMOND_COIN(0x4aedd9, Mytems.DIAMOND_COIN),
    RUBY_COIN(0xd92d45, Mytems.RUBY_COIN),
    RAINBOW_BUTTERFLY(0xFFB6C1, Mytems.RAINBOW_BUTTERFLY),
    SNOWFLAKE(0xCCFFFF, Mytems.SNOWFLAKE);
    ;

    public static final Map<String, Shine> KEY_MAP = new HashMap<>();
    public final int hex;
    public final String key;
    public final String humanName;
    public final TextColor color;
    public final Mytems mytems;

    Shine(final int hex, final Mytems mytems) {
        this.hex = hex;
        this.key = name().toLowerCase();
        this.color = TextColor.color(hex);
        this.humanName = Msg.toCamelCase(this);
        this.mytems = mytems;
    }

    Shine(final int hex) {
        this(hex, null);
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
