package com.winthier.title;

import lombok.Value;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

@Value
public final class ShinePlace {
    private final Location offset;
    private final Vector right;
    private final Vector up;
    private final double scale;

    public static ShinePlace of(Location location, double scale) {
        return of(location, new Vector(0, 0, 0), scale);
    }

    public static ShinePlace of(Location location, Vector add, double scale) {
        location.setPitch(0f);
        Vector right = location.getDirection()
            .normalize()
            .rotateAroundY(Math.PI * -0.5);
        Vector up = new Vector(0, 1, 0);
        return new ShinePlace(location.add(add), right, up, scale);
    }

    public Vector right() {
        return right.clone();
    }

    public Vector up() {
        return up.clone();
    }

    public void show(Shine shine) {
        switch (shine) {
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
            for (double[] p : points) {
                Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(255, 255, 0), 2.0f);
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
}
