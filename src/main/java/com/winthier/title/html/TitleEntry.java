package com.winthier.title.html;

import com.winthier.title.Title;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * A wrapper to aggregate html related data about a title.
 */
@Data @RequiredArgsConstructor
public final class TitleEntry {
    private final Title title;
    private HtmlNode titleHtml;

    public static TitleEntry of(Title title) {
        return new TitleEntry(title);
    }

    public HtmlNode getTitleHtml() {
        if (titleHtml == null) {
            titleHtml = computeTitleHtml();
        }
        return titleHtml;
    }

    public HtmlNode computeTitleHtml() {
        String json = title.getTitleJson();
        if (json != null && json.startsWith(":") && json.endsWith(":")) {
            String src = json.substring(1, json.length() - 1) + ".png";
            return HtmlNode.Type.IMG.node()
                .withAttribute("src", src)
                .withAttribute("alt", title.getName());
        } else if (json != null) {
            return HtmlNode.fromComponent(title.getTitleComponent());
        } else {
            return HtmlNode.fromLegacyText(title.getTitle());
        }
    }
}
