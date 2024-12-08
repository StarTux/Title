package com.winthier.title;

import com.cavetale.core.menu.MenuItemEvent;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import static com.cavetale.mytems.util.Items.tooltip;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class MenuListener implements Listener {
    protected void enable() {
        Bukkit.getPluginManager().registerEvents(this, TitlePlugin.getInstance());
    }

    @EventHandler
    private void onMenuItem(MenuItemEvent event) {
        if (event.getPlayer().hasPermission("title.title")) {
            event.addItem(builder -> builder
                          .key("title:title")
                          .command("title")
                          .icon(tooltip(new ItemStack(Material.NAME_TAG),
                                        List.of(text("Titles", DARK_AQUA)))));
        }
    }
}
