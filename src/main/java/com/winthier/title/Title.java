package com.winthier.title;

import cn.nukkit.utils.TextFormat;
import lombok.Value;

@Value
public final class Title {
    String name;
    String title;
    String description;

    public String formatted() {
        return TextFormat.colorize(title);
    }
}
