package com.thevoxelbox.voxelport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PortTick
        implements Runnable {

    static final Logger log = Logger.getLogger("Minecraft");
    public static int t;
    private static int sleepSpeed;
    public static HashMap<Player, NewPort> tickets = new HashMap();
    public static HashSet<Player> usedTickets = new HashSet();
    public static int codeTick = 100;

    public static void registerTicket(Player player, NewPort port) {
        tickets.put(player, port);
        player.sendRawMessage(ChatColor.AQUA + "Your pass has been accepted.");
        player.sendRawMessage(ChatColor.AQUA + "This VoxelPort will dispatch in " + ChatColor.LIGHT_PURPLE + ":     " + ChatColor.DARK_RED
                + "[" + port.nextDepartureTotal((int) (PortTick.t + (player.getWorld().getTime() % 100))) + ChatColor.DARK_RED + "]");
    }

    public static void setTime(int a) {
        sleepSpeed = a;
    }

    public PortTick() {
        this(5000);
    }

    public PortTick(int z) {
        sleepSpeed = z;
        t = 0;
        codeTick = (int) (sleepSpeed * 0.02);
        log.info("[VoxelPort] PortTick thread executing. Running at interval of " + sleepSpeed + "ms, with " + codeTick + " CodeTime each Tick");
    }

    public static void removeTicketsFor(String n) {
        Set<Player> toRemove = new HashSet<Player>();
        Set<Entry<Player, NewPort>> set = tickets.entrySet();
        for (Entry<Player, NewPort> m : set) {
            if (m.getValue().getName().equals(n)) {
                m.getKey().sendMessage(ChatColor.DARK_GREEN + "The portal has been deleted. Your ticket expires.");
                toRemove.add(m.getKey());
            }
        }
        for (Player p : toRemove) {
            tickets.remove(p);
        }
    }

    public static void removeThread() {
        for (Player pla : tickets.keySet()) {
            pla.sendMessage(ChatColor.DARK_GREEN + "Your ticket expires.");
        }
        tickets.clear();
        usedTickets.clear();
    }

    @Override
    public void run() {
        try {
            t += codeTick;
            if (t >= 24000) {
                t = 0;
            }
            if (!tickets.isEmpty()) {
                Set<Entry<Player, NewPort>> set = tickets.entrySet();
                for (Entry<Player, NewPort> m : set) {
                    m.getValue().departPlayer(m.getKey(), t);
                }
                if (!usedTickets.isEmpty()) {
                    for (Player p : usedTickets) {
                        tickets.remove(p);
                    }
                    usedTickets.clear();
                }
            }
        } catch (Exception e) {
            log.warning("[VoxelPort] Error occured in the PortTick Thread");
            e.printStackTrace();
        }
    }

    public static void stop() {
        removeThread();
    }
}
