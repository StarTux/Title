package com.winthier.title.html;

import com.winthier.title.Title;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * A wrapper to aggregate html related data about a title.
 */
@Data @RequiredArgsConstructor
public final class TitleEntry {
    protected final Title title;
    protected HtmlNode titleHtml;
    protected List<String> imageFiles = new ArrayList<>();

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
            imageFiles.add(src);
            return HtmlNode.Type.IMG.node()
                .withAttribute("src", src)
                .withAttribute("alt", title.getName());
        } else if (json != null) {
            HtmlNode result = HtmlNode.fromComponent(title.getTitleComponent());
            result.getAttributes().put("class", "mc");
            return result;
        } else {
            HtmlNode result = HtmlNode.fromLegacyText(title.getTitle());
            result.getAttributes().put("class", "mc");
            return result;
        }
    }
}
