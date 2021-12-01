package com.winthier.title.html;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;

@Data @RequiredArgsConstructor
public final class HtmlNode {
    private final Type type;
    private Map<String, String> attributes = new LinkedHashMap<>();
    private List<HtmlNode> children = new ArrayList<>();
    private Map<String, String> styleMap = new LinkedHashMap<>();
    private String text;

    public enum Type {
        HTML,
        META,
        A,
        DIV,
        SPAN,
        IMG,
        HEADER,
        BODY,
        TITLE,
        UL, LI,
        H2,
        H3,
        TABLE,
        TR,
        TD,
        STYLE;

        public HtmlNode node() {
            return new HtmlNode(this);
        }

        public void node(Consumer<HtmlNode> consumer) {
            HtmlNode node = new HtmlNode(this);
            consumer.accept(node);
        }
    }

    public void print(PrintStream out) {
        out.print("<");
        out.print(type.name());
        // Build style
        if (!styleMap.isEmpty()) {
            List<String> styles = new ArrayList<>();
            for (Map.Entry<String, String> entry : styleMap.entrySet()) {
                styles.add(entry.getKey() + ":" + entry.getValue());
            }
            attributes.put("style", String.join(";", styles));
        }
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            out.print(" ");
            out.print(entry.getKey());
            out.print("=\"");
            out.print(entry.getValue());
            out.print("\"");
        }
        out.print(">");
        if (type == Type.META) return;
        if (text != null) out.print(text);
        for (HtmlNode child : children) {
            child.print(out);
        }
        out.print("</");
        out.print(type.name());
        out.print(">");
    }

    public HtmlNode withText(String newText) {
        this.text = newText;
        return this;
    }

    public HtmlNode withStyle(String key, String value) {
        styleMap.put(key, value);
        return this;
    }

    public HtmlNode withChild(Type childType, Consumer<HtmlNode> consumer) {
        HtmlNode child = new HtmlNode(childType);
        consumer.accept(child);
        children.add(child);
        return this;
    }

    public static HtmlNode ofChildren(Type type, Collection<HtmlNode> children) {
        HtmlNode node = new HtmlNode(type);
        node.children.addAll(children);
        return node;
    }

    public HtmlNode withAttribute(String key, String value) {
        attributes.put(key, value);
        return this;
    }

    private static String colorToCss(ChatColor color) {
        NamedTextColor namedTextColor = NamedTextColor.NAMES.value(color.name().toLowerCase());
        if (namedTextColor == null) throw new IllegalArgumentException("color=" + color.name());
        return namedTextColor.asHexString();
    }

    private HtmlNode withMinecraftStyle(String minecraftColorName, boolean value) {
        switch (minecraftColorName) {
        case "BOLD":
            return withStyle("font-weight", value ? "bolder" : "normal");
        case "UNDERLINE":
        case "UNDERLINED":
            return withStyle("text-decoration", value ? "underline" : "none");
        case "STRIKETHROUGH":
            return withStyle("text-decoration", value ? "line-through" : "none");
        case "ITALIC":
            return withStyle("font-style", value ? "italic" : "normal");
        default:
            System.err.println("Invalid style: " + minecraftColorName);
            return this;
        }
    }

    private static void legacyHelper(StringBuilder sb, List<HtmlNode> nodes, ChatColor color, Set<ChatColor> decorations) {
        if (sb.isEmpty()) return;
        HtmlNode node = HtmlNode.Type.SPAN.node()
            .withText(sb.toString());
        if (color != null) {
            node.withStyle("color", colorToCss(color));
        }
        for (ChatColor decor : decorations) {
            node.withMinecraftStyle(decor.name(), true);
        }
        nodes.add(node);
        sb.setLength(0);
    }

    public static HtmlNode fromLegacyText(String text) {
        List<HtmlNode> nodes = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        ChatColor color = null;
        Set<ChatColor> decorations = EnumSet.noneOf(ChatColor.class);
        for (int i = 0; i < text.length(); i += 1) {
            char c = text.charAt(i);
            if (c == '&') {
                char d = text.charAt(++i);
                ChatColor chatColor = ChatColor.getByChar(d);
                if (chatColor.isColor()) {
                    legacyHelper(sb, nodes, color, decorations);
                    color = chatColor;
                    decorations.clear();
                } else if (chatColor == ChatColor.RESET) {
                    legacyHelper(sb, nodes, color, decorations);
                    color = null;
                    decorations.clear();
                } else {
                    decorations.add(chatColor);
                }
            } else {
                sb.append(c);
            }
        }
        legacyHelper(sb, nodes, color, decorations);
        HtmlNode result = nodes.size() == 1
            ? nodes.get(0)
            : HtmlNode.ofChildren(Type.SPAN, nodes);
        return result;
    }

    /**
     * Recursive!
     */
    public static HtmlNode fromComponent(Component component) {
        HtmlNode result = HtmlNode.Type.SPAN.node();
        if (component instanceof TextComponent) {
            result.text = ((TextComponent) component).content();
        }
        Style style = component.style();
        for (TextDecoration textDecoration : TextDecoration.values()) {
            switch (style.decoration(textDecoration)) {
            case NOT_SET: break;
            case TRUE:
                result.withMinecraftStyle(textDecoration.name(), true);
                break;
            case FALSE:
                result.withMinecraftStyle(textDecoration.name(), false);
                break;
            default: break;
            }
        }
        TextColor color = style.color();
        if (color != null) {
            result.withStyle("color", color.asHexString());
        }
        for (Component extra : component.children()) {
            HtmlNode child = HtmlNode.fromComponent(extra);
            result.children.add(child);
        }
        return result;
    }
}
