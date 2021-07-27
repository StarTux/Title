package com.winthier.title;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextFormat;

public abstract class TextEffect implements TextFormat {
    private static final List<TextEffect> VALUES = new ArrayList<>();
    private static final Map<String, TextEffect> NAME_MAP = new HashMap<>();
    public static final TextEffect RAINBOW = new TextEffectRainbow();
    public static final TextEffect SEQUENCE = new TextEffectSequence();
    public static final TextEffect BANNER = new TextEffectBanner();
    public static final TextEffect GRADIENT = new TextEffectGradient();

    public static final class TextEffectRainbow extends TextEffect {
        public static final String NAME = "rainbow";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public TextEffectRainbow with(String... args) {
            if (args.length != 0) {
                throw new IllegalArgumentException("args.length != 0: " + Arrays.asList(args));
            }
            return this;
        }

        @Override
        public Component format(String in) {
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
    }

    /**
     * The color sequence colors each letter in another color,
     * according to the given color sequence, which wraps around if
     * necessary.
     */
    @RequiredArgsConstructor
    public static final class TextEffectSequence extends TextEffect {
        public static final String NAME = "sequence";
        private final List<TextColor> colors;

        public TextEffectSequence() {
            this(Collections.emptyList());
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public TextEffect with(String... args) {
            List<TextColor> list = new ArrayList<>(args.length);
            for (String it : args) {
                list.add(parseTextColor(it));
            }
            return new TextEffectSequence(list);
        }

        @Override
        public Component format(String in) {
            if (colors.size() == 0) {
                throw new IllegalArgumentException("colors.size = 0");
            }
            TextComponent.Builder cb = Component.text();
            for (int i = 0; i < in.length(); i += 1) {
                TextColor color = colors.get(i % colors.size());
                cb.append(color != null
                          ? Component.text(in.charAt(i), color)
                          : Component.text(in.charAt(i)));
            }
            return cb.build();
        }
    }

    /**
     * Spread out the given color sequence over all the letters.
     */
    @RequiredArgsConstructor
    public static final class TextEffectBanner extends TextEffect {
        public static final String NAME = "banner";
        private final List<TextColor> colors;

        public TextEffectBanner() {
            this(Collections.emptyList());
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public TextEffect with(String... args) {
            List<TextColor> list = new ArrayList<>(args.length);
            for (String it : args) {
                list.add(parseTextColor(it));
            }
            return new TextEffectBanner(list);
        }

        @Override
        public Component format(String in) {
            if (colors.size() == 0) {
                throw new IllegalArgumentException("colors.size = 0");
            }
            TextComponent.Builder cb = Component.text();
            final int len = in.length();
            for (int i = 0; i < len; i += 1) {
                double fraction = (((double) i + 0.5) / (double) len) * (double) colors.size();
                int index = (int) Math.floor(fraction);
                TextColor color = colors.get(Math.min(colors.size(), index)); // Just to be sure
                cb.append(color != null
                          ? Component.text(in.charAt(i), color)
                          : Component.text(in.charAt(i)));
            }
            return cb.build();
        }
    }

    @RequiredArgsConstructor
    public static final class TextEffectGradient extends TextEffect {
        public static final String NAME = "gradient";
        private final List<TextColor> colors;

        protected TextEffectGradient() {
            this(Collections.emptyList());
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public TextEffect with(String... args) {
            List<TextColor> list = new ArrayList<>(args.length);
            for (String it : args) {
                list.add(parseTextColor(it));
            }
            return new TextEffectGradient(list);
        }

        private static int clampRGB(int val) {
            return Math.min(255, Math.max(0, val));
        }

        @Override
        public Component format(String in) {
            if (colors.size() == 0) {
                throw new IllegalArgumentException("colors.size = 0");
            }
            final int len = in.length();
            TextComponent.Builder comps = Component.text();
            for (int i = 0; i < len; i += 1) {
                String d = "" + in.charAt(i);
                double percentage = (double) i / (double) (len - 1);
                double colorPercentage = percentage * (double) (colors.size() - 1);
                int colorIndexA = (int) Math.floor(colorPercentage);
                int colorIndexB = (int) Math.ceil(colorPercentage);
                TextColor colorA = colors.get(colorIndexA);
                TextColor colorB = colors.get(colorIndexB);
                double percentageA = (double) colorIndexA / (double) (colors.size() - 1);
                double percentageB = (double) colorIndexB / (double) (colors.size() - 1);
                double progressAB = colorIndexA == colorIndexB
                    ? 0
                    : (percentage - percentageA) / (percentageB - percentageA);
                double progressBA = 1.0 - progressAB;
                double r = (double) colorA.red()   * progressBA + colorB.red()   * progressAB;
                double g = (double) colorA.green() * progressBA + colorB.green() * progressAB;
                double b = (double) colorA.blue()  * progressBA + colorB.blue()  * progressAB;
                TextColor color = TextColor.color(clampRGB((int) Math.round(r)),
                                                  clampRGB((int) Math.round(g)),
                                                  clampRGB((int) Math.round(b)));
                comps.append(Component.text(d, color));
            }
            return comps.build();
        }
    };

    static {
        VALUES.add(RAINBOW);
        VALUES.add(SEQUENCE);
        VALUES.add(GRADIENT);
        VALUES.add(BANNER);
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
