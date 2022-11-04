package com.winthier.title.html;

import com.winthier.title.Msg;
import com.winthier.title.Title;
import com.winthier.title.TitleCategory;
import com.winthier.title.TitleGroup;
import com.winthier.title.TitlePlugin;
import com.winthier.title.sql.UnlockedInfo;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
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
        Map<String, Integer> unlockedMap = new HashMap<>();
        for (UnlockedInfo row : plugin.getDb().find(UnlockedInfo.class).findList()) {
            unlockedMap.compute(row.getTitle(), (n, i) -> i != null ? i + 1 : 1);
        }
        Map<TitleCategory, List<TitleEntry>> titlesMap = new EnumMap<>(TitleCategory.class);
        for (Title title : titles) {
            TitleCategory category = title.parseCategory();
            if (category == TitleCategory.HIDDEN) continue;
            titlesMap.computeIfAbsent(category, c -> new ArrayList<>())
                .add(TitleEntry.of(title, unlockedMap.getOrDefault(title.getName(), 0)));
        }
        HtmlNode rootNode = HtmlNode.Type.HTML.node();
        rootNode.withChild(HtmlNode.Type.HEADER, header -> {
                header.withChild(HtmlNode.Type.META, meta -> meta.withAttribute("charset", "UTF-8"));
                header.withChild(HtmlNode.Type.TITLE, title -> title.withText("Titles"));
                header.withChild(HtmlNode.Type.STYLE, style -> {
                        style.setText(".mc{"
                                      + "font-family:'Courier New',monospace;"
                                      + "font-size:22px;"
                                      + "color:#FFF;"
                                      + "display:inline-block;"
                                      + "white-space:pre;"
                                      + "letter-spacing: 1px;"
                                      + "padding: 0.75px;"
                                      + "}"
                                      + "document,body{background-color:#113;color:#fff;}"
                                      + "body{width:800px;max-width:800px;margin:auto;}"
                                      + "tr,td{margin:0;padding:0;border:0;}"
                                      + "table{width:780px;margin-left:20px;padding:0;border:0}"
                                      + ".td-title{width:240px;max-width:260px;}"
                                      + ".td-count{width:40px;max-width:60px;text-align:right;padding-right:10px;}"
                                      + ".td-count{color:#AAA;font-family:monospace;}"
                                      + ".td-desc{width:500px;max-width:480px;}"
                                      + ".td-title{-webkit-text-stroke-width:0.5px;}"
                                      + ".td-title{-webkit-text-stroke-color:#888;}"
                                      + ".td-title{font-weight:bolder;}"
                                      + "body{font-family:sans;}"
                                      + ".td-desc{font-size:16px;}"
                                      + ".td-desc{width:580px;}"
                                      + "h2{font-size:26;}"
                                      + "h3{font-size:24;}"
                                      + "a::before{content:'#';}"
                                      + "a{color:#BBB;text-decoration:none;}");
                    });
            });
        rootNode.withChild(HtmlNode.Type.BODY, body -> {
                for (TitleGroup group : TitleGroup.values()) {
                    if (group == TitleGroup.UNKNOWN) continue;
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
                                                    td.withAttribute("class", "td-count");
                                                    if (entry.getPlayerCount() > 0) {
                                                        td.withText("" + entry.getPlayerCount());
                                                    }
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
