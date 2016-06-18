package com.winthier.title;

import lombok.Value;
import org.bukkit.ChatColor;

@Value
public class Title {
    String name;
    String title;
    String description;

    public String formatted() {
        return ChatColor.translateAlternateColorCodes('&', title);
    }
}
