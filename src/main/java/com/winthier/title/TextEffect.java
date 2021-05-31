package com.winthier.title;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextFormat;

public abstract class TextEffect implements TextFormat {
    private static final List<TextEffect> VALUES = new ArrayList<>();
    private static final Map<String, TextEffect> NAME_MAP = new HashMap<>();

    public static TextEffect RAINBOW = new TextEffect() {
            @Getter String name = "rainbow";

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

    public static TextEffect SEQUENCE = new TextEffect() {
            @Getter String name = "sequence";
            private List<TextColor> colors;

            /**
             * @bug This is very much not thread-safe.
             */
            @Override public TextEffect with(String... in) {
                colors = new ArrayList<>(in.length);
                for (String it : in) {
                    colors.add(parseTextColor(it));
                }
                return this;
            }

            @Override public Component format(String in) {
                TextComponent.Builder cb = Component.text();
                for (int i = 0; i < in.length(); i += 1) {
                    TextColor color = colors.get(i % colors.size());
                    cb.append(color != null
                              ? Component.text(in.charAt(i), color)
                              : Component.text(in.charAt(i)));
                }
                return cb.build();
            }
        };

    static {
        VALUES.add(RAINBOW);
        VALUES.add(SEQUENCE);
        for (TextEffect it : VALUES) {
            NAME_MAP.put(it.getName(), it);
        }
    }

    public static TextEffect of(String in) {
        String name;
        String[] args;
        if (in.indexOf(" ") >= 0) {
            String[] toks = in.split(" ");
            name = toks[0];
            args = Arrays.copyOfRange(toks, 1, toks.length);
        } else {
            name = in;
            args = null;
        }
        TextEffect result = NAME_MAP.get(name);
        return args != null && result != null
            ? result.with(args)
            : result;
    }

    /**
     * The name by which this effect is known.
     */
    public abstract String getName();

    /**
     * Turn the input (usually player name) into a component.
     */
    public abstract Component format(String in);

    /**
     * Overrides may accept space separated arguments.
     */
    public TextEffect with(String... args) {
        return this;
    }

    private static TextColor parseTextColor(String in) {
        if (in == null) {
            return null;
        } else if (in.startsWith("#")) {
            try {
                return TextColor.fromHexString(in);
            } catch (IllegalArgumentException iae) {
                TitlePlugin.getInstance().getLogger().warning("Invalid color code: " + in);
                return null;
            }
        } else {
            return NamedTextColor.NAMES.value(in);
        }
    }
}
