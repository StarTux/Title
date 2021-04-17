package com.winthier.title;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public final class GradientCommand implements CommandExecutor {
    private final TitlePlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        Iterator<String> iter = Arrays.asList(args).iterator();
        List<String> words = new ArrayList<>();
        Color[] colors = null;
        boolean italic = false;
        boolean bold = false;
        boolean underlined = false;
        boolean debug = false;
        while (iter.hasNext()) {
            String it = iter.next();
            switch (it) {
            case "-c": {
                String[] toks = iter.next().split(",");
                colors = new Color[toks.length];
                for (int i = 0; i < toks.length; i += 1) {
                    int colorValue;
                    try {
                        colorValue = Integer.parseInt(toks[i], 16);
                    } catch (NumberFormatException nfe) {
                        sender.sendMessage(ChatColor.RED + "Invalid color code: " + toks[i]);
                        return true;
                    }
                    Color color = new Color(colorValue);
                    colors[i] = color;
                    if (debug) {
                        System.err.println("color[" + i + "]=" + toRGB(color));
                    }
                }
                break;
            }
            case "-i":
                italic = true;
                break;
            case "-b":
                bold = true;
                break;
            case "-u":
                bold = true;
                break;
            case "-d":
                debug = true;
                break;
            case "--":
                while (iter.hasNext()) words.add(iter.next());
                break;
            default:
                words.add(it);
            }
        }
        if (colors == null) {
            sender.sendMessage(ChatColor.RED + "No color!");
            return true;
        }
        String name = String.join(" ", words);
        if (debug) {
            System.err.println("name=" + name);
        }
        int len = name.length();
        List<Map<String, Object>> comps = new ArrayList<>(len);
        for (int i = 0; i < len; i += 1) {
            String d = "" + name.charAt(i);
            String c = d.equals("\"") ? "\\" + d : d;
            double percentage = (double) i / (double) (len - 1);
            double colorPercentage = percentage * (double) (colors.length - 1);
            int colorIndexA = (int) Math.floor(colorPercentage);
            int colorIndexB = (int) Math.ceil(colorPercentage);
            Color colorA = colors[colorIndexA];
            Color colorB = colors[colorIndexB];
            float[] a = new float[4];
            float[] b = new float[4];
            colorA.getRGBComponents(a);
            colorB.getRGBComponents(b);
            double percentageA = (double) colorIndexA / (double) (colors.length - 1);
            double percentageB = (double) colorIndexB / (double) (colors.length - 1);
            double progressAB = colorIndexA == colorIndexB
                ? 0
                : (percentage - percentageA) / (percentageB - percentageA);
            double progressBA = 1.0 - progressAB;
            float red   = a[0] * (float) progressBA + b[0] * (float) progressAB;
            float green = a[1] * (float) progressBA + b[1] * (float) progressAB;
            float blue  = a[2] * (float) progressBA + b[2] * (float) progressAB;
            Color color = new Color(red, green, blue);
            if (debug) {
                System.err.println(d + " " + colorIndexA + "-" + colorIndexB + " " + String.format("%.02f", progressAB) + " #" + toRGB(color));
            }
            Map<String, Object> comp = new LinkedHashMap<>();
            comp.put("text", c);
            comp.put("color", "#" + toRGB(color));
            if (italic) comp.put("italic", true);
            if (bold) comp.put("bold", true);
            if (underlined) comp.put("underlined", true);
            comps.add(comp);
        }
        String json = Msg.GSON.toJson(comps);
        plugin.getLogger().info(json);
        Component text = Msg.parseComponent(json).insertion(json);
        sender.sendMessage(text);
        return true;
    }

    static String toRGB(Color color) {
        String result = Integer.toHexString(color.getRGB() & 0xFFFFFF);
        while (result.length() < 6) {
            result = "0" + result;
        }
        return result;
    }
}
