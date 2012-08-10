package com.thevoxelbox.voxelport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;


public class Zone {

    public int zonehighx, zonehighy, zonehighz, zonelowx, zonelowy, zonelowz;
    public float yaw;
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
        
        // Read new yaw value from data stream. Fail gracefully, as older saved Zone files may not contain this parameter.
        try {
            yaw = data.readFloat();
        } catch (Exception ex) {
            yaw = 0;
        }
    }

    public Zone(int highx, int lowx, int highy, int lowy, int highz, int lowz, String worldname) {
        this(highx, lowx, highy, lowy, highz, lowz, worldname, World.Environment.NORMAL);
    }

    public Zone(int x1, int x2, int y1, int y2, int z1, int z2, String worldname, World.Environment environment) {
        // To maintain backwards-compatibility with older Zone save formats, we must save the high and low values in a certain order.
        // This loses information on the orientation of the new Zone, which we regain via the new yaw parameter.
        zonehighx = (x1 > x2 ? x1 : x2);
        zonehighy = (y1 > y2 ? y1 : y2);
        zonehighz = (z1 > z2 ? z1 : z2);
        zonelowx = (x1 > x2 ? x2 : x1);
        zonelowy = (y1 > y2 ? y2 : y1);
        zonelowz = (z1 > z2 ? z2 : z1);
        world = worldname;
        worldEnvironment = environment;
        
        // Yaw is automatically calculated from the order in which the points were selected by the user. 
        yaw = (x2 >= x1 ? (z2 >= z1 ? 0 : 270) : (z2 >= z1 ? 90 : 180));
    }
    
    // Returns the first point selected when creating the Zone.
    // 
    // It is possible to use a fancy trigonometric formula to calculate this vector, but since we only ever in practice
    // encounter yaw values of 0, 90, 180, and 270, we can ignore this for now.
    public Vector getPointA() {
        switch (Math.round(this.yaw)) {
        case 90: 	return new Vector(zonehighx, zonelowy, zonelowz);
        case 180: 	return new Vector(zonehighx, zonelowy, zonehighz);
        case 270:	return new Vector(zonelowx, zonelowy, zonehighz);
        default:	return new Vector(zonelowx, zonelowy, zonelowz);
        }
    }

    // Returns the second point selected when creating the Zone
    // 
    // It is possible to use a fancy trigonometric formula to calculate this vector, but since we only ever in practice
    // encounter yaw values of 0, 90, 180, and 270, we can ignore this for now.
    public Vector getPointB() {
        switch (Math.round(this.yaw)) {
        case 90: 	return new Vector(zonelowx, zonehighy, zonehighz);
        case 180: 	return new Vector(zonelowx, zonehighy, zonelowz);
        case 270:	return new Vector(zonehighx, zonehighy, zonelowz);
        default:	return new Vector(zonehighx, zonehighy, zonehighz);
        }
    }

    // Returns a world-relative vector to the origin coordinate of the Zone (the point selected first)
    public Vector getZoneOrigin() {
        Vector origin = getPointA();
        if (origin.getX() > getPointB().getX()) origin.setX(origin.getX() + 1);
        if (origin.getY() > getPointB().getY()) origin.setY(origin.getY() + 1);
        if (origin.getZ() > getPointB().getZ()) origin.setZ(origin.getZ() + 1);

        return origin;
    }

    // Returns an origin-relative vector to the farthest point of the zone (the second point selected)
    public Vector getZoneExtent() {
        Vector extent = getPointB();
        if (extent.getX() > getPointA().getX()) extent.setX(extent.getX() + 1);
        if (extent.getY() > getPointA().getY()) extent.setY(extent.getY() + 1);
        if (extent.getZ() > getPointA().getZ()) extent.setZ(extent.getZ() + 1);

        return extent.subtract(getZoneOrigin());
    }

    public boolean inZone(Location loc) {
        Vector aa = Vector.getMinimum(getZoneOrigin(), getZoneOrigin().add(getZoneExtent()));
        Vector bb = Vector.getMaximum(getZoneOrigin(), getZoneOrigin().add(getZoneExtent()));

        return (world.equals(loc.getWorld().getName())) &&
                (loc.toVector().isInAABB(aa, bb));
    }

    public Location relativeLocationFromWorldLocation(Location worldLocation) {
        if (!inZone(worldLocation)) return null;

        float newyaw = (worldLocation.getYaw() - yaw) % 360;

        Vector retvec = worldLocation.toVector();
        retvec.subtract(getZoneOrigin());
        retvec = rotateVectorInXZPlane(retvec, -yaw);
        retvec.divide(rotateVectorInXZPlane(getZoneExtent(), -yaw));

        return new Location(null, retvec.getX(), retvec.getY(), retvec.getZ(), newyaw, worldLocation.getPitch());
    }

    public Location worldLocationFromRelativeLocation(Location relativeLocation) {
        float newyaw = (relativeLocation.getYaw() + yaw) % 360;

        Vector relvec = relativeLocation.toVector();
        relvec.multiply(rotateVectorInXZPlane(getZoneExtent(), -yaw));
        relvec = rotateVectorInXZPlane(relvec, yaw);
        relvec.add(getZoneOrigin());

        return new Location(Bukkit.getWorld(this.world), relvec.getX(), relvec.getY(), relvec.getZ(), newyaw, relativeLocation.getPitch());
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
        data.writeFloat(this.yaw);
    }
    
    // We need this helper method, but not badly enough to subclass Vector for it.
    public static Vector rotateVectorInXZPlane(Vector input, double degrees) {
        double rad = Math.toRadians(degrees);
        double newx = input.getX() * Math.cos(rad) - input.getZ() * Math.sin(rad);
        double newz = input.getX() * Math.sin(rad) + input.getZ() * Math.cos(rad);

        return new Vector(newx, input.getY(), newz);
    }
}
