package com.winthier.title;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

public interface Title extends Comparable<Title> {
    String getName();

    String getTitle();

    String getDescription();

    int getPriority();

    String getTitleJson();

    String getPlayerListPrefix();

    String getShine();

    default String formatted() {
        return ChatColor.translateAlternateColorCodes('&', getTitle());
    }

    default String formattedDescription() {
        if (getDescription() == null) return "";
        return ChatColor.translateAlternateColorCodes('&', getDescription());
    }

    default String stripped() {
        return ChatColor.stripColor(formatted());
    }

    default String strippedDescription() {
        return ChatColor.stripColor(formattedDescription());
    }

    default String formattedPlayerListPrefix() {
        return getPlayerListPrefix() != null ? Msg.colorize(getPlayerListPrefix()) : "";
    }

    default TextComponent getTitleComponent() {
        return getTitleJson() != null
            ? new TextComponent(Msg.toComponent(getTitleJson()))
            : new TextComponent(formatted());
    }

    default Shine parseShine() {
        String str = getShine();
        if (str == null) return null;
        try {
            return Shine.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    @Override
    default int compareTo(Title other) {
        int prio = Integer.compare(other.getPriority(), getPriority()); // reverse!
        if (prio != 0) return prio;
        return getName().compareToIgnoreCase(other.getName());
    }
}
