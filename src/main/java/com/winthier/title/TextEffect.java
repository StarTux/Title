package com.winthier.title;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextFormat;

public enum TextEffect implements TextFormat {
    RAINBOW() {
        @Override public Component format(String in) {
            final NamedTextColor[] colors = {
                NamedTextColor.RED,
                NamedTextColor.GOLD,
                NamedTextColor.YELLOW,
                NamedTextColor.GREEN,
                NamedTextColor.DARK_AQUA,
                NamedTextColor.BLUE,
                NamedTextColor.LIGHT_PURPLE
            };
            TextComponent.Builder cb = Component.text();
            for (int i = 0; i < in.length(); i += 1) {
                cb.append(Component.text(in.charAt(i), colors[i % colors.length]));
            }
            return cb.build();
        }
    };

    public static TextEffect of(String in) {
        try {
            return valueOf(in.toUpperCase());
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    public abstract Component format(String in);
}
