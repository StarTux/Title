package com.winthier.title;

import net.kyori.adventure.text.format.TextColor;

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
    EAGLE(0xBA6426); // brownish

    public final String humanName;
    public final TextColor color;

    Shine(final int hex) {
        this.color = TextColor.color(hex);
        this.humanName = Msg.toCamelCase(this);
    }
}
