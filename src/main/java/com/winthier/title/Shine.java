package com.winthier.title;

import net.md_5.bungee.api.ChatColor;

public enum Shine {
    HEART(ChatColor.of("#FF69B4")),
    STAR(ChatColor.of("#FFD700")),
    PI(ChatColor.of("#00BFFF")), // deepskyblue
    SHAMROCK(ChatColor.of("#228B22")), // forestgreen
    PRIDE(ChatColor.of("#DB7093")); // palevioletred

    public final String humanName;
    public final ChatColor color;

    Shine(final ChatColor color) {
        this.color = color;
        this.humanName = Msg.toCamelCase(this);
    }
}
