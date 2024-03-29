package com.winthier.title;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public final class Msg {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private Msg() { }

    public static Component parseComponent(String in) {
        try {
            return GsonComponentSerializer.gson().deserialize(in);
        } catch (JsonSyntaxException je) {
            je.printStackTrace();
            return Component.empty();
        }
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
}
