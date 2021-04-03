package com.winthier.title;

import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;

public enum Shine {
    HEART("#FF69B4"),
    STAR("#FFD700"),
    PI("#00BFFF"), // deepskyblue
    SHAMROCK("#228B22"), // forestgreen
    PRIDE("#DB7093"), // palevioletred
    EGG("#6495ED"), // cornflowerblue
    BUNNY("#FFB6C1"), // lightpink
    COIN("#FFFF00"); // yellow

    public final String humanName;
    public final TextColor textColor;
    public final ChatColor color;

    Shine(final String color) {
        this.color = ChatColor.of(color);
        this.textColor = TextColor.fromHexString(color);
        this.humanName = Msg.toCamelCase(this);
    }
}
