package org.cloudplugins.cloudJail.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class Jail {
    private final UUID worldUUID;
    private final String worldName;
    private final double x, y, z;
    private final float yaw, pitch;

    public Jail(Location location){
        this(location.getWorld(), location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
    }

    public Jail(World world, double x, double y, double z, float yaw, float pitch){
        this(world.getUID(), world.getName(), x, y, z, yaw, pitch);
    }

    public Jail(UUID worldUUID, String worldName, double x, double y, double z, float yaw, float pitch){
        this.worldUUID = worldUUID;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public World getWorld() {
        World world = worldUUID != null ? Bukkit.getWorld(worldUUID) : null;
        if (world == null && worldName != null) {
            world = Bukkit.getWorld(worldName);
        }
        return world;
    }

    public Location getLocation() {
        World world = getWorld();
        return world == null ? null : new Location(world, x, y, z, yaw, pitch);
    }

    public UUID getWorldUUID() { return worldUUID; }
    public String getWorldName() {
        World world = getWorld();
        return world != null ? world.getName() : worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
