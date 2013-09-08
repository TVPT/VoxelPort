package com.thevoxelbox.voxelport;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PortListener implements Listener
{

    //Erm doesn't order matter 
    private final HashMap<String, Long> moveQueue = new HashMap<String, Long>();

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event)
    {
        this.actOnMove(event.getPlayer(), System.currentTimeMillis());
    }

    public void actOnMove(final Player player, final long time)
    {
        try
        {
            if (time > this.moveQueue.get(player.getName()))
            {
                this.moveQueue.put(player.getName(), time + PortManager.CHECK_INTERVAL);
                PortManager.inBound(player, player.getLocation());
            } else
            {
                return;
            }
        }
        catch (final Exception e)
        {
            this.moveQueue.put(player.getName(), time);
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event)
    {
        if (event.getClickedBlock() == null)
        {
            return;
        }
        if (event.getClickedBlock().getTypeId() == 77)
        {

            final int i1 = event.getClickedBlock().getData();
            Block tarBlock = null;

            if (i1 == 1)
            {
                tarBlock = event.getClickedBlock().getRelative(-1, 0, 0);
            } else if (i1 == 2)
            {
                tarBlock = event.getClickedBlock().getRelative(1, 0, 0);
            } else if (i1 == 3)
            {
                tarBlock = event.getClickedBlock().getRelative(0, 0, -1);
            } else if (i1 == 4)
            {
                tarBlock = event.getClickedBlock().getRelative(0, 0, 1);
            } else
            {
                tarBlock = event.getClickedBlock().getRelative(0, -1, 0);
            }

            if ((tarBlock != null) && (tarBlock.getTypeId() == PortManager.BUTTON_BLOCK_ID))
            {
                if (tarBlock.getData() == PortManager.BUTTON_BLOCK_DATA)
                {
                    final Port port = PortManager.getPort(tarBlock.getLocation());
                    if ((port != null) && port.instant())
                    {
                        port.instaPort(event.getPlayer(), false);
                    }
                }
            }
        }
    }
}
