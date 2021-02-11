package com.winthier.title;

import lombok.Value;
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
        case HEART:
        default:
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

    public <T> void showParticle(Particle particle, T data, int amount, double x, double y) {
        Vector v = offset.toVector()
            .add(right().multiply(x * scale))
            .add(up().multiply(y * scale));
        offset.getWorld().spawnParticle(particle, v.getX(), v.getY(), v.getZ(), amount, 0.0, 0.0, 0.0, 0.0, data);
    }
}
