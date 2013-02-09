package com.thevoxelbox.voxelport;

import java.util.HashMap;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PortListener implements Listener {

    private HashMap<String, Long> next = new HashMap<String, Long>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        actOnMove(event.getPlayer(), System.currentTimeMillis());
    }

    public void actOnMove(Player p, long time) {
        try {
            if (time > next.get(p.getName())) {
                next.put(p.getName(), time + PortManager.CHECK_INTERVAL);
                PortManager.inBound(p, p.getLocation());
            } else {
                return;
            }
        } catch (Exception e) {
            next.put(p.getName(), time);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getClickedBlock().getTypeId() == 77) {

            int i1 = event.getClickedBlock().getData();
            Block b = null;

            if (i1 == 1) {
                b = event.getClickedBlock().getRelative(-1, 0, 0);
            } else if (i1 == 2) {
                b = event.getClickedBlock().getRelative(1, 0, 0);
            } else if (i1 == 3) {
                b = event.getClickedBlock().getRelative(0, 0, -1);
            } else if (i1 == 4) {
                b = event.getClickedBlock().getRelative(0, 0, 1);
            } else {
                b = event.getClickedBlock().getRelative(0, -1, 0);
            }

            if (b != null && b.getTypeId() == PortManager.BUTTON_BLOCK_ID) {
                if (b.getData() == PortManager.BUTTON_BLOCK_DATA) {
                    NewPort np = PortManager.getPort(b.getLocation());
                    if (np != null && np.instant()) {
                        np.instaPort(event.getPlayer(), false);
                    }
                }
            }
        }
    }
}
