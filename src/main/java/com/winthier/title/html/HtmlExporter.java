package com.winthier.title.html;

import com.winthier.title.Msg;
import com.winthier.title.Title;
import com.winthier.title.TitleCategory;
import com.winthier.title.TitlePlugin;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public final class HtmlExporter {
    private final TitlePlugin plugin;
    private final CommandSender sender;
    @Getter private final Set<String> imageFiles = new HashSet<>();

    private void log(String msg) {
        sender.sendMessage("[Title] HTML: " + msg);
    }

    public void export() {
        List<Title> titles = plugin.getTitles();
        Map<TitleCategory, List<TitleEntry>> titlesMap = new EnumMap<>(TitleCategory.class);
        for (Title title : titles) {
            titlesMap.computeIfAbsent(title.parseCategory(), c -> new ArrayList<>()).add(TitleEntry.of(title));
        }
        HtmlNode rootNode = HtmlNode.Type.HTML.node();
        rootNode.withChild(HtmlNode.Type.HEADER, header -> {
                header.withChild(HtmlNode.Type.META, meta -> meta.withAttribute("charset", "UTF-8"));
                header.withChild(HtmlNode.Type.TITLE, title -> title.withText("Titles"));
                header.withChild(HtmlNode.Type.STYLE, style -> {
                        style.setText(".mc{"
                                      + "font-family:'Courier New',monospace;"
                                      + "font-size:16px;"
                                      + "color:#FFF;"
                                      + "display:inline-block;"
                                      + "white-space:pre;"
                                      + "letter-spacing: 1px;"
                                      + "padding: 0.5px;"
                                      + "}"
                                      + "td{padding-right:20px;}"
                                      + "document,body{background-color:#113;color:#fff;}"
                                      + "body{width:800px;max-width:800px;margin:auto;}"
                                      + "table{width:100%;}"
                                      + ".td-title{width:240px;text-align:right;}"
                                      + ".td-desc{width:560px;}"
                                      + "h2,h3{text-align:middle;}"
                                      + "a{color:#fff;}"
                                      + "a:visited{color:#fff;}");
                    });
            });
        rootNode.withChild(HtmlNode.Type.BODY, body -> {
                for (TitleCategory.Group group : TitleCategory.Group.values()) {
                    body.withChild(HtmlNode.Type.H2, h2 -> {
                            h2.withChild(HtmlNode.Type.A, a -> {
                                    a.withText(Msg.toCamelCase(group));
                                    a.withAttribute("id", group.name().toLowerCase());
                                    a.withAttribute("href", "#" + group.name().toLowerCase());
                                });
                        });
                    for (TitleCategory category : TitleCategory.values()) {
                        if (category.group != group) continue;
                        List<TitleEntry> entries = titlesMap.getOrDefault(category, List.of());
                        if (entries.isEmpty()) continue;
                        Collections.sort(entries, (b, a) -> Integer.compare(a.title.getPriority(),
                                                                            b.title.getPriority()));
                        body.withChild(HtmlNode.Type.H3, h3 -> {
                                h3.withChild(HtmlNode.Type.A, a -> {
                                        a.withText(Msg.toCamelCase(category));
                                        a.withAttribute("id", category.name().toLowerCase());
                                        a.withAttribute("href", "#" + category.name().toLowerCase());
                                    });
                            });
                        body.withChild(HtmlNode.Type.TABLE, table -> {
                                for (TitleEntry entry : entries) {
                                    table.withChild(HtmlNode.Type.TR, tr -> {
                                            tr.withChild(HtmlNode.Type.TD, td -> {
                                                    td.withAttribute("class", "td-title");
                                                    td.getChildren().add(entry.getTitleHtml());
                                                    imageFiles.addAll(entry.imageFiles);
                                                });
                                            tr.withChild(HtmlNode.Type.TD, td -> {
                                                    td.withAttribute("class", "td-desc");
                                                    td.withText(entry.title.getDescription());
                                                });
                                        });
                                }
                            });
                    }
                }
            });
        try {
            plugin.getDataFolder().mkdirs();
            try (PrintStream out = new PrintStream(new File(plugin.getDataFolder(), "index.html"))) {
                out.println("<!doctype html>");
                rootNode.print(out);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
