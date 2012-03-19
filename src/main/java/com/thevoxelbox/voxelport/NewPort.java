package com.thevoxelbox.bukkit.port;

//import com.thevoxelbox.bukkit.port.NPC.NpcSpawner;
//import com.thevoxelbox.bukkit.port.NPC.PortNPC;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

/**
 *
 * @author Voxel
 */
public class NewPort {

    private String portalName;
    private Zone portalZone;
    private Location arrivalLocation;
    private Location departLocation;
    private Location redstoneKey;
    private TreeSet<Integer> departures = new TreeSet<Integer>();
    private ArrayList<String> welcomeMessages = new ArrayList<String>();
    private boolean loaded = false;
    private boolean instant = false;
    private boolean requireTicket = false;
    private int timeLeft;
    private int lastTime = -1;

    public void printInfo(Player p) {
        p.sendMessage(ChatColor.GOLD + "Info for VoxelPort " + ChatColor.BLUE + "\"" + ChatColor.AQUA + getName() + ChatColor.BLUE + "\"" + ChatColor.GOLD + " :");
        p.sendMessage(ChatColor.GREEN + "This port" + ChatColor.AQUA + ":");
        p.sendMessage((instant() ? (ChatColor.LIGHT_PURPLE + "Is") : (ChatColor.RED + "Is not")) + ChatColor.GREEN + " instant");
        p.sendMessage((ticket() ? (ChatColor.LIGHT_PURPLE + "Requires") : (ChatColor.RED + "Does not require")) + ChatColor.GREEN + " a ticket");
        p.sendMessage((redstoneKey != null ? (ChatColor.LIGHT_PURPLE + "Has a Redstone Key set, which " + (redstoneKey.getBlock().isBlockPowered() ? (ChatColor.AQUA + "is powered") : (ChatColor.RED + "is not powered"))) : (ChatColor.RED + "Does not have a Redstone Key")));
        p.sendMessage((!departures.isEmpty() ? (ChatColor.LIGHT_PURPLE + "Contains " + ChatColor.BLUE + departures.size() + ChatColor.LIGHT_PURPLE + " departure times.") : (ChatColor.RED + "Does not contain any departure times")));
        if (welcomeMessages.isEmpty()) {
            p.sendMessage(ChatColor.RED + "Does not contain welcome messages");
        } else {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Contains " + ChatColor.BLUE + welcomeMessages.size() + ChatColor.LIGHT_PURPLE + " welcome messages, and they are" + ChatColor.GREEN + ":");
            welcomePlayer(p);
        }
    }

    class MoveEventSucks implements Runnable {

        private Player p;
        private Location l;

        public MoveEventSucks(Player player, Location destination) {
            p = player;
            l = destination;
        }

        @Override
        public void run() {
            p.teleport(l, TeleportCause.ENDER_PEARL);
            p.sendMessage(ChatColor.DARK_AQUA + "Woosh!");
            int chunkx = p.getWorld().getChunkAt(l.getBlock()).getX();
            int chunkz = p.getWorld().getChunkAt(l.getBlock()).getZ();

            p.getWorld().refreshChunk(p.getLocation().getBlockX(), p.getLocation().getBlockZ());
        }
    }
    //

    public NewPort(String Name) {
        portalName = Name;
        readData();
    }

    public NewPort(int highx, int lowx, int highy, int lowy, int highz, int lowz, String worldname, String Name) {
        this(highx, lowx, highy, lowy, highz, lowz, worldname, Name, null, null);
    }

    public NewPort(int highx, int lowx, int highy, int lowy, int highz, int lowz, String worldname, String Name, Location arrival, Location depart) {
        portalZone = new Zone(highx, lowx, highy, lowy, highz, lowz, worldname);
        portalName = Name;
        arrivalLocation = arrival;
        departLocation = depart;
    }

    public NewPort(int highx, int lowx, int highy, int lowy, int highz, int lowz, String worldname, World.Environment env, String Name) {
        portalZone = new Zone(highx, lowx, highy, lowy, highz, lowz, worldname, env);
        portalName = Name;
    }

    public String getName() {
        return portalName;
    }

    public void setZone(Zone z) {
        portalZone = z;
    }

    public Zone getZone() {
        return portalZone;
    }

    public void setArrival(Location loc) {
        arrivalLocation = loc;
    }

    public void setDestination(Location loc) {
        departLocation = loc;
    }

    public Location getArrival() {
        return arrivalLocation;
    }

    public void setInstant(boolean bool) {
        instant = bool;
    }

    public void setTicket(boolean bool) {
        requireTicket = bool;
    }

    public void setRedstoneKey(Location loc) {
        redstoneKey = loc;
    }

    public boolean instant() {
        return instant;
    }

    public boolean ticket() {
        return requireTicket;
    }

    public Location getRedstoneKey() {
        return redstoneKey;
    }

    public void clearMessages() {
        welcomeMessages.clear();
    }

    public void addMessage(String message) {
        welcomeMessages.add(message);
    }

    public void welcomePlayer(Player p) {
        if (welcomeMessages.isEmpty()) {
            //p.sendMessage(ChatColor.DARK_GREEN + "This port doesn't have a welcome message. Nag an admin to create one.");
            return;
        }
        for (String s : welcomeMessages) {
            p.sendMessage(ChatColor.GREEN + s);
        }
    }

    public void clearDepartures() {
        departures.clear();
    }

    public void addDeparture(int departure) {
        departures.add(departure);
    }

    public void setDepartures(HashSet<Integer> hs) {
        for (int i : hs) {
            departures.add(i);
        }
    }

    public void generateDepartures(int start, int interval, Player p) {
        if (p == null) {
            int x;
            for (x = start; x < 24000; x += interval) {
                departures.add(x);
            }
            x -= 24000;
            if (x >= 0 && x < start) {
                for (x = x + 0; x < start; x += interval) {
                    departures.add(x);
                }
            }
        }
        if (departures.isEmpty()) {
            if (start > -1 && start < 24000 && interval > 0 && start % PortTick.codeTick == 0 && interval % PortTick.codeTick == 0) {
                if (24000 % interval == 0) {
                    int x;
                    for (x = start; x < 24000; x += interval) {
                        departures.add(x);
                    }
                    x -= 24000;
                    if (x >= 0 && x < start) {
                        for (int y = x; y < start; y += interval) {
                            departures.add(y);
                        }
                    }
                    p.sendMessage(ChatColor.AQUA + "Created dispatch times for portal \"" + portalName + "\" starting at " + mcTimeFromCodeTime(start));
                    p.sendMessage(ChatColor.AQUA + "And departing every " + mcTimeFromCodeTime(interval));
                } else {
                    p.sendMessage(ChatColor.RED + "Irregular dispatch times are not supported. Please make sure the interval goes evenly into 24000.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Invalid start or interval value. Make sure they are bolth positive, a multiple of " + PortTick.codeTick + ", and under 24000");
            }
        } else {
            p.sendMessage(ChatColor.RED + "This Portal already contains departures! Please clear the departures with \"/vp disp [portalname] \"clear\"\" and try again.");
        }
    }

    private String mcTimeFromCodeTime(int codeTime) {
        if (codeTime == 0) {
            return ChatColor.AQUA + "Never!";
        }
        int mctime = (int) (codeTime * 3.6D);
        int seconds = mctime % 60;
        mctime = ((mctime - seconds) / 60);
        int minutes = mctime % 60;
        int hours = (mctime - minutes) / 60;
        if (hours == 0) {
            if (minutes == 0) {
                if (seconds == 0) {
                    return ChatColor.GREEN + "Now?";
                } else {
                    return ChatColor.GREEN + "" + seconds + ChatColor.BLUE + "s";
                }
            } else {
                if (seconds == 0) {
                    return ChatColor.GREEN + "" + minutes + ChatColor.BLUE + "m";
                } else {
                    return ChatColor.GREEN + "" + minutes + ChatColor.BLUE + "m"
                            + ChatColor.DARK_BLUE + " : "
                            + ChatColor.GREEN + "" + seconds + ChatColor.BLUE + "s";
                }
            }
        } else {
            if (minutes == 0) {
                if (seconds == 0) {
                    return ChatColor.GREEN + "" + hours + ChatColor.BLUE + "h";
                } else {
                    return ChatColor.GREEN + "" + hours + ChatColor.BLUE + "h"
                            + ChatColor.DARK_BLUE + " : "
                            + ChatColor.GREEN + "" + seconds + ChatColor.BLUE + "s";
                }
            } else {
                if (seconds == 0) {
                    return ChatColor.GREEN + "" + hours + ChatColor.BLUE + "h"
                            + ChatColor.DARK_BLUE + " : "
                            + ChatColor.GREEN + "" + minutes + ChatColor.BLUE + "m";
                } else {
                    return ChatColor.GREEN + "" + hours + ChatColor.BLUE + "h"
                            + ChatColor.DARK_BLUE + " : "
                            + ChatColor.GREEN + "" + minutes + ChatColor.BLUE + "m"
                            + ChatColor.DARK_BLUE + " : "
                            + ChatColor.GREEN + "" + seconds + ChatColor.BLUE + "s";
                }
            }
        }
    }

    public String nextDeparture(int currentTime) {
        int time = 0;
        for (int x = currentTime; x < 24000; x += PortTick.codeTick) {
            if (departures.contains(x)) {
                return (mcTimeFromCodeTime(time));
            }
            time += PortTick.codeTick;
        }
        for (int x = 0; x < currentTime; x += PortTick.codeTick) {
            if (departures.contains(x)) {
                return (mcTimeFromCodeTime(time));
            }
            time += PortTick.codeTick;
        }
        return ChatColor.AQUA + "Never";
    }

    public String nextDepartureTotal(int currentTime) {
        //System.out.println("Time " + currentTime);
        int time = 0;
        int roundedTime = currentTime - (currentTime % PortTick.codeTick);
        for (int x = roundedTime + PortTick.codeTick; x < 24000; x += PortTick.codeTick) {
            if (departures.contains(x)) {
                int i = time + (PortTick.codeTick - (currentTime % PortTick.codeTick));
                timeLeft = time;
                return mcTimeFromCodeTime(i);
            }
            time += PortTick.codeTick;
        }
        for (int x = 0; x < currentTime; x += PortTick.codeTick) {
            if (departures.contains(x)) {
                int i = time + (PortTick.codeTick - (currentTime % PortTick.codeTick));
                timeLeft = time;
                return mcTimeFromCodeTime(i);
            }
            time += PortTick.codeTick;
        }
        timeLeft = 0;
        return ChatColor.AQUA + "Never";
    }

    public boolean insideZone(Location loc) {
        return portalZone.inZone(loc);
    }

    public boolean departPlayer(Player p, int time) {
        if (insideZone(p.getLocation())) {
            if (departures.contains(time)) {
                if (departLocation == null) {
                    p.sendMessage(ChatColor.RED + "This portal doesn't contain a target location!");
                    PortTick.usedTickets.add(p);
                    return true;
                }
                p.teleport(departLocation, TeleportCause.ENDER_PEARL);
                PortTick.usedTickets.add(p);
                return true;
            } else {
                p.sendMessage(ChatColor.GOLD + "The next departure will occur in " + ChatColor.DARK_AQUA + ":     " + ChatColor.DARK_RED + "[" + mcTimeFromCodeTime(timeLeft) + ChatColor.DARK_RED + "]");
                if (time != lastTime) {
                    timeLeft -= PortTick.codeTick;
                    lastTime = time;
                }
                if (timeLeft < 0) {
                    timeLeft = 0;
                }
                return false;
            }
        } else {
            p.sendMessage(ChatColor.DARK_GREEN + "You have left the VoxelPort, therefore your ticket has expired.");
            PortTick.usedTickets.add(p);
            return true;
        }
    }

    public void instaPort(Player p, boolean override) {
        if (departLocation == null) {
            p.sendMessage(ChatColor.RED + "This portal doesn't contain a target location!");
            return;
        } else {
            if (override) {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(VoxelPort.vp, new MoveEventSucks(p, departLocation));
            } else {
                if (redstoneKey != null && !redstoneKey.getBlock().isBlockPowered()) {
                    return;
                }
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(VoxelPort.vp, new MoveEventSucks(p, departLocation));
            }
        }
    }

    public boolean isPortActivated() {
        return redstoneKey == null ? true : redstoneKey.getBlock().isBlockPowered();
    }

    public void saveData() {
        try {
            File f = new File("plugins/VoxelPort/Ports/" + portalName);
            if (!f.getParentFile().isDirectory()) {
                f.mkdirs();
            }
            FileOutputStream file = new FileOutputStream(f);
            DataOutputStream data = new DataOutputStream(file);
            data.writeByte(1);
            portalZone.save(data);
            if (arrivalLocation != null) {
                data.writeByte(2);
                data.writeUTF(arrivalLocation.getWorld().getName());
                data.writeUTF(arrivalLocation.getWorld().getEnvironment().name());
                data.writeDouble(arrivalLocation.getX());
                data.writeDouble(arrivalLocation.getY());
                data.writeDouble(arrivalLocation.getZ());
                data.writeFloat(arrivalLocation.getYaw());
                data.writeFloat(arrivalLocation.getPitch());
            }
            if (departLocation != null) {
                data.writeByte(3);
                data.writeUTF(departLocation.getWorld().getName());
                data.writeUTF(departLocation.getWorld().getEnvironment().name());
                data.writeDouble(departLocation.getX());
                data.writeDouble(departLocation.getY());
                data.writeDouble(departLocation.getZ());
                data.writeFloat(departLocation.getYaw());
                data.writeFloat(departLocation.getPitch());
            }
            if (redstoneKey != null) {
                data.writeByte(9);
                data.writeUTF(redstoneKey.getWorld().getName());
                data.writeUTF(redstoneKey.getWorld().getEnvironment().name());
                data.writeDouble(redstoneKey.getX());
                data.writeDouble(redstoneKey.getY());
                data.writeDouble(redstoneKey.getZ());
            }
            if (!departures.isEmpty()) {
                for (int dep : departures) {
                    data.writeByte(4);
                    data.writeInt(dep);
                }
            }
            if (!welcomeMessages.isEmpty()) {
                for (String string : welcomeMessages) {
                    data.writeByte(5);
                    data.writeUTF(string);
                }
            }

            data.writeByte(6);
            data.writeBoolean(instant);
            data.writeBoolean(requireTicket);

            data.close();
            VoxelPort.log.info("[VoxelPort] Portal \"" + portalName + "\" saved.");
        } catch (IOException e) {
            VoxelPort.log.warning("[VoxelPort] Error while saving portal \"" + portalName + "\"");
            e.printStackTrace();
        }
    }

    private void readData() {
        if (new File("plugins/VoxelPort/Ports/" + portalName).exists()) {
            try {
                FileInputStream file = new FileInputStream(new File("plugins/VoxelPort/Ports/" + portalName));
                DataInputStream data = new DataInputStream(file);
                try {
                    while (true) {
                        byte by = data.readByte();
                        if (by == 1) {
                            portalZone = new Zone(data);
                        }
                        if (by == 2) {
                            String wName = data.readUTF();
                            World.Environment wEnv = World.Environment.valueOf(data.readUTF());
                            World world = Bukkit.getWorld(wName);
                            double wx = data.readDouble();
                            double wy = data.readDouble();
                            double wz = data.readDouble();
                            float wYaw = data.readFloat();
                            float wPitch = data.readFloat();
                            //VoxelPort.log.info("VoxelPort " + getName() + ", world: " + wName);
                            arrivalLocation = new Location(world, wx, wy, wz, wYaw, wPitch);
                        }
                        if (by == 3) {
                            String wName = data.readUTF();
                            World.Environment wEnv = World.Environment.valueOf(data.readUTF());
                            World world = Bukkit.getWorld(wName);
                            double wx = data.readDouble();
                            double wy = data.readDouble();
                            double wz = data.readDouble();
                            float wYaw = data.readFloat();
                            float wPitch = data.readFloat();
                            //VoxelPort.log.info("VoxelPort " + getName() + ", world: " + wName);
                            departLocation = new Location(world, wx, wy, wz, wYaw, wPitch);
                        }
                        if (by == 9) {
                            String wName = data.readUTF();
                            World.Environment wEnv = World.Environment.valueOf(data.readUTF());
                            World world = Bukkit.getWorld(wName);
                            double wx = data.readDouble();
                            double wy = data.readDouble();
                            double wz = data.readDouble();
                            float wYaw = 0;
                            float wPitch = 0;
                            //VoxelPort.log.info("VoxelPort " + getName() + ", world: " + wName);
                            redstoneKey = new Location(world, wx, wy, wz, wYaw, wPitch);
                        }
                        if (by == 4) {
                            departures.add(data.readInt());
                        }
                        if (by == 5) {
                            welcomeMessages.add(data.readUTF());
                        }
                        if (by == 6) {
                            instant = data.readBoolean();
                            requireTicket = data.readBoolean();
                        }
                    }
                } catch (EOFException eof) {
                    data.close();
                    loaded = true;
                }
            } catch (IOException e) {
                VoxelPort.log.warning("[VoxelPort] Invalid File. \"" + portalName + "\" is not a VoxelPort or is currupted.");
                e.printStackTrace();
            }
        }
    }

    public boolean loaded() {
        return loaded;
    }

    public void genZoneBoundKeys() {
        int contlowx = portalZone.zonelowx / PortManager.CONTAINER_SIZE;
        int contlowz = portalZone.zonelowz / PortManager.CONTAINER_SIZE;
        int conthighx = portalZone.zonehighx / PortManager.CONTAINER_SIZE;
        int conthighz = portalZone.zonehighz / PortManager.CONTAINER_SIZE;
        for (int x = contlowx; x <= conthighx; x++) {
            for (int z = contlowz; z <= conthighz; z++) {
                PortManager.insertPort(this, x, z);
            }
        }
    }

    public void deleteZoneBoundKeys() {
        int contlowx = portalZone.zonelowx / PortManager.CONTAINER_SIZE;
        int contlowz = portalZone.zonelowz / PortManager.CONTAINER_SIZE;
        int conthighx = portalZone.zonehighx / PortManager.CONTAINER_SIZE;
        int conthighz = portalZone.zonehighz / PortManager.CONTAINER_SIZE;
        for (int x = contlowx; x <= conthighx; x++) {
            for (int z = contlowz; z <= conthighz; z++) {
                PortManager.deletePort(this, x, z);
            }
        }
    }
}
