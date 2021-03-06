package com.winthier.title;

import com.cavetale.core.font.Emoji;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextFormat;
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
    private String nameColor;
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean prefix;
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

    public Component getTitleComponent() {
        if (titleJson == null) return Component.text(formatted());
        if (titleJson.startsWith(":") && titleJson.endsWith(":")) {
            return Emoji.getComponent(titleJson.substring(1, titleJson.length() - 1));
        }
        return Msg.parseComponent(titleJson);
    }

    public Component getTitleTag() {
        return getTitleComponent()
            .hoverEvent(HoverEvent.showText(getTooltip()))
            .clickEvent(ClickEvent.suggestCommand("/title " + name));
    }

    public Component getTooltip() {
        TextComponent.Builder tooltip = Component.text();
        tooltip.append(getTitleComponent());
        if (prefix) {
            tooltip.append(Component.space());
            TextFormat textFormat = getNameTextFormat();
            TextColor titleColor = textFormat != null && textFormat instanceof TextColor
                ? (TextColor) textFormat
                : TextColor.color(0xffffff);
            tooltip.append(Component.text(name, titleColor));
        }
        if (description != null) {
            tooltip.append(Component.newline());
            tooltip.append(Component.text(formattedDescription(), NamedTextColor.WHITE));
        }
        return tooltip.build();
    }

    public TextFormat getNameTextFormat() {
        if (nameColor == null) return null;
        if (nameColor.startsWith("#")) {
            try {
                return TextColor.fromHexString(nameColor);
            } catch (IllegalArgumentException iae) {
                TitlePlugin.getInstance().getLogger().warning("Invalid color code: " + nameColor);
            }
        }
        TextColor textColor = NamedTextColor.NAMES.value(nameColor);
        if (textColor != null) return textColor;
        return TextEffect.of(nameColor);
    }

    public boolean isEmptyTitle() {
        return titleJson != null && titleJson.isEmpty();
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
        int prio = Integer.compare(other.priority, priority); // highest first
        if (prio != 0) return prio;
        return name.compareToIgnoreCase(other.name);
    }
}
