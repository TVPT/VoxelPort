package com.thevoxelbox.voxelport;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * @author Voxel
 */
public class PortManager
{

    private static TreeMap<Integer, PortContainer> ports = new TreeMap<Integer, PortContainer>();
    private static HashMap<String, Port> reference = new HashMap<String, Port>();
    private static PortTick portTick;
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

    public PortManager(final VoxelPort vp)
    {
        PortManager.plugin = vp;
        PortManager.loadConfig();
        PortManager.loadPortals();
        VoxelPort.log.info("Starting thread...");
        PortManager.portTick = new PortTick(PortManager.PORT_TICK_SPEED);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(PortManager.plugin, PortManager.portTick, PortTick.codeTick, PortTick.codeTick <= 1 ? 2 : PortTick.codeTick);
        VoxelPort.log.info("Thread Started!");
    }

    public static void inBound(final Player player, final Location loc)
    {
        try
        {
            final PortContainer portCont = PortManager.ports.get((loc.getBlockX() / PortManager.CONTAINER_SIZE + ((loc.getBlockZ() / PortManager.CONTAINER_SIZE) * 10000)));
            if (portCont == null)
            {
                return;
            } else
            {
                portCont.check(player, loc);
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            return;
        }
    }

    public static Port getPort(final Location loc)
    {
        try
        {
            final PortContainer pc = PortManager.ports.get((loc.getBlockX() / PortManager.CONTAINER_SIZE + (loc.getBlockZ() / PortManager.CONTAINER_SIZE * 10000)));
            if (pc == null)
            {
                return null;
            } else
            {
                return pc.getPortAtLoc(loc);
            }
        }
        catch (final Exception e)
        {
            System.out.println("Exception getPort");
            e.printStackTrace();
            return null;
        }
    }

    private static void sortPorts()
    {
        for (final Port port : PortManager.reference.values())
        {
            port.genZoneBoundKeys();
        }
        if (PortManager.ports.isEmpty())
        {
            VoxelPort.log.warning("[VoxelPort] Portals have not been sorted.");
        } else
        {
            VoxelPort.log.info("[VoxelPort] Portal zones have been sorted into " + PortManager.ports.size() + " containers.");
        }
    }

    public static void insertPort(final Port n, final int x, final int z)
    {
        if (PortManager.ports.containsKey((x + (z * 10000))))
        {
            PortManager.ports.get((x + (z * 10000))).put(n);
        } else
        {
            PortManager.ports.put((x + (z * 10000)), new PortContainer(n));
        }
    }

    public static void deletePort(final Port oldPort, final int x, final int z)
    {
        try
        {
            if (PortManager.ports.get((x + (z * 10000))).remove(oldPort))
            {
                PortManager.ports.remove((x + (z * 10000)));
                VoxelPort.log.info("Removed x" + x + " z" + z + " " + oldPort.getName());
            } else
            {
                VoxelPort.log.info("Removed port from: x" + x + " z" + z + " " + oldPort.getName());
            }
        }
        catch (final Exception e)
        {
            VoxelPort.log.warning("Error: could not remove port");
            e.printStackTrace();
        }
    }

    private static void loadPortals()
    {
        try
        {
            final File f = new File("plugins/VoxelPort/Ports");
            if (f.exists())
            {
                final File[] portfiles = f.listFiles();
                for (final File file : portfiles)
                {
                    final Port newport = new Port(file.getName());
                    if (newport.loaded())
                    {
                        PortManager.reference.put(newport.getName(), newport);
                    }
                }
                if (PortManager.reference.isEmpty())
                {
                    VoxelPort.log.info("No portals were found.");
                } else
                {
                    VoxelPort.log.info("Portals loaded! " + PortManager.reference.size() + " portals have been loaded.");
                    PortManager.sortPorts();
                }
            } else
            { // ADD CONVERSION STUFF?
                f.mkdirs();
                final File old = new File("VoxelPorts/");
                if (old.exists())
                {
                    final File[] oldPorts = old.listFiles();
                    for (final File ol : oldPorts)
                    {
                        final oldPort op = new oldPort(ol.getName());
                        try
                        {
                            final Port np = new Port(op.zxh, op.zxl, op.zyh, op.zyl, op.zzh, op.zzl, op.r.getWorld().getName(), op.name, op.r, op.t);
                            np.setDepartures(op.disp);
                            np.saveData();
                            PortManager.reference.put(np.getName(), np);
                            VoxelPort.log.info("Portal \"" + np.getName() + "\" has been successfully converted.");
                        }
                        catch (final Exception e)
                        {
                            VoxelPort.log.warning("Error while converting old Portals to new.");
                        }
                        ol.delete();
                    }
                    final File oldn = new File("portal_names");
                    oldn.delete();
                    old.delete();
                    VoxelPort.log.info("Old portal files cleaned up and deleted");
                }
            }
        }
        catch (final Exception e)
        {
            VoxelPort.log.warning("[VoxelPort] Error while loading VoxelPorts");
            e.printStackTrace();
        }
    }

    private static void loadConfig()
    {
        final File f = new File("plugins/VoxelPort/VoxelPortConfig.txt");
        try(Scanner snr = new Scanner(f))
        {
            if (f.exists())
            {
                if (snr.hasNext())
                {
                    final String nl = snr.nextLine();
                    if (nl.contains("version."))
                    {
                        final double v = Double.parseDouble(nl.split("version.")[1]);
                        if (v != PortManager.CONFIG_VERSION)
                        {
                            VoxelPort.log.info("[VoxelPort] Updating Config file");
                            PortManager.saveConfig();
                            return;
                        }
                    } else
                    {
                        VoxelPort.log.info("[VoxelPort] Updating Config file");
                        PortManager.saveConfig();
                        return;
                    }
                }
                while (snr.hasNext())
                {
                    final String str = snr.nextLine();
                    if (str.startsWith("#"))
                    {
                        continue;
                    }
                    if (str.startsWith("PortTicketID"))
                    {
                        PortManager.TICKETID = Integer.parseInt(str.split(":")[1]);
                    }
                    if (str.startsWith("ContainerBlockSize"))
                    {
                        PortManager.CONTAINER_SIZE = Integer.parseInt(str.split(":")[1]);
                        VoxelPort.log.info("[VoxelPort] ContainerSize set to " + PortManager.CONTAINER_SIZE);
                    }
                    if (str.startsWith("WalkingUpdateInterval"))
                    {
                        PortManager.CHECK_INTERVAL = Integer.parseInt(str.split(":")[1]);
                    }
                    if (str.startsWith("PortTickMillisecond"))
                    {
                        PortManager.PORT_TICK_SPEED = Integer.parseInt(str.split(":")[1]);
                        if ((PortManager.PORT_TICK_SPEED % 50) != 0)
                        {
                            PortManager.plugin.onDisable();
                            throw new IllegalArgumentException("PortTickSpeed set to an invalid value!");
                        }
                    }
                    if (str.startsWith("PortButtonTrigerBlockID"))
                    {
                        final String[] spli = str.split(":")[1].split("-");
                        PortManager.BUTTON_BLOCK_ID = Integer.parseInt(spli[0]);
                        PortManager.BUTTON_BLOCK_DATA = Byte.parseByte(spli[1]);
                    }
                }
                snr.close();
                VoxelPort.log.info("[VoxelPort] Config loaded");
            } else
            {
                VoxelPort.log.warning("[VoxelPort] Config file not found!");
                PortManager.saveConfig();
            }
        }
        catch (final Exception e)
        {
            VoxelPort.log.warning("[VoxelPort] Error while loading VoxelPortConfig.txt");
            e.printStackTrace();
        }
    }

    public static void saveConfig()
    {
        try
        {
            final File f = new File("plugins/VoxelPort/VoxelPortConfig.txt");

            f.getParentFile().mkdirs();
            f.createNewFile();
            final PrintWriter pw = new PrintWriter(f);

            pw.write("#VoxelPort config file. version." + PortManager.CONFIG_VERSION + "\r\n");
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
            PortManager.loadConfig();

        }
        catch (final Exception e)
        {
            VoxelPort.log.warning("[VoxelPort] Error while saving VoxelPortConfig.txt");
            e.printStackTrace();
        }
    }

    public void manageCommand(final Player player, final String[] args)
    {
        /*
         *
         */
        if (args[0].equalsIgnoreCase("set"))
        {
            PortData portData = PortManager.data.get(player.getName());
            if (portData == null)
            {
                portData = new PortData(null);
            }

            if (args.length == 2)
            {
                final Port newPort = PortManager.reference.get(args[1]);
                portData.p = newPort;
                PortManager.data.put(player.getName(), portData);
                player.sendMessage(ChatColor.GREEN + "Current VoxelPort has been set to \"" + newPort.getName() + "\"");
                return;
            } else
            {
                final Port np = PortManager.getPort(player.getLocation());

                if (np == null)
                {
                    final Port npl = PortManager.getPort(new HitBlox(player, player.getWorld()).getTargetBlock().getLocation());

                    if (npl == null)
                    {
                        player.sendMessage(ChatColor.RED + "You are not standing or looking at a VoxelPort!");
                        return;
                    } else
                    {
                        portData.p = npl;
                        PortManager.data.put(player.getName(), portData);
                        player.sendMessage(ChatColor.GREEN + "Current VoxelPort has been set to VoxelPort you are looking at (" + npl.getName() + ")");
                        return;
                    }
                } else
                {
                    portData.p = np;
                    PortManager.data.put(player.getName(), portData);
                    player.sendMessage(ChatColor.GREEN + "Current VoxelPort has been set to VoxelPort at current location (" + np.getName() + ")");
                    return;
                }
            }
        }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("point"))
        {
            final Block target = new HitBlox(player, player.getWorld()).getTargetBlock();
            if (target == null)
            {
                player.sendMessage(ChatColor.RED + "This point is not valid!");
                return;
            }
            PortData pd = PortManager.data.get(player.getName());
            if (pd == null)
            {
                pd = new PortData(target);
                PortManager.data.put(player.getName(), pd);
                player.sendMessage(ChatColor.GREEN + "First point set.");
                return;
            }
            if (pd.a == null)
            {
                pd.a = target;
                player.sendMessage(ChatColor.GREEN + "First point set.");
            } else if (pd.b == null)
            {
                pd.b = target;
                player.sendMessage(ChatColor.GREEN + "Second point set.");
            } else
            {
                PortManager.data.put(player.getName(), new PortData(null));
                player.sendMessage(ChatColor.GREEN + "Port points cleared.");
            }
            return;
        }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("create"))
        {
            if (args.length < 2)
            {
                player.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }

            final PortData pd = PortManager.data.get(player.getName());
            if ((pd == null) || (pd.a == null) || (pd.b == null))
            {
                player.sendMessage(ChatColor.RED + "Plese select two zone points before creating a portal.");
                return;
            }

            if (PortManager.reference.containsKey(args[1]))
            {
                player.sendMessage(ChatColor.DARK_RED + "A portal with this nane already exists!");
                return;
            }

            final Port np = new Port((pd.a.getX() > pd.b.getX() ? pd.a.getX() : pd.b.getX()), (pd.a.getX() < pd.b.getX() ? pd.a.getX() : pd.b.getX()), (pd.a.getY() > pd.b.getY() ? pd.a.getY() : pd.b.getY()), (pd.a.getY() < pd.b.getY() ? pd.a.getY()
                    : pd.b.getY()), (pd.a.getZ() > pd.b.getZ() ? pd.a.getZ() : pd.b.getZ()), (pd.a.getZ() < pd.b.getZ() ? pd.a.getZ() : pd.b.getZ()), pd.a.getWorld().getName(), pd.a.getWorld().getEnvironment(), args[1]);

            np.saveData();

            pd.p = np;
            pd.a = null;
            pd.b = null;
            np.genZoneBoundKeys();

            PortManager.reference.put(np.getName(), np);
            player.sendMessage(ChatColor.BLUE + "Portal \"" + np.getName() + "\" created succesfully.");
            return;
        }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("target"))
        {
            if (args.length == 3)
            {
                final Port np = PortManager.reference.get(args[1]);
                if (np == null)
                {
                    player.sendMessage(ChatColor.RED + "No port with name " + args[1] + " found.");
                    return;
                }
                final Port tp = PortManager.reference.get(args[2]);
                if (tp == null)
                {
                    player.sendMessage(ChatColor.RED + "No port with name " + args[2] + " found.");
                    return;
                }
                if (tp.getArrival() == null)
                {
                    player.sendMessage(ChatColor.RED + "The target portal " + args[2] + " doesn't contain an arrival location");
                    return;
                }
                np.setDestination(tp.getArrival());
                np.saveData();
                player.sendMessage(ChatColor.GREEN + "target location for Port \"" + np.getName() + "\" has been set to arrival location of Port \"" + tp.getName() + "\"");
                return;
            }
            if (args.length < 2)
            {
                final PortData pd = PortManager.data.get(player.getName());
                if ((pd != null) && (pd.p != null))
                {
                    pd.p.setDestination(player.getLocation());
                    pd.p.saveData();
                    player.sendMessage(ChatColor.GREEN + "Target location for Port \"" + pd.p.getName() + "\" has been set to current location.");
                } else
                {
                    player.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                }
                return;
            }

            final Port np = PortManager.reference.get(args[1]);

            if (np != null)
            {
                np.setDestination(player.getLocation());
                np.saveData();
                player.sendMessage(ChatColor.GREEN + "Target location for Port \"" + np.getName() + "\" has been set to current location.");
                return;
            } else
            {
                player.sendMessage(ChatColor.RED + "A port with this name doesn't exist.");
                return;
            }
        }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("targetWorld"))
        {
            if (args.length < 3)
            {
                player.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            } else
            {
                final Port np = PortManager.reference.get(args[1]);
                if (np == null)
                {
                    player.sendMessage(ChatColor.RED + "No port with name " + args[1] + " found.");
                    return;
                }
                if (args.length == 4)
                {
                    switch (args[3].charAt(0))
                    {
                        case 'd':
                            np.setDestination(Bukkit.getWorld(args[2]).getSpawnLocation());
                            np.saveData();
                            player.sendMessage(ChatColor.GREEN + "Target location for Port \"" + np.getName() + "\" has been set to origin of world \"" + args[2] + "\"");
                            return;

                        case 'n':
                            np.setDestination(Bukkit.getWorld(args[2]).getSpawnLocation());
                            np.saveData();
                            player.sendMessage(ChatColor.GREEN + "Target location for Port \"" + np.getName() + "\" has been set to origin of nether world \"" + args[2] + "\"");
                            return;

                        default:
                            player.sendMessage(ChatColor.RED + "Invalid parameters! use \"default\" or \"nether\"");
                            return;
                    }
                } else
                {
                    np.setDestination(Bukkit.getWorld(args[2]).getSpawnLocation());
                    np.saveData();
                    player.sendMessage(ChatColor.GREEN + "Target location for Port \"" + np.getName() + "\" has been set to origin of world \"" + args[2] + "\"");
                    return;
                }
            }
        }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("arrive"))
        {
            if (args.length < 2)
            {
                final PortData pd = PortManager.data.get(player.getName());
                if ((pd != null) && (pd.p != null))
                {
                    pd.p.setArrival(player.getLocation());
                    pd.p.saveData();
                    player.sendMessage(ChatColor.GREEN + "Arrival location for Port \"" + pd.p.getName() + "\" has been set to current location.");
                } else
                {
                    player.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                }
                return;
            }

            final Port np = PortManager.reference.get(args[1]);

            if (np != null)
            {
                np.setArrival(player.getLocation());
                np.saveData();
                player.sendMessage(ChatColor.GREEN + "Arrival location for Port \"" + np.getName() + "\" has been set to current location.");
            } else
            {
                player.sendMessage(ChatColor.RED + "A port with this name doesn't exist.");
            }
            return;
        }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("info"))
        {
            if (args.length < 2)
            {
                final PortData pd = PortManager.data.get(player.getName());
                if ((pd != null) && (pd.p != null))
                {
                    pd.p.printInfo(player);
                } else
                {
                    player.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                }
                return;
            }

            final Port np = PortManager.reference.get(args[1]);

            if (np != null)
            {
                np.printInfo(player);
            } else
            {
                player.sendMessage(ChatColor.RED + "A port with this name doesn't exist.");
            }
            return;
        }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("disp"))
        {
            if (args.length < 3)
            {
                final PortData pd = PortManager.data.get(player.getName());
                if ((pd != null) && (pd.p != null))
                {
                    if (args[1].equalsIgnoreCase("clear"))
                    {
                        pd.p.clearDepartures();
                        player.sendMessage(ChatColor.GREEN + "Departures for port \"" + pd.p.getName() + "\" cleared.");
                        return;
                    }
                    pd.p.addDeparture(Integer.parseInt(args[1]));
                    pd.p.saveData();
                    player.sendMessage(ChatColor.GREEN + "Departures time " + args[1] + " added to port \"" + pd.p.getName() + "\"");
                } else
                {
                    player.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                }
                return;
            }

            final Port np = PortManager.reference.get(args[1]);

            if (np != null)
            {
                if (args[2].equalsIgnoreCase("clear"))
                {
                    np.clearDepartures();
                    player.sendMessage(ChatColor.GREEN + "Departures for port \"" + np.getName() + "\" cleared.");
                    return;
                }
                np.addDeparture(Integer.parseInt(args[2]));
                np.saveData();
                player.sendMessage(ChatColor.GREEN + "Departure time " + args[2] + " added to port \"" + np.getName() + "\"");
            } else
            {
                player.sendMessage(ChatColor.RED + "A port with this name doesn't exist.");
            }
            return;
        }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("genDisp"))
        {
            if (args.length < 3)
            {
                player.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }

            final Port np = PortManager.reference.get(args[1]);
            if (np == null)
            {
                player.sendMessage(ChatColor.RED + "No port with name " + args[1] + " found.");
                return;
            }

            if (args.length == 3)
            {
                np.generateDepartures(0, Integer.parseInt(args[2]), player);
                np.saveData();
                player.sendMessage(ChatColor.GREEN + "Dispatch times for VoxelPort \"" + np.getName() + "\" have been generated starting at 0 with intervals of " + args[2]);
            } else if (args.length == 4)
            {
                np.generateDepartures(Integer.parseInt(args[3]), Integer.parseInt(args[2]), player);
                np.saveData();
                player.sendMessage(ChatColor.GREEN + "Dispatch times for VoxelPort \"" + np.getName() + "\" have been generated starting at " + args[3] + " with intervals of " + args[2]);
            }
            return;
        }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("instaPort"))
        {
            if (args.length < 2)
            {
                player.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }
            if (args.length == 2)
            {
                final PortData pd = PortManager.data.get(player.getName());
                if ((pd != null) && (pd.p != null))
                {
                    pd.p.setInstant(Boolean.parseBoolean(args[1]));
                    pd.p.saveData();
                    player.sendMessage(ChatColor.GREEN + "InstaPort has been set to " + args[1] + " for VoxelPort \"" + pd.p.getName() + "\"");
                } else
                {
                    player.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                }
                return;
            }

            final Port np = PortManager.reference.get(args[1]);
            if (np == null)
            {
                player.sendMessage(ChatColor.RED + "No port with name " + args[1] + " found.");
                return;
            }

            np.setInstant(Boolean.parseBoolean(args[2]));
            np.saveData();
            player.sendMessage(ChatColor.GREEN + "InstaPort has been set to " + args[2] + " for VoxelPort \"" + np.getName() + "\"");
            return;
        }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("requireTicket"))
        {
            if (args.length < 2)
            {
                player.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }
            if (args.length == 2)
            {
                final PortData pd = PortManager.data.get(player.getName());
                if ((pd != null) && (pd.p != null))
                {
                    pd.p.setTicket(Boolean.parseBoolean(args[1]));
                    pd.p.saveData();
                    player.sendMessage(ChatColor.GREEN + "TicketRequirement has been set to " + args[1] + " for VoxelPort \"" + pd.p.getName() + "\"");
                } else
                {
                    player.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                }
                return;
            }

            final Port np = PortManager.reference.get(args[1]);
            if (np == null)
            {
                player.sendMessage(ChatColor.RED + "No port with name " + args[1] + " found.");
                return;
            }

            np.setTicket(Boolean.parseBoolean(args[2]));
            np.saveData();
            player.sendMessage(ChatColor.GREEN + "TicketRequirement has been set to " + args[2] + " for VoxelPort \"" + np.getName() + "\"");
            return;
        }
        /*
         *
         */
        // if (s[0].equalsIgnoreCase("depart")) {
        // newPort np = getPort(p.getLocation());
        // if (np == null) {
        // p.sendMessage(ChatColor.RED + "You are not inside a VoxelPort!");
        // return;
        // } else {
        // np.instaPort(p);
        // return;
        // }
        // }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("welcomeClear"))
        {
            if (args.length < 2)
            {
                player.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }

            final Port np = PortManager.reference.get(args[1]);
            if (np == null)
            {
                player.sendMessage(ChatColor.RED + "No port with name " + args[1] + " found.");
                return;
            } else
            {
                np.clearMessages();
                np.saveData();
                player.sendMessage(ChatColor.GREEN + "Welcome messages for VoxelPort " + np.getName() + " have been cleared!");
                return;
            }
        }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("welcome"))
        {
            if (args.length <= 2)
            {
                player.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }

            final Port np = PortManager.reference.get(args[1]);
            if (np == null)
            {
                player.sendMessage(ChatColor.RED + "No port with name " + args[1] + " found.");
                return;
            } else
            {
                String message = "";
                for (int x = 2; x < args.length; x++)
                {
                    if (args[x].startsWith("#$"))
                    {
                        if (Character.isDigit(args[x].charAt(2)))
                        {
                            message += ChatColor.getByChar(args[x].charAt(2));
                            message += args[x].substring(3) + " ";
                        } else
                        {
                            final char c = args[x].charAt(2);

                            switch (c)
                            {
                                case 'a':
                                    message += ChatColor.getByChar(c);
                                    message += args[x].substring(3) + " ";
                                    break;

                                case 'b':
                                    message += ChatColor.getByChar(c);
                                    message += args[x].substring(3) + " ";
                                    break;

                                case 'c':
                                    message += ChatColor.getByChar(c);
                                    message += args[x].substring(3) + " ";
                                    break;

                                case 'd':
                                    message += ChatColor.getByChar(c);
                                    message += args[x].substring(3) + " ";
                                    break;

                                case 'e':
                                    message += ChatColor.getByChar(c);
                                    message += args[x].substring(3) + " ";
                                    break;

                                case 'f':
                                    message += ChatColor.getByChar(c);
                                    message += args[x].substring(3) + " ";
                                    break;

                                default:
                                    player.sendMessage(ChatColor.RED + "Invalid Colour String! \"" + args[x] + "\"");
                                    return;
                            }
                        }
                    } else
                    {
                        message += args[x] + " ";
                    }
                }
                np.addMessage(message);
                np.saveData();
                player.sendMessage(ChatColor.BLUE + "Message added to VoxelPort " + np.getName());
                player.sendMessage(message);
                return;
            }
        }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("delete"))
        {
            if (args.length < 2)
            {
                player.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }

            final Port newPort = PortManager.reference.get(args[1]);
            if (newPort == null)
            {
                player.sendMessage(ChatColor.RED + "No port with name " + args[1] + " found.");
                return;
            } else
            {
                final File port = new File("plugins/VoxelPort/Ports/" + newPort.getName());
                if (PortTick.tickets.containsValue(newPort))
                {
                    PortTick.removeTicketsFor(newPort.getName());
                }
                newPort.deleteZoneBoundKeys();
                PortManager.reference.remove(newPort.getName());
                port.delete();
                player.sendMessage(ChatColor.GREEN + "VoxelPort " + newPort.getName() + " has been deleted!");
                return;
            }
        }
        /*
         *
         */
        if (args[0].equalsIgnoreCase("zone"))
        {
            final PortData portData = PortManager.data.get(player.getName());
            if ((portData == null) || (portData.a == null) || (portData.b == null))
            {
                player.sendMessage(ChatColor.RED + "Please select two zone points before changing the zone.");
                return;
            }

            if (args.length < 2)
            {
                if (portData.p == null)
                {
                    player.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                    return;
                }

                portData.p.setZone(new Zone((portData.a.getX() > portData.b.getX() ? portData.a.getX() : portData.b.getX()), (portData.a.getX() < portData.b.getX() ? portData.a.getX() : portData.b.getX()), (portData.a.getY() > portData.b.getY() ? portData.a.getY() : portData.b.getY()), (portData.a.getY() < portData.b.getY() ? portData.a.getY()
                        : portData.b.getY()), (portData.a.getZ() > portData.b.getZ() ? portData.a.getZ() : portData.b.getZ()), (portData.a.getZ() < portData.b.getZ() ? portData.a.getZ() : portData.b.getZ()), portData.a.getWorld().getName(), portData.a.getWorld().getEnvironment()));
                portData.p.saveData();
                player.sendMessage(ChatColor.GREEN + "Zone set for VoxelPort " + ChatColor.GOLD + portData.p.getName());
                return;
            } else
            {
                final Port np = PortManager.reference.get(args[1]);
                if (np == null)
                {
                    player.sendMessage(ChatColor.RED + "No port with name " + args[1] + " found.");
                    return;
                } else
                {
                    np.setZone(new Zone((portData.a.getX() > portData.b.getX() ? portData.a.getX() : portData.b.getX()), (portData.a.getX() < portData.b.getX() ? portData.a.getX() : portData.b.getX()), (portData.a.getY() > portData.b.getY() ? portData.a.getY() : portData.b.getY()), (portData.a.getY() < portData.b.getY() ? portData.a
                            .getY() : portData.b.getY()), (portData.a.getZ() > portData.b.getZ() ? portData.a.getZ() : portData.b.getZ()), (portData.a.getZ() < portData.b.getZ() ? portData.a.getZ() : portData.b.getZ()), portData.a.getWorld().getName(), portData.a.getWorld().getEnvironment()));
                    np.saveData();
                    player.sendMessage(ChatColor.GREEN + "Zone set for VoxelPort " + ChatColor.GOLD + np.getName());
                    return;
                }
            }
        }
        if (args[0].equalsIgnoreCase("redstoneKey"))
        {
            if (args.length == 1)
            {
                player.sendMessage(ChatColor.RED + "Invalid number of arguments!");
                return;
            }

            final PortData pd = PortManager.data.get(player.getName());

            if (args[1].equalsIgnoreCase("set"))
            {
                if ((pd == null) || (pd.a == null))
                {
                    player.sendMessage(ChatColor.RED + "Please select Block A with /point first!");
                    return;
                }

                if (args.length == 3)
                {
                    final Port np = PortManager.reference.get(args[2]);
                    if (np == null)
                    {
                        player.sendMessage(ChatColor.RED + "No port with name " + args[2] + " found.");
                        return;
                    }

                    np.setRedstoneKey(pd.a.getLocation());
                    pd.a = null;
                    np.saveData();
                    player.sendMessage(ChatColor.GREEN + "RedstoneKey set for VoxelPort " + ChatColor.GOLD + np.getName());
                    return;
                }

                if (pd.p == null)
                {
                    player.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                    return;
                }

                pd.p.setRedstoneKey(pd.a.getLocation());
                pd.a = null;
                pd.p.saveData();
                player.sendMessage(ChatColor.GREEN + "RedstoneKey set for VoxelPort " + ChatColor.GOLD + pd.p.getName());
                return;
            } else if (args[1].equalsIgnoreCase("clear"))
            {
                if (args.length == 3)
                {
                    final Port np = PortManager.reference.get(args[2]);
                    if (np == null)
                    {
                        player.sendMessage(ChatColor.RED + "No port with name " + args[2] + " found.");
                        return;
                    }

                    np.setRedstoneKey(null);
                    np.saveData();
                    player.sendMessage(ChatColor.GREEN + "RedstoneKey cleared for VoxelPort " + ChatColor.GOLD + np.getName());
                    return;
                }

                if ((pd == null) || (pd.p == null))
                {
                    player.sendMessage(ChatColor.RED + "You haven't set a port, please pick a portal name.");
                    return;
                }

                pd.p.setRedstoneKey(null);
                pd.p.saveData();
                player.sendMessage(ChatColor.GREEN + "RedstoneKey cleared for VoxelPort " + ChatColor.GOLD + pd.p.getName());
                return;
            }

            return;
        }
    }

    private class PortData
    {

        public PortData(final Block t)
        {
            this.a = t;
        }

        public Block a;
        public Block b;
        public Port p;
    }
}
