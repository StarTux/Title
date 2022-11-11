package com.winthier.title;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.Value;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@Value
public final class ShinePlace {
    private final Location eye;
    private final Location offset;
    private final Vector right;
    private final Vector up;
    private final double scale;
    static Random random = new Random();
    protected static final Set<UUID> ENTITIES = new HashSet<>();

    public static ShinePlace of(Location eye, double scale) {
        return of(eye, new Vector(0, 0, 0), scale);
    }

    public static ShinePlace of(Location eye, Vector add, double scale) {
        Location location = eye.clone();
        location.setPitch(0f);
        Vector right = location.getDirection()
            .normalize()
            .rotateAroundY(Math.PI * 0.5);
        Vector up = new Vector(0, 1, 0);
        return new ShinePlace(eye, location.add(add), right, up, scale);
    }

    public Vector right() {
        return right.clone();
    }

    public Vector up() {
        return up.clone();
    }

    public void show(Shine shine) {
        switch (shine) {
        case COPPER_COIN:
        case SILVER_COIN:
        case GOLDEN_COIN:
        case DIAMOND_COIN:
        case RUBY_COIN:
            final Item entity = eye.getWorld().dropItem(eye, shine.mytems.createIcon(), item -> {
                    item.setPersistent(false);
                    item.setCanMobPickup(false);
                    item.setCanPlayerPickup(false);
                    item.setPersistent(false);
                    item.setOwner(java.util.UUID.randomUUID());
                    item.setPickupDelay(32767);
                    item.setGlowing(true);
                    item.setInvulnerable(true);
                });
            if (entity == null) return;
            final UUID uuid = entity.getUniqueId();
            ENTITIES.add(uuid);
            new BukkitRunnable() {
                int ticks = 0;

                @Override public void run() {
                    if (entity == null || entity.isDead()) {
                        cancel();
                        ENTITIES.remove(uuid);
                        return;
                    }
                    if (ticks > 100) {
                        entity.remove();
                        return;
                    }
                    if (ticks % 10 == 0) {
                        entity.getWorld().spawnParticle(Particle.REDSTONE, entity.getLocation().add(0.0, 0.25, 0.0),
                                                        8, 0.125, 0.125, 0.125, 0.0,
                                                        new Particle.DustOptions(Color.fromRGB(shine.hex), 0.75f));
                    }
                    ticks += 1;
                }
            }.runTaskTimer(TitlePlugin.getInstance(), 1L, 1L);
            break;
        case MOON: {
            double[][] points = {
                {-0.20, 1.00}, {-0.07, 1.00}, {0.07, 1.00}, {0.20,
                1.00}, {0.33, 1.00}, {-0.60, 0.87}, {-0.47, 0.87},
                {0.47, 0.87}, {0.60, 0.87}, {-0.73, 0.73}, {0.73,
                0.73}, {-0.87, 0.60}, {0.87, 0.60}, {-0.87, 0.47},
                {0.20, 0.47}, {0.33, 0.47}, {0.47, 0.47}, {0.60,
                0.47}, {0.87, 0.47}, {-1.00, 0.33}, {0.07, 0.33},
                {0.73, 0.33}, {0.87, 0.33}, {-1.00, 0.20}, {-0.07,
                0.20}, {-1.00, 0.07}, {-0.07, 0.07}, {-1.00, -0.07},
                {-0.07, -0.07}, {-1.00, -0.20}, {-0.07, -0.20},
                {-1.00, -0.33}, {0.07, -0.33}, {0.73, -0.33}, {0.87,
                -0.33}, {-0.87, -0.47}, {0.20, -0.47}, {0.33, -0.47},
                {0.47, -0.47}, {0.60, -0.47}, {0.87, -0.47}, {-0.87,
                -0.60}, {0.87, -0.60}, {-0.73, -0.73}, {0.73, -0.73},
                {-0.60, -0.87}, {-0.47, -0.87}, {0.47, -0.87}, {0.60,
                -0.87}, {-0.33, -1.00}, {-0.20, -1.00}, {-0.07,
                -1.00}, {0.07, -1.00}, {0.20, -1.00}, {0.33, -1.00}
            };
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(128, 255, 0), 2.0f);
            for (double[] p : points) {
                showParticle(Particle.REDSTONE, dust, 2, p[0] * 1.25, p[1] * 1.25);
            }
            break;
        }
        case SKULL: {
            double[][] points = {
                {-0.43, 1.00}, {-0.29, 1.00}, {-0.14, 1.00}, {0.00,
                1.00}, {0.14, 1.00}, {0.29, 1.00}, {0.43, 1.00},
                {-0.71, 0.86}, {-0.57, 0.86}, {0.57, 0.86}, {0.71,
                0.86}, {-0.86, 0.71}, {0.86, 0.71}, {-0.86, 0.57},
                {0.86, 0.57}, {-1.00, 0.43}, {1.00, 0.43}, {-1.00,
                0.29}, {1.00, 0.29}, {-1.00, 0.14}, {-0.43, 0.14},
                {-0.29, 0.14}, {0.29, 0.14}, {0.43, 0.14}, {1.00,
                0.14}, {-1.00, 0.00}, {-0.57, 0.00}, {-0.43, 0.00},
                {-0.29, 0.00}, {-0.14, 0.00}, {0.14, 0.00}, {0.29,
                0.00}, {0.43, 0.00}, {0.57, 0.00}, {1.00, 0.00},
                {-1.00, -0.14}, {-0.57, -0.14}, {-0.43, -0.14},
                {-0.29, -0.14}, {-0.14, -0.14}, {0.14, -0.14}, {0.29,
                -0.14}, {0.43, -0.14}, {0.57, -0.14}, {1.00, -0.14},
                {-1.00, -0.29}, {-0.43, -0.29}, {-0.29, -0.29}, {0.29,
                -0.29}, {0.43, -0.29}, {1.00, -0.29}, {-0.86, -0.43},
                {0.00, -0.43}, {0.86, -0.43}, {-0.71, -0.57}, {-0.57,
                -0.57}, {-0.14, -0.57}, {0.00, -0.57}, {0.14, -0.57},
                {0.57, -0.57}, {0.71, -0.57}, {-0.43, -0.71}, {0.43,
                -0.71}, {-0.43, -0.86}, {-0.14, -0.86}, {0.14, -0.86},
                {0.43, -0.86}, {-0.43, -1.00}, {-0.29, -1.00}, {-0.14,
                -1.00}, {0.00, -1.00}, {0.14, -1.00}, {0.29, -1.00},
                {0.43, -1.00}
            };
            for (double[] p : points) {
                showParticle(Particle.WAX_OFF, null, 1, p[0], p[1]);
            }
            break;
        }
        case PUMPKIN: {
            double[][] points = {
                {0.07, 1.00}, {0.20, 1.00}, {-0.07, 0.87}, {0.33,
                0.87}, {-0.20, 0.73}, {0.20, 0.73}, {-0.60, 0.60},
                {-0.47, 0.60}, {-0.33, 0.60}, {0.33, 0.60}, {0.47,
                0.60}, {0.60, 0.60}, {-0.73, 0.47}, {0.73, 0.47},
                {-0.87, 0.33}, {0.87, 0.33}, {-1.00, 0.20}, {1.00,
                0.20}, {-1.00, 0.07}, {1.00, 0.07}, {-1.00, -0.07},
                {-0.07, -0.07}, {0.07, -0.07}, {1.00, -0.07}, {-1.00,
                -0.20}, {-0.07, -0.20}, {1.00, -0.20}, {-1.00, -0.33},
                {-0.47, -0.33}, {-0.33, -0.33}, {0.33, -0.33}, {0.47,
                -0.33}, {1.00, -0.33}, {-1.00, -0.47}, {-0.33, -0.47},
                {-0.20, -0.47}, {-0.07, -0.47}, {0.07, -0.47}, {0.20,
                -0.47}, {0.33, -0.47}, {1.00, -0.47}, {-0.87, -0.60},
                {-0.20, -0.60}, {-0.07, -0.60}, {0.20, -0.60}, {0.87,
                -0.60}, {-0.87, -0.73}, {0.87, -0.73}, {-0.73, -0.87},
                {-0.60, -0.87}, {0.73, -0.87}, {-0.60, -1.00}, {-0.47,
                -1.00}, {-0.33, -1.00}, {-0.20, -1.00}, {-0.07,
                -1.00}, {0.07, -1.00}, {0.20, -1.00}, {0.33, -1.00},
                {0.47, -1.00}, {0.60, -1.00}
            };
            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0xFF8000), 1.0f);
            for (double[] p : points) {
                showParticle(Particle.REDSTONE, dustOptions, 2, p[0], p[1]);
            }
            double[][] eyes = {
                {-0.33, 0.33}, {0.33, 0.33}, {-0.47, 0.20}, {-0.33,
                0.20}, {-0.20, 0.20}, {0.20, 0.20}, {0.33, 0.20},
                {0.47, 0.20}, {-0.47, 0.07}, {-0.33, 0.07}, {-0.20,
                0.07}, {0.20, 0.07}, {0.33, 0.07}, {0.47, 0.07}
            };
            for (double[] p : eyes) {
                showParticle(Particle.FLAME, null, 1, p[0], p[1]);
            }
            break;
        }
        case GOAT: {
            double[][] points = {
                {-0.92, 0.23}, {-0.85, 0.23}, {-0.77, 0.23}, {-0.69,
                0.23}, {-0.38, 0.23}, {-0.31, 0.23}, {-0.23, 0.23},
                {-0.15, 0.23}, {0.15, 0.23}, {0.23, 0.23}, {0.31,
                0.23}, {0.38, 0.23}, {0.62, 0.23}, {0.69, 0.23},
                {0.77, 0.23}, {0.85, 0.23}, {0.92, 0.23}, {1.00,
                0.23}, {-1.00, 0.15}, {-0.92, 0.15}, {-0.69, 0.15},
                {-0.62, 0.15}, {-0.46, 0.15}, {-0.38, 0.15}, {-0.15,
                0.15}, {-0.08, 0.15}, {0.08, 0.15}, {0.15, 0.15},
                {0.38, 0.15}, {0.46, 0.15}, {0.77, 0.15}, {0.85,
                0.15}, {-1.00, 0.08}, {-0.92, 0.08}, {-0.46, 0.08},
                {-0.38, 0.08}, {-0.15, 0.08}, {-0.08, 0.08}, {0.08,
                0.08}, {0.15, 0.08}, {0.38, 0.08}, {0.46, 0.08},
                {0.77, 0.08}, {0.85, 0.08}, {-1.00, 0.00}, {-0.92,
                0.00}, {-0.77, 0.00}, {-0.69, 0.00}, {-0.62, 0.00},
                {-0.46, 0.00}, {-0.38, 0.00}, {-0.15, 0.00}, {-0.08,
                0.00}, {0.08, 0.00}, {0.15, 0.00}, {0.23, 0.00},
                {0.31, 0.00}, {0.38, 0.00}, {0.46, 0.00}, {0.77,
                0.00}, {0.85, 0.00}, {-1.00, -0.08}, {-0.92, -0.08},
                {-0.69, -0.08}, {-0.62, -0.08}, {-0.46, -0.08},
                {-0.38, -0.08}, {-0.15, -0.08}, {-0.08, -0.08}, {0.08,
                -0.08}, {0.15, -0.08}, {0.38, -0.08}, {0.46, -0.08},
                {0.77, -0.08}, {0.85, -0.08}, {-0.92, -0.15}, {-0.85,
                -0.15}, {-0.77, -0.15}, {-0.69, -0.15}, {-0.38,
                -0.15}, {-0.31, -0.15}, {-0.23, -0.15}, {-0.15,
                -0.15}, {0.08, -0.15}, {0.15, -0.15}, {0.38, -0.15},
                {0.46, -0.15}, {0.77, -0.15}, {0.85, -0.15}
            };
            for (double[] p : points) {
                showParticle(Particle.FALLING_DUST, Material.GOLD_BLOCK.createBlockData(), 4, 0.05, p[0] * 1.8, p[1] * 1.8);
            }
            break;
        }
        case EAGLE: {
            double[][] points = {
                {-0.22, 0.91}, {-0.13, 0.91}, {-0.04, 0.91}, {0.04,
                0.91}, {0.13, 0.91}, {0.22, 0.91}, {-0.48, 0.83},
                {-0.39, 0.83}, {-0.30, 0.83}, {0.30, 0.83}, {0.39,
                0.83}, {0.48, 0.83}, {0.57, 0.83}, {-0.57, 0.74},
                {0.65, 0.74}, {-0.57, 0.65}, {0.74, 0.65}, {-0.83,
                0.57}, {-0.74, 0.57}, {-0.65, 0.57}, {-0.13, 0.57},
                {-0.04, 0.57}, {0.65, 0.57}, {-0.91, 0.48}, {-0.22,
                0.48}, {0.04, 0.48}, {0.74, 0.48}, {-1.00, 0.39},
                {-0.13, 0.39}, {-0.04, 0.39}, {0.74, 0.39}, {-1.00,
                0.30}, {0.83, 0.30}, {-1.00, 0.22}, {-0.74, 0.22},
                {-0.65, 0.22}, {0.83, 0.22}, {-0.91, 0.13}, {-0.74,
                0.13}, {-0.57, 0.13}, {-0.48, 0.13}, {-0.39, 0.13},
                {0.83, 0.13}, {-0.83, 0.04}, {-0.30, 0.04}, {0.83,
                0.04}, {-0.30, -0.04}, {0.83, -0.04}, {-0.39, -0.13},
                {0.83, -0.13}, {-0.39, -0.22}, {0.83, -0.22}, {-0.48,
                -0.30}, {0.83, -0.30}, {-0.57, -0.39}, {0.91, -0.39},
                {-0.65, -0.48}, {0.91, -0.48}, {-0.65, -0.57}, {0.91,
                -0.57}, {-0.65, -0.65}, {0.91, -0.65}, {-0.74, -0.74},
                {0.74, -0.74}, {0.91, -0.74}, {-0.74, -0.83}, {-0.57,
                -0.83}, {-0.39, -0.83}, {-0.30, -0.83}, {0.83, -0.83},
                {-0.57, -0.91}, {-0.48, -0.91}, {-0.22, -0.91}, {0.04,
                -0.91}, {0.30, -0.91}, {0.39, -0.91}, {0.57, -0.91},
                {0.65, -0.91}, {-0.13, -1.00}, {-0.04, -1.00}, {0.22,
                -1.00}
            };
            for (double[] p : points) {
                showParticle(Particle.END_ROD, null, 1, p[0] * 1.2, p[1] * 1.2);
            }
            break;
        }
        case YINYANG: {
            double[][] yin = {
                {0.33, 1.00}, {0.50, 0.83}, {0.67, 0.83}, {0.67,
                0.67}, {0.83, 0.67}, {0.00, 0.50}, {0.67, 0.50},
                {0.83, 0.50}, {0.67, 0.33}, {1.00, 0.33}, {0.50,
                0.17}, {1.00, 0.17}, {0.00, 0.00}, {0.17, 0.00},
                {0.33, 0.00}, {1.00, 0.00}, {-0.33, -0.17}, {-0.17,
                -0.17}, {1.00, -0.17}, {-0.50, -0.33}, {1.00, -0.33},
                {-0.50, -0.50}, {0.83, -0.50}, {-0.50, -0.67}, {0.83,
                -0.67}, {-0.33, -0.83}, {0.50, -0.83}, {0.67, -0.83},
                {-0.17, -1.00}, {0.00, -1.00}, {0.17, -1.00}, {0.33,
                -1.00}
            };
            double[][] yang = {
                {-0.33, 1.00}, {-0.17, 1.00}, {0.00, 1.00}, {0.17,
                1.00}, {-0.67, 0.83}, {-0.50, 0.83}, {0.33, 0.83},
                {-0.83, 0.67}, {0.50, 0.67}, {-0.83, 0.50}, {0.50,
                0.50}, {-1.00, 0.33}, {0.50, 0.33}, {-1.00, 0.17},
                {0.17, 0.17}, {0.33, 0.17}, {-1.00, 0.00}, {-0.33,
                0.00}, {-0.17, 0.00}, {0.00, 0.00}, {-1.00, -0.17},
                {-0.50, -0.17}, {-1.00, -0.33}, {-0.67, -0.33},
                {-0.83, -0.50}, {-0.67, -0.50}, {0.00, -0.50}, {-0.83,
                -0.67}, {-0.67, -0.67}, {-0.67, -0.83}, {-0.50,
                -0.83}, {-0.33, -1.00}
            };
            Particle.DustOptions da = new Particle.DustOptions(Color.fromRGB(0xffffff), 2.0f);
            Particle.DustOptions db = new Particle.DustOptions(Color.fromRGB(0x000000), 2.0f);
            for (double[] p : yin) {
                showParticle(Particle.REDSTONE, da, 2, p[0], p[1]);
            }
            for (double[] p : yang) {
                showParticle(Particle.REDSTONE, db, 2, p[0], p[1]);
            }
            break;
        }
        case EARTH: {
            double[][] circle = {
                {1.00, 0.00}, {0.98, 0.17}, {0.94, 0.34}, {0.87, 0.50}, {0.77, 0.64}, {0.64, 0.77},
                {0.50, 0.87}, {0.34, 0.94}, {0.17, 0.98}, {0.00, 1.00}, {-0.17, 0.98}, {-0.34, 0.94},
                {-0.50, 0.87}, {-0.64, 0.77}, {-0.77, 0.64}, {-0.87, 0.50}, {-0.94, 0.34},
                {-0.98, 0.17}, {-1.00, 0.00}, {-0.98, -0.17}, {-0.94, -0.34}, {-0.87, -0.50},
                {-0.77, -0.64}, {-0.64, -0.77}, {-0.50, -0.87}, {-0.34, -0.94}, {-0.17, -0.98},
                {-0.00, -1.00}, {0.17, -0.98}, {0.34, -0.94}, {0.50, -0.87}, {0.64, -0.77},
                {0.77, -0.64}, {0.87, -0.50}, {0.94, -0.34}, {0.98, -0.17}
            };
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(0x4169E1), 2.0f);
            Particle.DustOptions dust2 = new Particle.DustOptions(Color.fromRGB(0x228B22), 2.0f);
            for (double[] p : circle) {
                if (random.nextInt(2) > 0) {
                    showParticle(Particle.REDSTONE, dust, 2, p[0], p[1]);
                } else {
                    showParticle(Particle.REDSTONE, dust2, 2, p[0], p[1]);
                }
            }
            break;
        }
        case EGG: {
            double[][] shape = {
                {-0.33, 1.00}, {-0.11, 1.00}, {0.11, 1.00}, {0.33, 1.00}, {-0.56, 0.80},
                {0.56, 0.80}, {-0.78, 0.60}, {0.78, 0.60}, {-0.78, 0.40}, {0.78, 0.40},
                {-1.00, 0.20}, {1.00, 0.20}, {-1.00, 0.00}, {1.00, 0.00}, {-1.00, -0.20},
                {1.00, -0.20}, {-1.00, -0.40}, {1.00, -0.40}, {-1.00, -0.60}, {1.00, -0.60},
                {-0.78, -0.80}, {0.78, -0.80}, {-0.56, -1.00}, {-0.33, -1.00}, {-0.11, -1.00},
                {0.11, -1.00}, {0.33, -1.00}, {0.56, -1.00}
            };
            int rgb = 0xFFFFFF & java.awt.Color.HSBtoRGB(random.nextFloat(), 1.0f, 1.0f);
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(rgb), 2.0f);
            for (double[] p : shape) {
                showParticle(Particle.REDSTONE, dust, 2, 0.8 * p[0], 0.8 * p[1]);
            }
            break;
        }
        case BUNNY: {
            double[][] shape = {
                {-0.60, 1.00}, {-0.40, 1.00}, {0.40, 1.00}, {0.60, 1.00}, {-0.80, 0.87},
                {-0.20, 0.87}, {0.20, 0.87}, {0.80, 0.87}, {-0.80, 0.73}, {-0.20, 0.73},
                {0.20, 0.73}, {0.80, 0.73}, {-0.80, 0.60}, {-0.20, 0.60}, {0.20, 0.60}, {0.80, 0.60},
                {-0.80, 0.47}, {-0.20, 0.47}, {0.20, 0.47}, {0.80, 0.47}, {-0.80, 0.33},
                {-0.20, 0.33}, {0.20, 0.33}, {0.80, 0.33}, {-0.80, 0.20}, {-0.40, 0.20},
                {-0.20, 0.20}, {0.00, 0.20}, {0.20, 0.20}, {0.40, 0.20}, {0.80, 0.20}, {-0.80, 0.07},
                {0.80, 0.07}, {-1.00, -0.07}, {1.00, -0.07}, {-1.00, -0.20}, {1.00, -0.20},
                {-1.00, -0.33}, {-0.40, -0.33}, {0.40, -0.33}, {1.00, -0.33}, {-1.00, -0.47},
                {-0.40, -0.47}, {0.40, -0.47}, {1.00, -0.47}, {-1.00, -0.60}, {1.00, -0.60},
                {-1.00, -0.73}, {1.00, -0.73}, {-0.80, -0.87}, {-0.60, -0.87}, {0.60, -0.87},
                {0.80, -0.87}, {-0.40, -1.00}, {-0.20, -1.00}, {0.00, -1.00}, {0.20, -1.00},
                {0.40, -1.00}
            };
            int rgb = 0xFFFFFF & java.awt.Color.HSBtoRGB(random.nextFloat(), 1.0f, 1.0f);
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(rgb), 2.0f);
            for (double[] p : shape) {
                showParticle(Particle.REDSTONE, dust, 2, p[0], p[1]);
            }
            break;
        }
        case COIN: {
            double[][] shape = {
                {-0.27, 1.00}, {-0.09, 1.00}, {0.09, 1.00}, {0.27, 1.00}, {-0.64, 0.82},
                {-0.45, 0.82}, {-0.27, 0.82}, {-0.09, 0.82}, {0.09, 0.82}, {0.27, 0.82},
                {0.45, 0.82}, {0.64, 0.82}, {-0.82, 0.64}, {-0.64, 0.64}, {-0.45, 0.64},
                {-0.27, 0.64}, {-0.09, 0.64}, {0.09, 0.64}, {0.27, 0.64}, {0.45, 0.64}, {0.64, 0.64},
                {0.82, 0.64}, {-0.82, 0.45}, {-0.64, 0.45}, {-0.45, 0.45}, {-0.09, 0.45},
                {0.09, 0.45}, {0.45, 0.45}, {0.64, 0.45}, {0.82, 0.45}, {-1.00, 0.27}, {-0.82, 0.27},
                {-0.64, 0.27}, {-0.45, 0.27}, {0.45, 0.27}, {0.64, 0.27}, {0.82, 0.27}, {1.00, 0.27},
                {-1.00, 0.09}, {-0.82, 0.09}, {-0.64, 0.09}, {-0.27, 0.09}, {0.27, 0.09},
                {0.64, 0.09}, {0.82, 0.09}, {1.00, 0.09}, {-1.00, -0.09}, {-0.82, -0.09},
                {-0.27, -0.09}, {0.27, -0.09}, {0.82, -0.09}, {1.00, -0.09}, {-1.00, -0.27},
                {-0.82, -0.27}, {-0.64, -0.27}, {0.64, -0.27}, {0.82, -0.27}, {1.00, -0.27},
                {-0.82, -0.45}, {-0.64, -0.45}, {-0.45, -0.45}, {0.45, -0.45}, {0.64, -0.45},
                {0.82, -0.45}, {-0.82, -0.64}, {-0.64, -0.64}, {-0.45, -0.64}, {-0.27, -0.64},
                {-0.09, -0.64}, {0.09, -0.64}, {0.27, -0.64}, {0.45, -0.64}, {0.64, -0.64},
                {0.82, -0.64}, {-0.64, -0.82}, {-0.45, -0.82}, {-0.27, -0.82}, {-0.09, -0.82},
                {0.09, -0.82}, {0.27, -0.82}, {0.45, -0.82}, {0.64, -0.82}, {-0.27, -1.00},
                {-0.09, -1.00}, {0.09, -1.00}, {0.27, -1.00}
            };
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(0xFFFF00), 2.0f);
            for (double[] p : shape) {
                showParticle(Particle.REDSTONE, dust, 2, p[0], p[1]);
            }
            break;
        }
        case PRIDE: {
            int[][] colors = {
                {255, 0, 0},
                {255, 127, 0},
                {255, 255, 0},
                {0, 255, 0},
                {0, 0, 255},
                {46, 43, 95},
                {130, 0, 255}
            };
            double[][][] points = {
                {{1.00, 0.00}, {0.98, 0.15}, {0.92, 0.29}, {0.83, 0.42}, {0.71, 0.53}, {0.56, 0.62}, {0.38, 0.69}, {0.20, 0.74},
                 {0.00, 0.75}, {-0.20, 0.74}, {-0.38, 0.69}, {-0.56, 0.62}, {-0.71, 0.53}, {-0.83, 0.42}, {-0.92, 0.29}, {-0.98, 0.15}},
                {{1.00, -0.15}, {0.98, -0.00}, {0.92, 0.14}, {0.83, 0.27}, {0.71, 0.38}, {0.56, 0.47}, {0.38, 0.54}, {0.20, 0.59},
                 {0.00, 0.60}, {-0.20, 0.59}, {-0.38, 0.54}, {-0.56, 0.47}, {-0.71, 0.38}, {-0.83, 0.27}, {-0.92, 0.14}, {-0.98, -0.00}},
                {{1.00, -0.30}, {0.98, -0.15}, {0.92, -0.01}, {0.83, 0.12}, {0.71, 0.23}, {0.56, 0.32}, {0.38, 0.39}, {0.20, 0.44},
                 {0.00, 0.45}, {-0.20, 0.44}, {-0.38, 0.39}, {-0.56, 0.32}, {-0.71, 0.23}, {-0.83, 0.12}, {-0.92, -0.01}, {-0.98, -0.15}},
                {{1.00, -0.45}, {0.98, -0.30}, {0.92, -0.16}, {0.83, -0.03}, {0.71, 0.08}, {0.56, 0.17}, {0.38, 0.24}, {0.20, 0.29},
                 {0.00, 0.30}, {-0.20, 0.29}, {-0.38, 0.24}, {-0.56, 0.17}, {-0.71, 0.08}, {-0.83, -0.03}, {-0.92, -0.16}, {-0.98, -0.30}},
                {{1.00, -0.60}, {0.98, -0.45}, {0.92, -0.31}, {0.83, -0.18}, {0.71, -0.07}, {0.56, 0.02}, {0.38, 0.09}, {0.20, 0.14},
                 {0.00, 0.15}, {-0.20, 0.14}, {-0.38, 0.09}, {-0.56, 0.02}, {-0.71, -0.07}, {-0.83, -0.18}, {-0.92, -0.31}, {-0.98, -0.45}},
                {{1.00, -0.75}, {0.98, -0.60}, {0.92, -0.46}, {0.83, -0.33}, {0.71, -0.22}, {0.56, -0.13}, {0.38, -0.06}, {0.20, -0.01},
                 {0.00, 0.00}, {-0.20, -0.01}, {-0.38, -0.06}, {-0.56, -0.13}, {-0.71, -0.22}, {-0.83, -0.33}, {-0.92, -0.46}, {-0.98, -0.60}},
                {{1.00, -0.90}, {0.98, -0.75}, {0.92, -0.61}, {0.83, -0.48}, {0.71, -0.37}, {0.56, -0.28}, {0.38, -0.21}, {0.20, -0.16},
                 {0.00, -0.15}, {-0.20, -0.16}, {-0.38, -0.21}, {-0.56, -0.28}, {-0.71, -0.37}, {-0.83, -0.48}, {-0.92, -0.61}, {-0.98, -0.75}}
            };
            double[][] clouds = {
                {1.125, -0.4},
                {-1.125, -0.4}
            };
            for (int i = 0; i < points.length; i += 1) {
                double[][] segment = points[i];
                int[] color = colors[i];
                Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(color[0], color[1], color[2]), 2.0f);
                for (double[] p : segment) {
                    showParticle(Particle.REDSTONE, dust, 1, p[0], p[1]);
                }
            }
            for (double[] p : clouds) {
                showParticle(Particle.CLOUD, null, 32, 0.55, p[0], p[1]);
            }
            break;
        }
        case SHAMROCK: {
            double[][] shamrockPoints = {
                {-0.29, 1.00}, {-0.14, 1.00}, {0.14, 1.00}, {0.29, 1.00}, {-0.43, 0.86},
                {0.00, 0.86}, {0.43, 0.86}, {-0.57, 0.71}, {0.57, 0.71}, {-0.57, 0.57}, {0.00, 0.57},
                {0.57, 0.57}, {-0.57, 0.43}, {0.00, 0.43}, {0.57, 0.43}, {-0.71, 0.29},
                {-0.57, 0.29}, {0.57, 0.29}, {0.71, 0.29}, {-0.86, 0.14}, {-0.43, 0.14},
                {0.43, 0.14}, {0.86, 0.14}, {-1.00, 0.00}, {1.00, 0.00}, {-1.00, -0.14},
                {-0.43, -0.14}, {-0.29, -0.14}, {0.29, -0.14}, {0.43, -0.14}, {1.00, -0.14},
                {-0.86, -0.29}, {-0.57, -0.29}, {0.57, -0.29}, {0.86, -0.29}, {-1.00, -0.43},
                {0.00, -0.43}, {1.00, -0.43}, {-1.00, -0.57}, {-0.14, -0.57}, {0.00, -0.57},
                {0.14, -0.57}, {1.00, -0.57}, {-0.86, -0.71}, {-0.29, -0.71}, {0.00, -0.71},
                {0.29, -0.71}, {0.86, -0.71}, {-0.71, -0.86}, {-0.57, -0.86}, {-0.43, -0.86},
                {0.00, -0.86}, {0.14, -0.86}, {0.43, -0.86}, {0.57, -0.86}, {0.71, -0.86},
                {0.14, -1.00}
            };
            for (double[] p : shamrockPoints) {
                showParticle(Particle.VILLAGER_HAPPY, null, 4, 0.035, p[0], p[1]);
            }
            break;
        }
        case PI: {
            double[][] piPoints = {
                {-0.83, 1.00}, {-0.67, 1.00}, {-0.50, 1.00}, {-0.33, 1.00}, {-0.17, 1.00},
                {0.00, 1.00}, {0.17, 1.00}, {0.33, 1.00}, {0.50, 1.00}, {0.67, 1.00}, {0.83, 1.00},
                {1.00, 1.00}, {-1.00, 0.83}, {-0.83, 0.83}, {-0.67, 0.83}, {-0.50, 0.83},
                {-0.33, 0.83}, {-0.17, 0.83}, {0.00, 0.83}, {0.17, 0.83}, {0.33, 0.83},
                {0.50, 0.83}, {0.67, 0.83}, {0.83, 0.83}, {1.00, 0.83}, {-1.00, 0.67}, {-0.83, 0.67},
                {-0.50, 0.67}, {-0.33, 0.67}, {0.33, 0.67}, {0.50, 0.67}, {-1.00, 0.50},
                {-0.50, 0.50}, {-0.33, 0.50}, {0.33, 0.50}, {0.50, 0.50}, {-0.50, 0.33},
                {-0.33, 0.33}, {0.17, 0.33}, {0.33, 0.33}, {-0.50, 0.17}, {-0.33, 0.17},
                {0.17, 0.17}, {0.33, 0.17}, {-0.50, 0.00}, {-0.33, 0.00}, {0.17, 0.00}, {0.33, 0.00},
                {-0.50, -0.17}, {-0.33, -0.17}, {0.17, -0.17}, {0.33, -0.17}, {-0.67, -0.33},
                {-0.50, -0.33}, {-0.33, -0.33}, {0.17, -0.33}, {0.33, -0.33}, {-0.67, -0.50},
                {-0.50, -0.50}, {0.17, -0.50}, {0.33, -0.50}, {0.50, -0.50}, {1.00, -0.50},
                {-0.83, -0.67}, {-0.67, -0.67}, {-0.50, -0.67}, {0.33, -0.67}, {0.50, -0.67},
                {0.67, -0.67}, {0.83, -0.67}, {1.00, -0.67}, {-0.83, -0.83}, {-0.67, -0.83},
                {-0.50, -0.83}, {0.33, -0.83}, {0.50, -0.83}, {0.67, -0.83}, {0.83, -0.83},
                {-0.67, -1.00}, {0.50, -1.00}, {0.67, -1.00},
            };
            double[][] circlePoints = {
                {1.00, 0.00}, {0.98, 0.17}, {0.94, 0.34}, {0.87, 0.50}, {0.77, 0.64}, {0.64, 0.77},
                {0.50, 0.87}, {0.34, 0.94}, {0.17, 0.98}, {0.00, 1.00}, {-0.17, 0.98}, {-0.34, 0.94},
                {-0.50, 0.87}, {-0.64, 0.77}, {-0.77, 0.64}, {-0.87, 0.50}, {-0.94, 0.34},
                {-0.98, 0.17}, {-1.00, 0.00}, {-0.98, -0.17}, {-0.94, -0.34}, {-0.87, -0.50},
                {-0.77, -0.64}, {-0.64, -0.77}, {-0.50, -0.87}, {-0.34, -0.94}, {-0.17, -0.98},
                {-0.00, -1.00}, {0.17, -0.98}, {0.34, -0.94}, {0.50, -0.87}, {0.64, -0.77},
                {0.77, -0.64}, {0.87, -0.50}, {0.94, -0.34}, {0.98, -0.17}
            };
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(0, 191, 255), 1.6f);
            for (double[] p : piPoints) {
                showParticle(Particle.REDSTONE, dust, 1, p[0] * 0.8, p[1] * 0.8);
            }
            for (double[] p : circlePoints) {
                showParticle(Particle.END_ROD, null, 1, p[0] * 1.2, p[1] * 1.2);
            }
            break;
        }
        case STAR: {
            double[][] points = {
                {0.00, 1.00}, {-0.14, 0.87}, {0.14, 0.87}, {-0.14, 0.73}, {0.14, 0.73},
                {-0.29, 0.60}, {0.29, 0.60}, {-0.29, 0.47}, {0.29, 0.47}, {-1.00, 0.33},
                {-0.86, 0.33}, {-0.71, 0.33}, {-0.57, 0.33}, {-0.43, 0.33}, {0.43, 0.33},
                {0.57, 0.33}, {0.71, 0.33}, {0.86, 0.33}, {1.00, 0.33}, {-1.00, 0.20},
                {1.00, 0.20}, {-0.86, 0.07}, {-0.14, 0.07}, {0.14, 0.07}, {0.86, 0.07},
                {-0.71, -0.07}, {-0.14, -0.07}, {0.14, -0.07}, {0.71, -0.07},
                {-0.57, -0.20}, {-0.14, -0.20}, {0.14, -0.20}, {0.57, -0.20},
                {-0.71, -0.33}, {0.71, -0.33}, {-0.71, -0.47}, {0.71, -0.47},
                {-0.86, -0.60}, {0.00, -0.60}, {0.86, -0.60}, {-0.86, -0.73},
                {-0.29, -0.73}, {-0.14, -0.73}, {0.14, -0.73}, {0.29, -0.73},
                {0.86, -0.73}, {-1.00, -0.87}, {-0.57, -0.87}, {-0.43, -0.87},
                {0.43, -0.87}, {0.57, -0.87}, {1.00, -0.87}, {-1.00, -1.00},
                {-0.86, -1.00}, {-0.71, -1.00}, {0.71, -1.00}, {0.86, -1.00},
                {1.00, -1.00}
            };
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(255, 255, 0), 2.0f);
            for (double[] p : points) {
                showParticle(Particle.REDSTONE, dust, 2, p[0] * 1.25, p[1] * 1.25);
            }
            break;
        }
        case HEART:
        default: {
            double[][] points = {
                {-0.60, 1.00},
                {-0.40, 1.00},
                {0.40, 1.00},
                {0.60, 1.00},
                {-0.80, 0.78},
                {-0.20, 0.78},
                {0.20, 0.78},
                {0.80, 0.78},
                {-1.00, 0.56},
                {0.00, 0.56},
                {1.00, 0.56},
                {-1.00, 0.33},
                {1.00, 0.33},
                {-1.00, 0.11},
                {1.00, 0.11},
                {-0.80, -0.11},
                {0.80, -0.11},
                {-0.60, -0.33},
                {0.60, -0.33},
                {-0.40, -0.56},
                {0.40, -0.56},
                {-0.20, -0.78},
                {0.20, -0.78},
                {0.00, -1.00}
            };
            for (double[] p : points) {
                showParticle(Particle.HEART, null, 8, p[0], p[1]);
            }
            break;
        }
        }
    }

    public <T> void showParticle(Particle particle, T data, int amount, double x, double y) {
        Vector v = offset.toVector()
            .add(right().multiply(x * scale))
            .add(up().multiply(y * scale));
        offset.getWorld().spawnParticle(particle, v.getX(), v.getY(), v.getZ(), amount, 0.0, 0.0, 0.0, 0.0, data);
    }

    public <T> void showParticle(Particle particle, T data, int amount, double spread, double x, double y) {
        Vector v = offset.toVector()
            .add(right().multiply(x * scale))
            .add(up().multiply(y * scale));
        offset.getWorld().spawnParticle(particle, v.getX(), v.getY(), v.getZ(), amount, spread, spread, spread, 0.0, data);
    }
}
