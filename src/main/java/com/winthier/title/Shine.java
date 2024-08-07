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
    COPPER_COIN(0xe77c56, Mytems.COPPER_COIN_SHINE),
    SILVER_COIN(0xd8d8d8, Mytems.SILVER_COIN_SHINE),
    GOLDEN_COIN(0xfdf55f, Mytems.GOLDEN_COIN_SHINE),
    GOLDEN_HOOP(0xffff18, Mytems.GOLDEN_HOOP_SHINE),
    DIAMOND_COIN(0x4aedd9, Mytems.DIAMOND_COIN_SHINE),
    RUBY_COIN(0xd92d45, Mytems.RUBY_COIN_SHINE),
    RAINBOW_BUTTERFLY(0xFFB6C1, Mytems.RAINBOW_BUTTERFLY),
    SNOWFLAKE(0xCCFFFF, Mytems.SNOWFLAKE),
    DICE(0xAAAAAA, Mytems.DICE_ROLL),
    LIGHTNING(0x00FFFF, Mytems.LIGHTNING),
    SUNSHINE(0xFFFF00),
    BLUE_BUTTERFLY(0x9595D8, Mytems.BLUE_BUTTERFLY),
    CYAN_BUTTERFLY(0x399D9D, Mytems.CYAN_BUTTERFLY),
    GREEN_BUTTERFLY(0x1DB91D, Mytems.GREEN_BUTTERFLY),
    ORANGE_BUTTERFLY(0xFF8000, Mytems.ORANGE_BUTTERFLY),
    PINK_BUTTERFLY(0xD89595, Mytems.PINK_BUTTERFLY),
    PURPLE_BUTTERFLY(0xD85FD8, Mytems.PURPLE_BUTTERFLY),
    YELLOW_BUTTERFLY(0xD8D85F, Mytems.YELLOW_BUTTERFLY),
    CHRISMAS_TREE(0x3C8D0D, Mytems.CHRISTMAS_TREE),
    MOM(0xD32A2A, Mytems.MOM),
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
