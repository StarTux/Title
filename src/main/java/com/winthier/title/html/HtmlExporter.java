package com.winthier.title.html;

import com.winthier.title.Title;
import com.winthier.title.TitlePlugin;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public final class HtmlExporter {
    private final TitlePlugin plugin;
    private final CommandSender sender;

    private void log(String msg) {
        sender.sendMessage("[Title] HTML: " + msg);
    }

    public void export() {
        List<Title> titles = plugin.getDb().listTitles();
        List<TitleEntry> entries = new ArrayList<>(titles.size());
        for (Title title : titles) {
            entries.add(TitleEntry.of(title));
        }
        HtmlNode rootNode = HtmlNode.Type.HTML.node();
        rootNode.withChild(HtmlNode.Type.HEADER, header -> {
                header.withChild(HtmlNode.Type.META, meta -> meta.withAttribute("charset", "UTF-8"));
                header.withChild(HtmlNode.Type.TITLE, title -> title.withText("Titles"));
                header.withChild(HtmlNode.Type.STYLE, style -> {
                        style.setText("html,body{background-color:#303030;color:white;font-family:monospace;}");
                    });
            });
        rootNode.withChild(HtmlNode.Type.BODY, body -> {
                body.withChild(HtmlNode.Type.UL, titleList -> {
                        for (TitleEntry entry : entries) {
                            titleList.withChild(HtmlNode.Type.LI, titleListEntry -> {
                                    titleListEntry.getChildren().add(entry.getTitleHtml());
                                });
                        }
                    });
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
