package com.winthier.title;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Map;
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
            } else {
                result[i++] = new TextComponent("");
            }
        }
        return result;
    }

    public static String colorize(String in) {
        return ChatColor.translateAlternateColorCodes('&', in);
    }
}
