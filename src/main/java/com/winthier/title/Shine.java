package com.winthier.title;

import net.md_5.bungee.api.ChatColor;

public enum Shine {
    HEART(ChatColor.of("#FF69B4"));

    public final String humanName;
    public final ChatColor color;

    Shine(final ChatColor color) {
        this.color = color;
        this.humanName = Msg.toCamelCase(this);
    }
}
