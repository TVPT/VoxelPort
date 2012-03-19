package com.thevoxelbox.voxelport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.bukkit.Location;
import org.bukkit.World;


public class Zone {

    public int zonehighx, zonehighy, zonehighz, zonelowx, zonelowy, zonelowz;
    public String world;
    public World.Environment worldEnvironment;

    public Zone(DataInputStream data) throws IOException {
        zonehighx = data.readInt();
        zonehighy = data.readInt();
        zonehighz = data.readInt();
        zonelowx = data.readInt();
        zonelowy = data.readInt();
        zonelowz = data.readInt();
        world = data.readUTF();
        worldEnvironment = World.Environment.valueOf(data.readUTF());
    }

    public Zone(int highx, int lowx, int highy, int lowy, int highz, int lowz, String worldname) {
        zonehighx = highx;
        zonehighy = highy;
        zonehighz = highz;
        zonelowx = lowx;
        zonelowy = lowy;
        zonelowz = lowz;
        world = worldname;
        worldEnvironment = World.Environment.NORMAL;
    }

    public Zone(int highx, int lowx, int highy, int lowy, int highz, int lowz, String worldname, World.Environment environment) {
        zonehighx = highx;
        zonehighy = highy;
        zonehighz = highz;
        zonelowx = lowx;
        zonelowy = lowy;
        zonelowz = lowz;
        world = worldname;
        worldEnvironment = environment;
    }

    public boolean inZone(Location loc) {
        return (loc.getBlockX() <= zonehighx) && (loc.getBlockX() >= zonelowx)
                && (loc.getBlockZ() <= zonehighz) && (loc.getBlockZ() >= zonelowz)
                && (loc.getBlockY() <= zonehighy) && (loc.getBlockY() >= zonelowy)
                && (world.equals(loc.getWorld().getName()));
    }

    public void save(DataOutputStream data) throws IOException {
        data.writeInt(zonehighx);
        data.writeInt(zonehighy);
        data.writeInt(zonehighz);
        data.writeInt(zonelowx);
        data.writeInt(zonelowy);
        data.writeInt(zonelowz);
        data.writeUTF(world);
        data.writeUTF(worldEnvironment.name());
    }
}
