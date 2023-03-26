package com.winthier.title;

import com.cavetale.mytems.Mytems;
import com.winthier.title.sql.SQLSuffix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

/**
 * The cache stores all required information to build for the session.
 * - displayName
 * - playerListName
 * - playerTagPrefix
 * - playerTagSuffix
 */
public final class Cache {
    protected Title title;
    protected SQLSuffix suffix;
    protected Component playerListPrefix;

    protected Mytems titlePrefixMytems; // animated
    protected Component titlePrefixComponent;
    protected Component titlePrefixTooltip;

    protected String playerName;
    protected TextEffect textEffect; // Custom
    protected TextColor textColor; // Adventure

    protected Mytems titleSuffixMytems; // animated
    protected Component titleSuffixComponent;

    protected Component playerListSuffix;
}
