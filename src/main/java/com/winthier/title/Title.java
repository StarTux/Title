package com.winthier.title;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

@Data @Table(name = "titles")
public final class Title implements Comparable<Title> {
    @Id
    private Integer id;
    @Column(nullable = false, length = 32, unique = true)
    private String name;
    @Column(nullable = false, length = 255)
    private String title;
    @Column(nullable = true, length = 255)
    private String description;
    @Column(nullable = false, columnDefinition = "INT(3) DEFAULT 0")
    private int priority;
    @Column(nullable = true, length = 4096)
    private String titleJson;
    @Column(nullable = true, length = 255)
    private String playerListPrefix;
    @Column(nullable = true, length = 255)
    private String shine;

    public Title() { }

    public Title(final String name, final String title, final String description) {
        this.name = name;
        this.title = title;
        this.description = description;
    }

    public String formatted() {
        return ChatColor.translateAlternateColorCodes('&', getTitle());
    }

    public String formattedDescription() {
        if (getDescription() == null) return "";
        return ChatColor.translateAlternateColorCodes('&', getDescription());
    }

    public String stripped() {
        return ChatColor.stripColor(formatted());
    }

    public String strippedDescription() {
        return ChatColor.stripColor(formattedDescription());
    }

    public String formattedPlayerListPrefix() {
        return getPlayerListPrefix() != null ? Msg.colorize(getPlayerListPrefix()) : "";
    }

    public TextComponent getTitleComponent() {
        return getTitleJson() != null
            ? new TextComponent(Msg.toComponent(getTitleJson()))
            : new TextComponent(formatted());
    }

    public Shine parseShine() {
        String str = getShine();
        if (str == null) return null;
        try {
            return Shine.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    @Override
    public int compareTo(Title other) {
        int prio = Integer.compare(other.getPriority(), getPriority()); // reverse!
        if (prio != 0) return prio;
        return getName().compareToIgnoreCase(other.getName());
    }
}
