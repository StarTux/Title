package com.winthier.title;

import com.cavetale.mytems.Mytems;
import net.kyori.adventure.text.Component;

public final class MytemsHandler {
    public Component forKey(String key) {
        Mytems mytems = Mytems.forId(key);
        if (mytems == null) {
            TitlePlugin.getInstance().getLogger().warning("Mytems not found: " + key);
            return Component.empty();
        }
        return mytems.component;
    }
}
