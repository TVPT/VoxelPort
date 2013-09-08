package com.thevoxelbox.voxelport;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Voxel
 */
public class PortContainer
{

    private final ArrayList<Port> collection = new ArrayList<Port>();

    public PortContainer(final Port newPort)
    {
        this.collection.add(newPort);
    }

    public void put(final Port newPort)
    {
        this.collection.add(newPort);
    }

    public boolean remove(final Port oldPort)
    {
        this.collection.remove(oldPort);
        return this.collection.isEmpty();
    }

    public void check(final Player player, final Location loc)
    {
        if (PortTick.tickets.containsKey(player))
        {
            return;
        }
        if (player.getItemInHand().getTypeId() == 337)
        {
            return;
        }
        for (final Port port : this.collection)
        {
            if (port.insideZone(loc))
            {
                if ((player.getItemInHand().getTypeId() == 266) && player.hasPermission("voxelport.admin"))
                {
                    port.instaPort(player, true);
                    return;
                }
                if (port.ticket())
                {
                    final ItemStack i = player.getItemInHand();
                    // n.turnNpcToPlayer(l);
                    if (i.getTypeId() == PortManager.TICKETID)
                    {
                        if (port.instant())
                        {
                            port.instaPort(player, false);
                            this.removeTicketFromPlayer(player);
                            return;
                        }
                        if (port.isPortActivated())
                        {
                            port.welcomePlayer(player);
                            this.removeTicketFromPlayer(player);
                            PortTick.registerTicket(player, port);
                            return;
                        }
                    }
                } else
                {
                    if (port.instant())
                    {
                        port.instaPort(player, false);
                        return;
                    }
                    if (port.isPortActivated())
                    {
                        port.welcomePlayer(player);

                        PortTick.registerTicket(player, port);
                        return;
                    }
                }
            }
        }
    }

    /*
     * public PortNPC matchNpcId(int id) { for (newPort n : collection) {
     * PortNPC pn = n.matchNpcId(id); if (pn != null) { return pn; } } return
     * null; }
     */
    public Port getPortAtLoc(final Location loc)
    {
        for (final Port n : this.collection)
        {
            if (n.insideZone(loc))
            {
                return n;
            }
        }
        return null;
    }

    private void removeTicketFromPlayer(final Player player)
    {
        final ItemStack itemInHand = player.getItemInHand();
        final int newAmount = itemInHand.getAmount() - 1;
        if (newAmount == 0)
        {
            player.getInventory().remove(itemInHand);
        } else
        {
            itemInHand.setAmount(newAmount);
        }
    }
}
