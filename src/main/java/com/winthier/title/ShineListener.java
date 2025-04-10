package com.winthier.title;

import com.cavetale.core.connect.NetworkServer;
import java.util.Objects;
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
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@RequiredArgsConstructor
public final class ShineListener implements Listener {
    private final TitlePlugin plugin;
    private NamespacedKey shineKey;
    private static final long COOLDOWN = 1000L;

    public ShineListener enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        shineKey = new NamespacedKey(plugin, "shine");
        return this;
    }

    private boolean shine(Player player, Location location, double scale) {
        Shine shine = plugin.getPlayerShine(player);
        if (shine == null) return false;
        ShinePlace.of(location, scale).show(shine);
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerTeleport(PlayerTeleportEvent event) {
        if (plugin.shinesDisabled) return;
        Player player = event.getPlayer();
        if (player.isInvisible()) return;
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (event.getCause() == TeleportCause.COMMAND) return;
        Location from = event.getFrom();
        final Location to = event.getTo();
        if (Objects.equals(from.getWorld(), to.getWorld())) {
            double distance = from.distanceSquared(to);
            if (distance < 4.0) return;
        }
        Shine shine = plugin.getPlayerShine(player);
        if (shine == null) return;
        // Check cooldown
        final Session session = plugin.findSession(player);
        if (session == null) return;
        final long now = System.currentTimeMillis();
        if (now - session.lastShineTime < COOLDOWN) return;
        session.lastShineTime = now;
        Bukkit.getScheduler().runTask(plugin, () -> {
                if (((Entity) player).isOnGround()) {
                    ShinePlace.of(to.clone().add(0, player.getEyeHeight(), 0), 2.0).show(shine);
                } else {
                    ShinePlace.of(to.clone().add(0, player.getEyeHeight() + 2.0, 0), 2.0).show(shine);
                }
            });
    }

    /**
     * Show shine on arrow hit.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onProjectileHit(ProjectileHitEvent event) {
        if (plugin.shinesDisabled) return;
        switch (NetworkServer.current()) {
        // Blinding shines in players' faces can be an unfair
        // advantage for the shooter
        case PVP_ARENA: return;
        case SURVIVAL_GAMES: return;
        case CAPTURE_THE_FLAG: return;
        default: break;
        }
        if (!(event.getEntity() instanceof AbstractArrow)) return;
        Projectile proj = event.getEntity();
        if (proj.getPersistentDataContainer().has(shineKey, PersistentDataType.BYTE)) return;
        proj.getPersistentDataContainer().set(shineKey, PersistentDataType.BYTE, (byte) 1);
        if (!(proj.getShooter() instanceof Player)) return;
        Player player = (Player) proj.getShooter();
        Shine shine = plugin.getPlayerShine(player);
        if (shine == null) return;
        // Check cooldown
        final Session session = plugin.findSession(player);
        if (session == null) return;
        final long now = System.currentTimeMillis();
        if (now - session.lastShineTime < COOLDOWN) return;
        Block block = event.getHitBlock();
        if (block != null) {
            BlockFace face = event.getHitBlockFace();
            if (face == null) return;
            Location location = block.getRelative(face).getLocation().add(0.5, 0.5, 0.5);
            Vector up;
            Vector right;
            if (face.getModY() == 0) {
                // Against wall
                up = new Vector(0.0, 1.0, 0.0);
                right = face.getDirection().normalize().rotateAroundY(Math.PI * 0.5);
                location.setDirection(face.getDirection());
            } else {
                // Horizontal, facing up or down
                up = new Vector(0.0, 0.0, -1.0);
                right = new Vector(1.0, 0.0, 0.0);
                location.setPitch(face.getModY() > 0 ? -90f : 90f);
                location.setYaw(player.getLocation().getYaw() + 180f);
            }
            session.lastShineTime = now;
            new ShinePlace(location, location, right, up, 2.0).show(shine);
            return;
        }
        Entity entity = event.getHitEntity();
        if (entity != null && !(entity instanceof Player)) {
            Vector right = entity.getLocation().toVector().subtract(proj.getLocation().toVector());
            right = right.setY(0);
            if (right.length() < 0.1) return;
            right = right.normalize().rotateAroundY(Math.PI * -0.5);
            Vector up = new Vector(0.0, 1.0, 0.0);
            Location location = proj.getLocation();
            session.lastShineTime = now;
            new ShinePlace(location, location, right, up, 2.0).show(shine);
            return;
        }
    }

    /**
     * Show shine while Elytra gliding.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerMove(PlayerMoveEvent event) {
        if (plugin.shinesDisabled) return;
        switch (NetworkServer.current()) {
        // Shine is too distracting in Elytra races
        case RACE: return;
        default: break;
        }
        final Player player = event.getPlayer();
        if (player.isInvisible()) return;
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
        if (!player.isGliding()) return;
        final Session session = plugin.findSession(player);
        if (session == null) return;
        Shine shine = plugin.getPlayerShine(player);
        if (shine == null) return;
        Location location = player.getEyeLocation();
        Vector vector = location.toVector();
        // Check cooldown
        final long now = System.currentTimeMillis();
        if (now - session.lastShineTime < COOLDOWN) return;
        // Check distance
        Vector lastFlyingShine = session.lastFlyingShine;
        if (lastFlyingShine != null && lastFlyingShine.distanceSquared(vector) < 256.0) {
            return;
        }
        session.lastFlyingShine = vector;
        session.lastShineTime = now;
        ShinePlace.of(location, 3.0).show(shine);
    }
}
