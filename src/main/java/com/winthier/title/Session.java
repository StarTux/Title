package com.winthier.title;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.util.Vector;

/**
 * A session is kept alive as long as a player stays logged in.
 */
public final class Session {
    protected Component playerListPrefix = null;
    protected Component playerListSuffix = null;
    protected Vector lastFlyingShine;
    protected Component teamPrefix = Component.empty();
    protected Component teamSuffix = Component.empty();
    protected NamedTextColor teamColor = NamedTextColor.WHITE;
}
