package com.thevoxelbox.voxelport;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class VoxelPort extends JavaPlugin
{

    public static Logger log;
    private final PortListener portListener = new PortListener();
    public static VoxelPort voxelPort;
    public static PortManager portManager;

    @Override
    public void onDisable()
    {
        PortTick.stop();
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override
    public void onEnable()
    {
        VoxelPort.log = this.getLogger();
        VoxelPort.voxelPort = this;
        VoxelPort.portManager = new PortManager(this);
        Bukkit.getPluginManager().registerEvents(this.portListener, this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args)
    {
        final String commandName = command.getName().toLowerCase();

        if ((sender instanceof Player))
        {
            final Player player = (Player) sender;

            if (commandName.equals("vworld"))
            {
                player.sendMessage(ChatColor.GOLD + "Current World: " + ChatColor.GREEN + player.getWorld().getName());
                return true;
            }
            if (commandName.equals("ctime"))
            {
                player.sendMessage(ChatColor.GOLD + "Voxel Time: " + ChatColor.GREEN + player.getWorld().getTime());
                return true;
            }
            if (commandName.equals("vp") && player.hasPermission("voxelport.vp"))
            {
                if (args.length >= 1)
                {
                    try
                    {
                        VoxelPort.portManager.manageCommand(player, args);
                    }
                    catch (final Exception e)
                    {
                        player.sendMessage(ChatColor.RED + "Invalid input! You entered something incorrectly.");
                        player.sendMessage(ChatColor.DARK_RED + e.toString());
                    }
                    return true;
                } else
                {
                    // Print commands
                    player.sendMessage(ChatColor.RED + "The /vp commands are: []-required ()-optional \"\"-word");
                    player.sendMessage(ChatColor.GREEN + "point  --  Stores the two Zone points");
                    player.sendMessage(ChatColor.DARK_GRAY + "set (portName)");
                    player.sendMessage(ChatColor.GREEN + "create [portName]");
                    player.sendMessage(ChatColor.DARK_GRAY + "target (portName) (portName)");
                    player.sendMessage(ChatColor.GREEN + "targetWorld [portName] (\"default\"/\"nether\")");
                    player.sendMessage(ChatColor.DARK_GRAY + "arrive (portName)");
                    player.sendMessage(ChatColor.GREEN + "disp (portName) (\"clear\") [number between 0 - 24000]");
                    player.sendMessage(ChatColor.DARK_GRAY + "genDisp [portName] [interval] (startTime)");
                    player.sendMessage(ChatColor.GREEN + "instaPort (portName) [\"true\"/\"false\"]");
                    player.sendMessage(ChatColor.DARK_GRAY + "requireTicket (portName) [\"true\"/\"false\"]");
                    player.sendMessage(ChatColor.GREEN + "welcomeClear [portName]");
                    player.sendMessage(ChatColor.DARK_GRAY + "welcome [portName] [message]");
                    player.sendMessage(ChatColor.GREEN + "delete [portName]");
                    player.sendMessage(ChatColor.DARK_GREEN + "zone (portName)");
                    player.sendMessage(ChatColor.GREEN + "redstoneKey [\"set\"|\"clear\"] (portName)");
                    player.sendMessage(ChatColor.DARK_GREEN + "info (portName)");
                    return true;
                }
            }
        }
        return false;
    }
}
