package com.thevoxelbox.voxelport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.World;

public class Zone
{

    public int zonehighx, zonehighy, zonehighz, zonelowx, zonelowy, zonelowz;
    public String world;
    public World.Environment worldEnvironment;

    public Zone(final DataInputStream data) throws IOException
    {
        this.zonehighx = data.readInt();
        this.zonehighy = data.readInt();
        this.zonehighz = data.readInt();
        this.zonelowx = data.readInt();
        this.zonelowy = data.readInt();
        this.zonelowz = data.readInt();
        this.world = data.readUTF();
        this.worldEnvironment = World.Environment.valueOf(data.readUTF());
    }

    public Zone(final int highx, final int lowx, final int highy, final int lowy, final int highz, final int lowz, final String worldname)
    {
        this.zonehighx = highx;
        this.zonehighy = highy;
        this.zonehighz = highz;
        this.zonelowx = lowx;
        this.zonelowy = lowy;
        this.zonelowz = lowz;
        this.world = worldname;
        this.worldEnvironment = World.Environment.NORMAL;
    }

    public Zone(final int highx, final int lowx, final int highy, final int lowy, final int highz, final int lowz, final String worldname, final World.Environment environment)
    {
        this.zonehighx = highx;
        this.zonehighy = highy;
        this.zonehighz = highz;
        this.zonelowx = lowx;
        this.zonelowy = lowy;
        this.zonelowz = lowz;
        this.world = worldname;
        this.worldEnvironment = environment;
    }

    public boolean inZone(final Location loc)
    {
        return (loc.getBlockX() <= this.zonehighx) && (loc.getBlockX() >= this.zonelowx) && (loc.getBlockZ() <= this.zonehighz) && (loc.getBlockZ() >= this.zonelowz) && (loc.getBlockY() <= this.zonehighy) && (loc.getBlockY() >= this.zonelowy)
                && (this.world.equals(loc.getWorld().getName()));
    }

    public void save(final DataOutputStream data) throws IOException
    {
        data.writeInt(this.zonehighx);
        data.writeInt(this.zonehighy);
        data.writeInt(this.zonehighz);
        data.writeInt(this.zonelowx);
        data.writeInt(this.zonelowy);
        data.writeInt(this.zonelowz);
        data.writeUTF(this.world);
        data.writeUTF(this.worldEnvironment.name());
    }
}
