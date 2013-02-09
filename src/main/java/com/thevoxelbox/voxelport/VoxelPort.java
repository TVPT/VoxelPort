package com.thevoxelbox.voxelport;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class VoxelPort extends JavaPlugin {

    public static final Logger log = Logger.getLogger("Minecraft");
    private final PortListener listener = new PortListener();
    public static Server s;
    public static VoxelPort vp;
    public static PortManager portMan;

    @Override
    public void onDisable() {
        PortTick.stop();
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override
    public void onEnable() {
        s = getServer();
        vp = this;

        portMan = new PortManager(this);

        s.getPluginManager().registerEvents(listener, this);

        PluginDescriptionFile pdfFile = getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String commandName = command.getName().toLowerCase();

        if ((sender instanceof Player)) {
            Player p = (Player) sender;


            if (commandName.equals("vworld")) {
                p.sendMessage(ChatColor.GOLD + "Current World: " + ChatColor.GREEN + p.getWorld().getName());
                return true;
            }
            if (commandName.equals("ctime")) {
                p.sendMessage(ChatColor.GOLD + "Voxel Time: " + ChatColor.GREEN + p.getWorld().getTime());
                return true;
            }
            if (commandName.equals("vp") && p.hasPermission("voxelport.vp")) {
                if (args.length >= 1) {
                    try {
                        portMan.manageCommand(p, args);
                    } catch (Exception e) {
                        p.sendMessage(ChatColor.RED + "Invalid input! You entered something incorrectly.");
                        p.sendMessage(ChatColor.DARK_RED + e.toString());
                    }
                    return true;
                } else {
                    // Print commands
                    p.sendMessage(ChatColor.RED + "The /vp commands are: []-required ()-optional \"\"-word");
                    p.sendMessage(ChatColor.GREEN + "point  --  Stores the two Zone points");
                    p.sendMessage(ChatColor.DARK_GRAY + "set (portName)");
                    p.sendMessage(ChatColor.GREEN + "create [portName]");
                    p.sendMessage(ChatColor.DARK_GRAY + "target (portName) (portName)");
                    p.sendMessage(ChatColor.GREEN + "targetWorld [portName] (\"default\"/\"nether\")");
                    p.sendMessage(ChatColor.DARK_GRAY + "arrive (portName)");
                    p.sendMessage(ChatColor.GREEN + "disp (portName) (\"clear\") [number between 0 - 24000]");
                    p.sendMessage(ChatColor.DARK_GRAY + "genDisp [portName] [interval] (startTime)");
                    p.sendMessage(ChatColor.GREEN + "instaPort (portName) [\"true\"/\"false\"]");
                    p.sendMessage(ChatColor.DARK_GRAY + "requireTicket (portName) [\"true\"/\"false\"]");
                    p.sendMessage(ChatColor.GREEN + "welcomeClear [portName]");
                    p.sendMessage(ChatColor.DARK_GRAY + "welcome [portName] [message]");
                    p.sendMessage(ChatColor.GREEN + "delete [portName]");
                    p.sendMessage(ChatColor.DARK_GREEN + "zone (portName)");
                    p.sendMessage(ChatColor.GREEN + "redstoneKey [\"set\"|\"clear\"] (portName)");
                    p.sendMessage(ChatColor.DARK_GREEN + "info (portName)");
                    return true;
                }
            }
        }
        return false;
    }
}
