package com.winthier.title;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import net.kyori.adventure.text.Component;
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
        return Msg.colorize(title);
    }

    public String formattedDescription() {
        if (description == null) return "";
        return Msg.colorize(description);
    }

    public String stripped() {
        return ChatColor.stripColor(formatted());
    }

    public String strippedDescription() {
        return ChatColor.stripColor(formattedDescription());
    }

    public String formattedPlayerListPrefix() {
        return playerListPrefix != null ? Msg.colorize(playerListPrefix) : "";
    }

    public Component getTitleComponent() {
        return titleJson != null
            ? Msg.parseComponent(titleJson)
            : Component.text(formatted());
    }

    public Shine parseShine() {
        if (shine == null) return null;
        try {
            return Shine.valueOf(shine.toUpperCase());
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    @Override
    public int compareTo(Title other) {
        int prio = Integer.compare(other.priority, priority); // reverse!
        if (prio != 0) return prio;
        return name.compareToIgnoreCase(other.name);
    }
}
