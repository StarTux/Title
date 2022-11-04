package com.winthier.title;

import static com.cavetale.core.util.CamelCase.toCamelCase;

public enum TitleGroup {
    RANK,
    STORE,
    SEASONAL,
    EVENT,
    OTHER,
    UNKNOWN;

    public String getDisplayName() {
        return toCamelCase(" ", this);
    }
}
