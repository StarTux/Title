package com.winthier.title;

import net.kyori.adventure.text.format.TextColor;

public enum Shine {
    HEART("#FF69B4"),
    STAR("#FFD700"),
    PI("#00BFFF"), // deepskyblue
    SHAMROCK("#228B22"), // forestgreen
    PRIDE("#DB7093"), // palevioletred
    EGG("#6495ED"), // cornflowerblue
    BUNNY("#FFB6C1"), // lightpink
    COIN("#FFFF00"), // yellow
    EARTH("#4169E1"), // royalblue
    YINYANG("#808080");

    public final String humanName;
    public final TextColor color;

    Shine(final String color) {
        this.color = TextColor.fromHexString(color);
        this.humanName = Msg.toCamelCase(this);
    }
}
