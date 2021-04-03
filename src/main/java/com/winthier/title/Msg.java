package com.winthier.title;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public final class Msg {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private Msg() { }

    public static HoverEvent hover(String msg) {
        BaseComponent[] lore = TextComponent.fromLegacyText(msg);
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, lore);
    }

    public static HoverEvent hover(BaseComponent[] lore) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, lore);
    }

    public static ClickEvent click(String cmd) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd);
    }

    public static ComponentBuilder builder(String txt) {
        return new ComponentBuilder(txt);
    }

    public static TextComponent text(String in) {
        return new TextComponent(in);
    }

    public static BaseComponent[] toComponent(String in) {
        List<Object> list = (List<Object>) Msg.GSON.fromJson(in, List.class);
        if (list == null) return new BaseComponent[0];
        BaseComponent[] result = new BaseComponent[list.size()];
        int i = 0;
        for (Object o : list) {
            if (o instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) o;
                Object text = map.get("text");
                TextComponent component = new TextComponent(text instanceof String ? (String) text : "");
                Object color = map.get("color");
                if (color instanceof String) component.setColor(ChatColor.of((String) color));
                Object italic = map.get("italic");
                if (italic instanceof Boolean) component.setItalic((Boolean) italic);
                Object bold = map.get("bold");
                if (bold instanceof Boolean) component.setBold((Boolean) bold);
                Object underlined = map.get("underlined");
                if (underlined instanceof Boolean) component.setUnderlined((Boolean) underlined);
                Object strikethrough = map.get("strikethrough");
                if (strikethrough instanceof Boolean) component.setStrikethrough((Boolean) strikethrough);
                Object obfuscated = map.get("obfuscated");
                if (obfuscated instanceof Boolean) component.setObfuscated((Boolean) obfuscated);
                Object font = map.get("font");
                if (font instanceof String) component.setFont((String) font);
                result[i++] = component;
            } else if (o instanceof String) {
                result[i++] = new TextComponent((String) o);
            } else {
                result[i++] = new TextComponent("");
            }
        }
        return result;
    }

    public static Component parseComponent(String in) {
        return GsonComponentSerializer.gson().deserialize(in);
    }

    public static String colorize(String in) {
        return ChatColor.translateAlternateColorCodes('&', in);
    }

    public static String toCamelCase(String in) {
        return in.substring(0, 1).toUpperCase()
            + in.substring(1).toLowerCase();
    }

    public static String toCamelCase(String[] in) {
        String[] out = new String[in.length];
        for (int i = 0; i < in.length; i += 1) {
            out[i] = toCamelCase(in[i]);
        }
        return String.join(" ", out);
    }

    public static String toCamelCase(Enum en) {
        return toCamelCase(en.name().split("_"));
    }

    public static String rainbowify(String in) {
        final ChatColor[] colors = {
            ChatColor.RED,
            ChatColor.GOLD,
            ChatColor.YELLOW,
            ChatColor.GREEN,
            ChatColor.DARK_AQUA,
            ChatColor.BLUE,
            ChatColor.LIGHT_PURPLE
        };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in.length(); i += 1) {
            sb.append(colors[i % colors.length]);
            sb.append(in.charAt(i));
        }
        return sb.toString();
    }
}
