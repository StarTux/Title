package com.winthier.title;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public final class ShineListener implements Listener {
    private final TitlePlugin plugin;
    private NamespacedKey shineKey;
    private Map<UUID, Vector> lastFlyShines = new HashMap<>();

    public ShineListener enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        shineKey = new NamespacedKey(plugin, "shine");
        return this;
    }

    boolean shine(Player player, Location location, double scale) {
        Title title = plugin.getDb().getCachedTitle(player.getUniqueId());
        if (title == null || title.getShine() == null) return false;
        Shine shine;
        try {
            shine = Shine.valueOf(title.getShine().toUpperCase());
        } catch (IllegalArgumentException iae) {
            return false;
        }
        ShinePlace.of(location, scale).show(shine);
        return true;
    }

    Shine getShine(Player player) {
        Title title = plugin.getDb().getCachedTitle(player.getUniqueId());
        if (title == null || title.getShine() == null) return null;
        Shine shine;
        try {
            return Shine.valueOf(title.getShine().toUpperCase());
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        Shine shine = getShine(player);
        if (shine == null) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnGround()) {
                    ShinePlace.of(player.getEyeLocation(), 2.0).show(shine);
                } else {
                    ShinePlace.of(player.getEyeLocation().add(0.0, 2.0, 0.0), 2.0).show(shine);
                }
            });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow)) return;
        Projectile proj = event.getEntity();
        if (proj.getPersistentDataContainer().has(shineKey, PersistentDataType.BYTE)) return;
        proj.getPersistentDataContainer().set(shineKey, PersistentDataType.BYTE, (byte) 1);
        if (!(proj.getShooter() instanceof Player)) return;
        Player player = (Player) proj.getShooter();
        Shine shine = getShine(player);
        if (shine == null) return;
        Block block = event.getHitBlock();
        if (block != null) {
            BlockFace face = event.getHitBlockFace();
            if (face == null) return;
            Location location = block.getRelative(face).getLocation().add(0.5, 0.5, 0.5);
            Vector up;
            Vector right;
            if (face.getModY() == 0) {
                up = new Vector(0.0, 1.0, 0.0);
                right = face.getDirection().normalize().rotateAroundY(Math.PI * -0.5);
            } else {
                up = new Vector(0.0, 0.0, -1.0);
                right = new Vector(1.0, 0.0, 0.0);
            }
            new ShinePlace(location, right, up, 2.0).show(shine);
            return;
        }
        Entity entity = event.getHitEntity();
        if (entity != null) {
            Vector right = entity.getLocation().toVector().subtract(proj.getLocation().toVector());
            right = right.setY(0);
            if (right.length() < 0.1) return;
            right = right.normalize().rotateAroundY(Math.PI * -0.5);
            Vector up = new Vector(0.0, 1.0, 0.0);
            new ShinePlace(proj.getLocation(), right, up, 2.0).show(shine);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isGliding()) return;
        Shine shine = getShine(player);
        if (shine == null) return;
        Location location = player.getLocation();
        Vector vector = location.toVector();
        Vector lastFlyShine = lastFlyShines.get(player.getUniqueId());
        if (lastFlyShine != null && lastFlyShine.distanceSquared(vector) < 64.0) {
            return;
        }
        lastFlyShines.put(player.getUniqueId(), vector);
        ShinePlace.of(location, 3.0).show(shine);
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event) {
        lastFlyShines.remove(event.getPlayer().getUniqueId());
    }
}
