package com.winthier.title;

import lombok.Value;
import org.bukkit.ChatColor;

@Value
public final class Title {
    String name;
    String title;
    String description;

    public String formatted() {
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    public String formattedDescription() {
        if (description == null) return "";
        return ChatColor.translateAlternateColorCodes('&', description);
    }

    public String stripped() {
        return ChatColor.stripColor(formatted());
    }

    public String strippedDescription() {
        return ChatColor.stripColor(formattedDescription());
    }
}
