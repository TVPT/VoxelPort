package com.thevoxelbox.voxelport;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class oldPort {

    public String name;
    public Location r = null;
    public int zxh;
    public int zxl;
    public int zzh;
    public int zzl;
    public int zyh;
    public int zyl;
    public Location t = null;
    public oldPort target = null;
    public HashSet<Integer> disp = new HashSet<Integer>();
    public HashSet<String> welcome = new HashSet<String>();
    public int start = -1;
    public int interval = -1;

    public oldPort(int xh, int xl, int zh, int zl, int yh, int yl, String n) {
        this.zxh = xh;
        this.zxl = xl;
        this.zzh = zh;
        this.zzl = zl;
        this.zyh = yh;
        this.zyl = yl;
        this.name = n;
    }

    public oldPort(String na) {
        this.name = na;
        readData();
    }

    public oldPort(String vpo, boolean a) {
        this.name = vpo;
        readTargetData();
    }

    private void readData() {
        if (new File("VoxelPorts/" + this.name).exists()) {
            try {
                FileInputStream file = new FileInputStream(new File("VoxelPorts/" + this.name));
                DataInputStream data = new DataInputStream(file);
                try {
                    while (true) {
                        byte by = data.readByte();
                        if (by == 1) {
                            this.zxh = data.readInt();
                            this.zxl = data.readInt();
                            this.zyh = data.readInt();
                            this.zyl = data.readInt();
                            this.zzh = data.readInt();
                            this.zzl = data.readInt();
                        }
                        if (by == 2) {
                            double rx = data.readDouble();
                            double ry = data.readDouble();
                            double rz = data.readDouble();
                            String wo = data.readUTF();
                            this.r = new Location(Bukkit.getWorld(wo), rx, ry, rz);
                        }
                        if (by == 3) {
                            double tx = data.readDouble();
                            double ty = data.readDouble();
                            double tz = data.readDouble();
                            String wo = data.readUTF();
                            this.t = new Location(Bukkit.getWorld(wo), tx, ty, tz);
                        }
                        if (by == 4) {
                            this.disp.add(Integer.valueOf(data.readInt()));
                        }
                        if (by == 5) {
                            String ss = data.readUTF();
                            this.target = new oldPort(ss, true);
                        }
                        if (by == 6) {
                            int f = data.readInt();
                            for (int u = 0; u < f; u++) {
                                this.welcome.add(data.readUTF());
                            }
                        }
                        if (by == 7) {
                            this.start = data.readInt();
                            this.interval = data.readInt();
                        }
                    }
                } catch (EOFException eof) {
                    data.close();
                    VoxelPort.log.info("[VoxelPort] Portal \"" + this.name + "\" loaded!");
                }
            } catch (IOException e) {
                VoxelPort.log.warning("[VoxelPort] Invalid File. \"" + this.name + "\" is not a VoxelPort or is currupted.");
            }
        }
    }

    private void readTargetData() {
        if (new File("VoxelPorts/" + this.name).exists()) {
            try {
                FileInputStream file = new FileInputStream(new File("VoxelPorts/" + this.name));
                DataInputStream data = new DataInputStream(file);
                try {
                    while (true) {
                        byte by = data.readByte();
                        if (by == 6) {
                            int f = data.readInt();
                            for (int u = 0; u < f; u++) {
                                this.welcome.add(data.readUTF());
                            }
                        }
                    }
                } catch (EOFException eof) {
                    data.close();
                    VoxelPort.log.info("[VoxelPort] Portal target \"" + this.name + "\" loaded!");
                }
            } catch (IOException e) {
                System.out.println("Deerp. IOException @");
            }
        }
    }
}
