/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thevoxelbox.voxelport;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author Voxel
 */
public class PortManager {

    private static TreeMap<Integer, PortContainer> ports = new TreeMap<Integer, PortContainer>();
    public static HashMap<String, NewPort> reference = new HashMap<String, NewPort>();
    public static HashMap<String, String> portTargets = new HashMap<String, String>();
    //
    private static PortTick portTick;
    //
    public static int TICKETID;
    public static int CONTAINER_SIZE;
    public static int CHECK_INTERVAL;
    public static int PORT_TICK_SPEED;
    public static int BUTTON_BLOCK_ID;
    public static byte BUTTON_BLOCK_DATA;
    private static final double CONFIG_VERSION = 2.027;
    //
    private static HashMap<String, PortData> data = new HashMap<String, PortData>();
    //
    private static VoxelPort plugin;

    public PortManager(VoxelPort vp) {
        plugin = vp;
        loadConfig();
        loadPortals();
        loadTargets();
        VoxelPort.log.info("[VoxelPort] Starting thread...");
        portTick = new PortTick(PORT_TICK_SPEED);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, portTick, PortTick.codeTick, PortTick.codeTick <= 1 ? 2 : PortTick.codeTick);
        VoxelPort.log.info("[VoxelPort] Thread Started!");
    }

    public static void inBound(Player p, Location l) {
        try {
            PortContainer pc = ports.get(((int) (l.getBlockX() / CONTAINER_SIZE) + ((int) (l.getBlockZ() / CONTAINER_SIZE)) * 10000));
            if (pc == null) {
                return;
            } else {
                pc.check(p, l);
            }
        } catch (Exception e) {
            System.out.println("Exception inBound");
            e.printStackTrace();
            return;
        }
    }

    public static NewPort getPort(Location l) {
        try {
            PortContainer pc = ports.get(((int) (l.getBlockX() / CONTAINER_SIZE) + (int) (l.getBlockZ() / CONTAINER_SIZE) * 10000));
            if (pc == null) {
                return null;
            } else {
                return pc.getPort(l);
            }
        } catch (Exception e) {
            System.out.println("Exception getPort");
            e.printStackTrace();
            return null;
        }
    }

    private static void sortPorts() {
        for (NewPort n : reference.values()) {
            n.genZoneBoundKeys();
        }
        if (ports.isEmpty()) {
            VoxelPort.log.warning("[VoxelPort] Portals have not been sorted.");
        } else {
            VoxelPort.log.info("[VoxelPort] Portal zones have been sorted into " + ports.size() + " containers.");
        }
    }

    public static void insertPort(NewPort n, int x, int z) {
        if (ports.containsKey((x + z * 10000))) {
            ports.get((x + z * 10000)).put(n);
        } else {
            ports.put((x + z * 10000), new PortContainer(n));
        }
    }

    public static void deletePort(NewPort n, int x, int z) {
        try {
            if (ports.get((x + z * 10000)).remove(n)) {
                ports.remove((x + z * 10000));
                VoxelPort.log.info("Removed x" + x + " z" + z + " " + n.getName());
            } else {
                VoxelPort.log.info("Removed port from: x" + x + " z" + z + " " + n.getName());
            }
        } catch (Exception e) {
            VoxelPort.log.warning("[VoxelPort] This should not have been printed. Something is not right, report this.");
            e.printStackTrace();
        }
    }

    private static void loadPortals() {
        try {
            File f = new File("plugins/VoxelPort/Ports");
            if (f.exists()) {
                File[] portfiles = f.listFiles();
                for (File file : portfiles) {
                    NewPort newport = new NewPort(file.getName());
                    if (newport.loaded()) {
                        reference.put(newport.getName(), newport);
                    }
                }
                if (reference.isEmpty()) {
                    VoxelPort.log.info("[VoxelPort] No portals were found.");
                } else {
                    VoxelPort.log.info("[VoxelPort] Portals loaded! " + reference.size() + " portals have been loaded.");
                    sortPorts();
                }
            } else { // ADD CONVERSION STUFF?
                f.mkdirs();
                File old = new File("VoxelPorts/");
                if (old.exists()) {
                    File[] oldPorts = old.listFiles();
                    for (File ol : oldPorts) {
                        oldPort op = new oldPort(ol.getName());
                        try {
                            NewPort np = new NewPort(op.zxh, op.zxl, op.zyh, op.zyl, op.zzh, op.zzl, op.r.getWorld().getName(), op.name, op.r, op.t);
                            np.setDepartures(op.disp);
                            np.saveData();
                            reference.put(np.getName(), np);
                            VoxelPort.log.info("[VoxelPort] Portal \"" + np.getName() + "\" has been sucessfully converted.");
                        } catch (Exception e) {
                            VoxelPort.log.warning("[VoxelPort] Error while converting old Portals to new.");
                        }
                        ol.delete();
                    }
                    File oldn = new File("portal_names");
                    oldn.delete();
                    old.delete();
                    VoxelPort.log.info("[VoxelPort] Old portal files cleaned up and deleted");
                }
            }
        } catch (Exception e) {
            VoxelPort.log.warning("[VoxelPort] Error while loading VoxelPorts");
            e.printStackTrace();
        }
    }

    private static void loadConfig() {
        try {
            File f = new File("plugins/VoxelPort/VoxelPortConfig.txt");
            if (f.exists()) {
                Scanner snr = new Scanner(f);
                if (snr.hasNext()) {
                    String nl = snr.nextLine();
                    if (nl.contains("version.")) {
                        double v = Double.parseDouble(nl.split("version.")[1]);
                        if (v != CONFIG_VERSION) {
                            VoxelPort.log.info("[VoxelPort] Updating Config file");
                            saveConfig();
                            return;
                        }
                    } else {
                        VoxelPort.log.info("[VoxelPort] Updating Config file");
                        saveConfig();
                        return;
                    }
                }
                while (snr.hasNext()) {
                    String str = snr.nextLine();
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.startsWith("PortTicketID")) {
                        TICKETID = Integer.parseInt(str.split(":")[1]);
                    }
                    if (str.startsWith("ContainerBlockSize")) {
                        CONTAINER_SIZE = Integer.parseInt(str.split(":")[1]);
                        VoxelPort.log.info("[VoxelPort] ContainerSize set to " + CONTAINER_SIZE);
                    }
                    if (str.startsWith("WalkingUpdateInterval")) {
                        CHECK_INTERVAL = Integer.parseInt(str.split(":")[1]);
                    }
                    if (str.startsWith("PortTickMillisecond")) {
                        PORT_TICK_SPEED = Integer.parseInt(str.split(":")[1]);
                        if (PORT_TICK_SPEED % 50 != 0) {
                            plugin.onDisable();
                            throw new IllegalArgumentException("PortTickSpeed set to an invalid value!");
                        }
                    }
                    if (str.startsWith("PortButtonTrigerBlockID")) {
                        String[] spli = str.split(":")[1].split("-");
                        BUTTON_BLOCK_ID = Integer.parseInt(spli[0]);
                        BUTTON_BLOCK_DATA = Byte.parseByte(spli[1]);
                    }
                }
                snr.close();
                VoxelPort.log.info("[VoxelPort] Config loaded");
            } else {
                VoxelPort.log.warning("[VoxelPort] Config file not found!");
                saveConfig();
            }
        } catch (Exception e) {
            VoxelPort.log.warning("[VoxelPort] Error while loading VoxelPortConfig.txt");
            e.printStackTrace();
        }
    }

    private static void loadTargets() {
        try {
            File f = new File("plugins/VoxelPort/VoxelPortTargets.txt");
            if (f.exists()) {
                Scanner snr = new Scanner(f);
                while (snr.hasNext()) {
                    String str = snr.nextLine();
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.contains(":")) {
                        String s[] = str.split(":");
                        if (reference.get(s[0]) != null && reference.get(s[1]) != null) portTargets.put(s[0], s[1]);
                    }
                }
                snr.close();
                VoxelPort.log.info("[VoxelPort] VoxelPortTargets.txt loaded");
            } else {
                VoxelPort.log.warning("[VoxelPort] VoxelPortTargets.txt not found!");
                saveTargets();
            }
        } catch (Exception e) {
            VoxelPort.log.warning("[VoxelPort] Error while loading VoxelPortTargets.txt");
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        try {
            File f = new File("plugins/VoxelPort/VoxelPortConfig.txt");

            f.getParentFile().mkdirs();
            f.createNewFile();
            PrintWriter pw = new PrintWriter(f);

            pw.write("#VoxelPort config file. version." + CONFIG_VERSION + "\r\n");
            pw.write("#Edit the values on the right to adjust them to your liking\r\n");
            pw.write("#The names depict exactly what each field represents\r\n");
            pw.write("#\r\n");
            pw.write("PortTicketID:334\r\n");
            pw.write("ContainerBlockSize:100\r\n");
            pw.write("WalkingUpdateInterval:2000\r\n");
            pw.write("PortTickMillisecond:5000\r\n");
            pw.write("PortButtonTrigerBlockID:73-4\r\n");
            pw.write("#\r\n");
            pw.write("#PortTickMilliseconds is the speed at which VoxelPort runs, or checks active Tickets.\r\n");
            pw.write("#Its required for this value to be a multiple of 50ms because 50ms == 1 CodeTime\r\n");
            pw.write("#PortButtonTrigerBlockID is the ID and Data value of the block that the button sits on\r\n");

            pw.close();
            VoxelPort.log.info("[VoxelPort] Config saved");
            loadConfig();

        } catch (Exception e) {
            VoxelPort.log.warning("[VoxelPort] Error while saving VoxelPortConfig.txt");
            e.printStackTrace();
        }
    }

    protected static void saveTargets() {
        try {
            File f = new File("plugins/VoxelPort/VoxelPortTargets.txt");

            f.getParentFile().mkdirs();
            f.createNewFile();
            PrintWriter pw = new PrintWriter(f);

            pw.write("#VoxelPort target port list\r\n");

            for (Map.Entry<String, String> m : portTargets.entrySet()) {
                pw.write(m.getKey() + ":" + m.getValue() + "\r\n");
            }

            pw.close();
            VoxelPort.log.info("[VoxelPort] Target list saved");
            loadConfig();
        }
        catch (Exception e) {
            VoxelPort.log.warning("[VoxelPort] Error while saving VoxelPortTargets.txt");
            e.printStackTrace();
        }
    }

    public void manageCommand(Player p, String[] s) {
        /*
         *
         */
        if (s[0].equalsIgnoreCase("set")) {
            PortData pd = data.get(p.getName());
            if (pd == null) {
                pd = new PortData(null);
            }

            if (s.length == 2) {
                NewPort np = reference.get(s[1]);
                pd.p = np;
                data.put(p.getName(), pd);
                p.sendMessage(ChatColor.GREEN + "Current VoxelPort has been set to \"" + np.getName() + "\"");
                return;
            } else {
                NewPort np = getPort(p.getLocation());

                if (np == null) {
                    NewPort npl = getPort(new HitBlox(p, p.getWorld()).getTargetBlock().getLocation());
                    
                    if (npl == null) {
                        p.sendMessage(ChatColor.RED + "You are not standing or looking at a VoxelPort!");
                        return;
                    } else {
                        pd.p = npl;
                        data.put(p.getName(), pd);
                        p.sendMessage(ChatColor.GREEN + "Current VoxelPort has been set to VoxelPort you are looking at (" + npl.getName() + ")");
                        return;
                    }
                } else {
                    pd.p = np;
                    data.put(p.getName(), pd);
                    p.sendMessage(ChatColor.GREEN + "Current VoxelPort has been set to VoxelPort at current location (" + np.getName() + ")");
                    return;
                }
            }
        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("point")) {
            Block target = new HitBlox(p, p.getWorld()).getTargetBlock();
            if (target == null) {
                p.sendMessage(ChatColor.RED + "This point is not valid!");
                return;
            }
            PortData pd = data.get(p.getName());
            if (pd == null) {
                pd = new PortData(target);
                data.put(p.getName(), pd);
                p.sendMessage(ChatColor.GREEN + "First point set.");
                return;
            }
            if (pd.a == null) {
                pd.a = target;
                p.sendMessage(ChatColor.GREEN + "First point set.");
            } else if (pd.b == null) {
                pd.b = target;
                p.sendMessage(ChatColor.GREEN + "Second point set.");
            } else {
                data.put(p.getName(), new PortData(null));
                p.sendMessage(ChatColor.GREEN + "Port points cleared.");
            }
            return;
        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("create")) {
            if (s.length < 2) {
                p.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }

            PortData pd = data.get(p.getName());
            if (pd == null || pd.a == null || pd.b == null) {
                p.sendMessage(ChatColor.RED + "Plese select two zone points before creating a portal.");
                return;
            }

            if (reference.containsKey(s[1])) {
                p.sendMessage(ChatColor.DARK_RED + "A portal with this nane already exists!");
                return;
            }

            NewPort np = new NewPort(
                    pd.a.getX(),
                    pd.b.getX(),
                    pd.a.getY(),
                    pd.b.getY(),
                    pd.a.getZ(),
                    pd.b.getZ(),
                    pd.a.getWorld().getName(),
                    pd.a.getWorld().getEnvironment(),
                    s[1]);

            np.saveData();

            pd.p = np;
            pd.a = null;
            pd.b = null;
            np.genZoneBoundKeys();

            reference.put(np.getName(), np);
            p.sendMessage(ChatColor.BLUE + "Portal \"" + np.getName() + "\" created succesfully.");
            return;
        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("target")) {
            if (s.length == 3) {
                NewPort np = reference.get(s[1]);
                if (np == null) {
                    p.sendMessage(ChatColor.RED + "No port with name " + s[1] + " found.");
                    return;
                }
                NewPort tp = reference.get(s[2]);
                if (tp == null) {
                    p.sendMessage(ChatColor.RED + "No port with name " + s[2] + " found.");
                    return;
                }
                if (tp.getArrival() == null) {
                    np.setDestination(null);
                } else {
                    np.setDestination(tp.getArrival());
                    np.saveData();
                }

                portTargets.put(np.getName(), tp.getName());
                saveTargets();
                p.sendMessage(ChatColor.GREEN + "target location for Port \"" + np.getName() + "\" has been set to Port \"" + tp.getName() + "\"");

                return;
            }
            if (s.length < 2) {
                PortData pd = data.get(p.getName());
                if (pd != null && pd.p != null) {
                    pd.p.setDestination(p.getLocation());
                    pd.p.saveData();
                    portTargets.remove(pd.p.getName());
                    saveTargets();
                    p.sendMessage(ChatColor.GREEN + "Target location for Port \"" + pd.p.getName() + "\" has been set to current location.");
                } else {
                    p.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                }
                return;
            }

            NewPort np = reference.get(s[1]);

            if (np != null) {
                np.setDestination(p.getLocation());
                np.saveData();
                portTargets.remove(np.getName());
                saveTargets();
                p.sendMessage(ChatColor.GREEN + "Target location for Port \"" + np.getName() + "\" has been set to current location.");
                return;
            } else {
                p.sendMessage(ChatColor.RED + "A port with this name doesn't exist.");
                return;
            }
        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("targetWorld")) {
            if (s.length < 3) {
                p.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            } else {
                NewPort np = reference.get(s[1]);
                if (np == null) {
                    p.sendMessage(ChatColor.RED + "No port with name " + s[1] + " found.");
                    return;
                }
                if (s.length == 4) {
                    switch (s[3].charAt(0)) {
                        case 'd':
                            np.setDestination(VoxelPort.s.getWorld(s[2]).getSpawnLocation());
                            np.saveData();
                            portTargets.remove(np.getName());
                            saveTargets();
                            p.sendMessage(ChatColor.GREEN + "Target location for Port \"" + np.getName() + "\" has been set to origin of world \"" + s[2] + "\"");
                            return;

                        case 'n':
                            np.setDestination(VoxelPort.s.getWorld(s[2]).getSpawnLocation());
                            np.saveData();
                            portTargets.remove(np.getName());
                            saveTargets();
                            p.sendMessage(ChatColor.GREEN + "Target location for Port \"" + np.getName() + "\" has been set to origin of nether world \"" + s[2] + "\"");
                            return;

                        default:
                            p.sendMessage(ChatColor.RED + "Invalid parameters! use \"default\" or \"nether\"");
                            return;
                    }
                } else {
                    np.setDestination(VoxelPort.s.getWorld(s[2]).getSpawnLocation());
                    np.saveData();
                    portTargets.remove(np.getName());
                    saveTargets();
                    p.sendMessage(ChatColor.GREEN + "Target location for Port \"" + np.getName() + "\" has been set to origin of world \"" + s[2] + "\"");
                    return;
                }
            }
        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("arrive")) {
            Location arrivalLocation = p.getLocation();
            
            NewPort np = null;
            if (s.length < 2) {
                // The command omits a port name, so apply it to the port in the "working slot"
                PortData pd = data.get(p.getName());
                if (pd != null && pd.p != null) {
                    np = pd.p;
                } else {
                    p.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                    return;
                }
            } else {
                np = reference.get(s[1]);
            }

            if (np != null) {
                if (s.length >= 3 && s[2].equalsIgnoreCase("clear")) {
                    arrivalLocation = null;
                    p.sendMessage(ChatColor.GREEN + "Arrival location for Port \"" + np.getName() + "\" has been cleared.");
                } else {       
                    p.sendMessage(ChatColor.GREEN + "Arrival location for Port \"" + np.getName() + "\" has been set to current location.");
                }

                np.setArrival(arrivalLocation);
                np.saveData();

                // Automatically keep port destinations synced with their targets' arrival locations
                for (Map.Entry<String, String> m : portTargets.entrySet()) {
                    if (m.getValue().equalsIgnoreCase(np.getName())) {
                        NewPort linkedPort = reference.get(m.getKey());
                        linkedPort.setDestination(arrivalLocation);
                        linkedPort.saveData();
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED + "A port with this name doesn't exist.");
            }
            return;
        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("info")) {
            if (s.length < 2) {
                PortData pd = data.get(p.getName());
                if (pd != null && pd.p != null) {
                    pd.p.printInfo(p);
                } else {
                    p.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                }
                return;
            }

            NewPort np = reference.get(s[1]);

            if (np != null) {
                np.printInfo(p);
            } else {
                p.sendMessage(ChatColor.RED + "A port with this name doesn't exist.");
            }
            return;
        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("disp")) {
            if (s.length < 3) {
                PortData pd = data.get(p.getName());
                if (pd != null && pd.p != null) {
                    if (s[1].equalsIgnoreCase("clear")) {
                        pd.p.clearDepartures();
                        p.sendMessage(ChatColor.GREEN + "Departures for port \"" + pd.p.getName() + "\" cleared.");
                        return;
                    }
                    pd.p.addDeparture(Integer.parseInt(s[1]));
                    pd.p.saveData();
                    p.sendMessage(ChatColor.GREEN + "Departures time " + s[1] + " added to port \"" + pd.p.getName() + "\"");
                } else {
                    p.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                }
                return;
            }

            NewPort np = reference.get(s[1]);

            if (np != null) {
                if (s[2].equalsIgnoreCase("clear")) {
                    np.clearDepartures();
                    p.sendMessage(ChatColor.GREEN + "Departures for port \"" + np.getName() + "\" cleared.");
                    return;
                }
                np.addDeparture(Integer.parseInt(s[2]));
                np.saveData();
                p.sendMessage(ChatColor.GREEN + "Departure time " + s[2] + " added to port \"" + np.getName() + "\"");
            } else {
                p.sendMessage(ChatColor.RED + "A port with this name doesn't exist.");
            }
            return;
        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("genDisp")) {
            if (s.length < 3) {
                p.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }

            NewPort np = reference.get(s[1]);
            if (np == null) {
                p.sendMessage(ChatColor.RED + "No port with name " + s[1] + " found.");
                return;
            }

            if (s.length == 3) {
                np.generateDepartures(0, Integer.parseInt(s[2]), p);
                np.saveData();
                p.sendMessage(ChatColor.GREEN + "Dispatch times for VoxelPort \"" + np.getName() + "\" have been generated starting at 0 with intervals of " + s[2]);
            } else if (s.length == 4) {
                np.generateDepartures(Integer.parseInt(s[3]), Integer.parseInt(s[2]), p);
                np.saveData();
                p.sendMessage(ChatColor.GREEN + "Dispatch times for VoxelPort \"" + np.getName() + "\" have been generated starting at " + s[3] + " with intervals of " + s[2]);
            }
            return;
        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("instaPort")) {
            if (s.length < 2) {
                p.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }
            if (s.length == 2) {
                PortData pd = data.get(p.getName());
                if (pd != null && pd.p != null) {
                    pd.p.setInstant(Boolean.parseBoolean(s[1]));
                    pd.p.saveData();
                    p.sendMessage(ChatColor.GREEN + "InstaPort has been set to " + s[1] + " for VoxelPort \"" + pd.p.getName() + "\"");
                } else {
                    p.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                }
                return;
            }

            NewPort np = reference.get(s[1]);
            if (np == null) {
                p.sendMessage(ChatColor.RED + "No port with name " + s[1] + " found.");
                return;
            }

            np.setInstant(Boolean.parseBoolean(s[2]));
            np.saveData();
            p.sendMessage(ChatColor.GREEN + "InstaPort has been set to " + s[2] + " for VoxelPort \"" + np.getName() + "\"");
            return;
        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("requireTicket")) {
            if (s.length < 2) {
                p.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }
            if (s.length == 2) {
                PortData pd = data.get(p.getName());
                if (pd != null && pd.p != null) {
                    pd.p.setTicket(Boolean.parseBoolean(s[1]));
                    pd.p.saveData();
                    p.sendMessage(ChatColor.GREEN + "TicketRequirement has been set to " + s[1] + " for VoxelPort \"" + pd.p.getName() + "\"");
                } else {
                    p.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                }
                return;
            }

            NewPort np = reference.get(s[1]);
            if (np == null) {
                p.sendMessage(ChatColor.RED + "No port with name " + s[1] + " found.");
                return;
            }

            np.setTicket(Boolean.parseBoolean(s[2]));
            np.saveData();
            p.sendMessage(ChatColor.GREEN + "TicketRequirement has been set to " + s[2] + " for VoxelPort \"" + np.getName() + "\"");
            return;
        }
        /*
         *
         */
//        if (s[0].equalsIgnoreCase("depart")) {
//            newPort np = getPort(p.getLocation());
//            if (np == null) {
//                p.sendMessage(ChatColor.RED + "You are not inside a VoxelPort!");
//                return;
//            } else {
//                np.instaPort(p);
//                return;
//            }
//        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("welcomeClear")) {
            if (s.length < 2) {
                p.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }

            NewPort np = reference.get(s[1]);
            if (np == null) {
                p.sendMessage(ChatColor.RED + "No port with name " + s[1] + " found.");
                return;
            } else {
                np.clearMessages();
                np.saveData();
                p.sendMessage(ChatColor.GREEN + "Welcome messages for VoxelPort " + np.getName() + " have been cleared!");
                return;
            }
        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("welcome")) {
            if (s.length <= 2) {
                p.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }

            NewPort np = reference.get(s[1]);
            if (np == null) {
                p.sendMessage(ChatColor.RED + "No port with name " + s[1] + " found.");
                return;
            } else {
                String message = "";
                for (int x = 2; x < s.length; x++) {
                    if (s[x].startsWith("#$")) {
                        if (Character.isDigit(s[x].charAt(2))) {
                            message += ChatColor.getByChar(s[x].charAt(2));
                            message += s[x].substring(3) + " ";
                        } else {
                            int i;
                            char c = s[x].charAt(2);

                            switch (c) {
                                case 'a':
                                    i = 10;
                                    message += ChatColor.getByChar(c);
                                    message += s[x].substring(3) + " ";
                                    break;

                                case 'b':
                                    i = 11;
                                    message += ChatColor.getByChar(c);
                                    message += s[x].substring(3) + " ";
                                    break;

                                case 'c':
                                    i = 12;
                                    message += ChatColor.getByChar(c);
                                    message += s[x].substring(3) + " ";
                                    break;

                                case 'd':
                                    i = 13;
                                    message += ChatColor.getByChar(c);
                                    message += s[x].substring(3) + " ";
                                    break;

                                case 'e':
                                    i = 14;
                                    message += ChatColor.getByChar(c);
                                    message += s[x].substring(3) + " ";
                                    break;

                                case 'f':
                                    i = 15;
                                    message += ChatColor.getByChar(c);
                                    message += s[x].substring(3) + " ";
                                    break;

                                default:
                                    p.sendMessage(ChatColor.RED + "Invalid Colour String! \"" + s[x] + "\"");
                                    return;
                            }
                        }
                    } else {
                        message += s[x] + " ";
                    }
                }
                np.addMessage(message);
                np.saveData();
                p.sendMessage(ChatColor.BLUE + "Message added to VoxelPort " + np.getName());
                p.sendMessage(message);
                return;
            }
        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("delete")) {
            if (s.length < 2) {
                p.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }

            NewPort np = reference.get(s[1]);
            if (np == null) {
                p.sendMessage(ChatColor.RED + "No port with name " + s[1] + " found.");
                return;
            } else {
                File port = new File("plugins/VoxelPort/Ports/" + np.getName());
                if (PortTick.tickets.containsValue(np)) {
                    PortTick.removeTicketsFor(np.getName());
                }
                np.deleteZoneBoundKeys();
                reference.remove(np.getName());
                port.delete();
                p.sendMessage(ChatColor.GREEN + "VoxelPort " + np.getName() + " has been deleted!");
                return;
            }
        }
        /*
         *
         */
        if (s[0].equalsIgnoreCase("zone")) {
            PortData pd = data.get(p.getName());
            if (pd == null || pd.a == null || pd.b == null) {
                p.sendMessage(ChatColor.RED + "Plese select two zone points before changing the zone.");
                return;
            }

            if (s.length < 2) {
                if (pd.p == null) {
                    p.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                    return;
                }

                pd.p.setZone(new Zone(
                        (pd.a.getX() > pd.b.getX() ? pd.a.getX() : pd.b.getX()),
                        (pd.a.getX() < pd.b.getX() ? pd.a.getX() : pd.b.getX()),
                        (pd.a.getY() > pd.b.getY() ? pd.a.getY() : pd.b.getY()),
                        (pd.a.getY() < pd.b.getY() ? pd.a.getY() : pd.b.getY()),
                        (pd.a.getZ() > pd.b.getZ() ? pd.a.getZ() : pd.b.getZ()),
                        (pd.a.getZ() < pd.b.getZ() ? pd.a.getZ() : pd.b.getZ()),
                        pd.a.getWorld().getName(),
                        pd.a.getWorld().getEnvironment()));
                pd.p.saveData();
                p.sendMessage(ChatColor.GREEN + "Zone set for VoxelPort " + ChatColor.GOLD + pd.p.getName());
                return;
            } else {
                NewPort np = reference.get(s[1]);
                if (np == null) {
                    p.sendMessage(ChatColor.RED + "No port with name " + s[1] + " found.");
                    return;
                } else {
                    np.setZone(new Zone(
                            (pd.a.getX() > pd.b.getX() ? pd.a.getX() : pd.b.getX()),
                            (pd.a.getX() < pd.b.getX() ? pd.a.getX() : pd.b.getX()),
                            (pd.a.getY() > pd.b.getY() ? pd.a.getY() : pd.b.getY()),
                            (pd.a.getY() < pd.b.getY() ? pd.a.getY() : pd.b.getY()),
                            (pd.a.getZ() > pd.b.getZ() ? pd.a.getZ() : pd.b.getZ()),
                            (pd.a.getZ() < pd.b.getZ() ? pd.a.getZ() : pd.b.getZ()),
                            pd.a.getWorld().getName(),
                            pd.a.getWorld().getEnvironment()));
                    np.saveData();
                    p.sendMessage(ChatColor.GREEN + "Zone set for VoxelPort " + ChatColor.GOLD + np.getName());
                    return;
                }
            }
        }
        if (s[0].equalsIgnoreCase("redstoneKey")) {
            if (s.length == 1) {
                p.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }

            PortData pd = data.get(p.getName());

            if (s[1].equalsIgnoreCase("set")) {
                if (pd == null || pd.a == null) {
                    p.sendMessage(ChatColor.RED + "Please select Block A with /point first!");
                    return;
                }

                if (s.length == 3) {
                    NewPort np = reference.get(s[2]);
                    if (np == null) {
                        p.sendMessage(ChatColor.RED + "No port with name " + s[2] + " found.");
                        return;
                    }

                    np.setRedstoneKey(pd.a.getLocation());
                    pd.a = null;
                    np.saveData();
                    p.sendMessage(ChatColor.GREEN + "RedstoneKey set for VoxelPort " + ChatColor.GOLD + np.getName());
                    return;
                }

                if (pd.p == null) {
                    p.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                    return;
                }

                pd.p.setRedstoneKey(pd.a.getLocation());
                pd.a = null;
                pd.p.saveData();
                p.sendMessage(ChatColor.GREEN + "RedstoneKey set for VoxelPort " + ChatColor.GOLD + pd.p.getName());
                return;
            } else if (s[1].equalsIgnoreCase("clear")) {
                if (s.length == 3) {
                    NewPort np = reference.get(s[2]);
                    if (np == null) {
                        p.sendMessage(ChatColor.RED + "No port with name " + s[2] + " found.");
                        return;
                    }

                    np.setRedstoneKey(null);
                    np.saveData();
                    p.sendMessage(ChatColor.GREEN + "RedstoneKey cleared for VoxelPort " + ChatColor.GOLD + np.getName());
                    return;
                }

                if (pd == null || pd.p == null) {
                    p.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                    return;
                }

                pd.p.setRedstoneKey(null);
                pd.p.saveData();
                p.sendMessage(ChatColor.GREEN + "RedstoneKey cleared for VoxelPort " + ChatColor.GOLD + pd.p.getName());
                return;
            }

            return;
        }
    }

    private class PortData {

        public PortData(Block t) {
            a = t;
        }
        public Block a;
        public Block b;
        public NewPort p;
    }
}
