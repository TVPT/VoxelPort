package com.thevoxelbox.voxelport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PortTick implements Runnable
{
    public static int time;
    private static int sleepSpeed;
    public static final HashMap<Player, Port> tickets = new HashMap<Player, Port>();
    public static final HashSet<Player> usedTickets = new HashSet<Player>();
    public static int codeTick = 100;

    public static void registerTicket(final Player player, final Port port)
    {
        PortTick.tickets.put(player, port);
        player.sendRawMessage(ChatColor.AQUA + "Your pass has been accepted.");
        player.sendRawMessage(ChatColor.AQUA + "This VoxelPort will dispatch in " + ChatColor.LIGHT_PURPLE + ":     " + ChatColor.DARK_RED + "[" + port.nextDepartureTotal((int) (PortTick.time + (player.getWorld().getTime() % 100))) + ChatColor.DARK_RED + "]");
    }

    public static void setTime(final int a)
    {
        PortTick.sleepSpeed = a;
    }

    public PortTick()
    {
        this(5000);
    }

    public PortTick(final int z)
    {
        PortTick.sleepSpeed = z;
        PortTick.time = 0;
        PortTick.codeTick = (int) (PortTick.sleepSpeed * 0.02);
        VoxelPort.log.info("PortTick thread executing. Running at interval of " + PortTick.sleepSpeed + "ms, with " + PortTick.codeTick + " CodeTime each Tick");
    }

    public static void removeTicketsFor(final String name)
    {
        final Set<Player> toRemove = new HashSet<Player>();
        for (final Entry<Player, Port> entry : PortTick.tickets.entrySet())
        {
            if (entry.getValue().getName().equals(name))
            {
                entry.getKey().sendMessage(ChatColor.DARK_GREEN + "The portal has been deleted. Your ticket expires.");
                toRemove.add(entry.getKey());
            }
        }
        for (final Player player : toRemove)
        {
            PortTick.tickets.remove(player);
        }
    }

    public static void removeThread()
    {
        for (final Player player : PortTick.tickets.keySet())
        {
            player.sendMessage(ChatColor.DARK_GREEN + "Your ticket expires.");
        }
        PortTick.tickets.clear();
        PortTick.usedTickets.clear();
    }

    @Override
    public void run()
    {
        try
        {
            PortTick.time += PortTick.codeTick;
            if (PortTick.time >= 24000)
            {
                PortTick.time = 0;
            }
            if (!PortTick.tickets.isEmpty())
            {
                final Set<Entry<Player, Port>> set = PortTick.tickets.entrySet();
                for (final Entry<Player, Port> m : set)
                {
                    m.getValue().departPlayer(m.getKey(), PortTick.time);
                }
                if (!PortTick.usedTickets.isEmpty())
                {
                    for (final Player p : PortTick.usedTickets)
                    {
                        PortTick.tickets.remove(p);
                    }
                    PortTick.usedTickets.clear();
                }
            }
        }
        catch (final Exception e)
        {
            VoxelPort.log.warning("[VoxelPort] Error occurred in the PortTick Thread");
            e.printStackTrace();
        }
    }

    public static void stop()
    {
        PortTick.removeThread();
    }
}
