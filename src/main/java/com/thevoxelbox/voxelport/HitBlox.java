package com.thevoxelbox.voxelport;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * @author Voxel
 */
public class HitBlox
{

    private Location playerLoc;
    private double rotX, rotY, viewHeight, rot_xsin, rotXcos, rotYsin, rotYcos;
    private double length, hLength, step;
    private double range;
    private double playerX, playerY, playerZ;
    private double xOffset, yOffset, zOffset;
    private int lastX, lastY, lastZ;
    private int targetX, targetY, targetZ;
    private final World world;

    /**
     * Constructor requiring player, uses default values
     * 
     * @param player
     */
    public HitBlox(final Player player, final World world)
    {
        this.world = world;
        this.init(player.getLocation(), 250, 0.2, 1.65); // Reasonable
                                                            // default
        // values
    }

    public HitBlox(final Player player, final World world, final double maxRange)
    {
        this.world = world;
        this.init(player.getLocation(), maxRange, 0.2, 1.65);
        this.fromOffworld();
    }

    /**
     * Constructor requiring location, uses default values
     * 
     * @param loc
     */
    public HitBlox(final Location loc)
    {
        this.world = loc.getWorld();
        this.init(loc, 200, 0.2, 0);
    }

    /**
     * Constructor requiring player, max range, and a stepping value
     * 
     * @param player
     * @param range
     * @param step
     */
    public HitBlox(final Player player, final int range, final double step)
    {
        this.world = player.getWorld();
        this.init(player.getLocation(), range, step, 1.65);
    }

    /**
     * Constructor requiring location, max range, and a stepping value
     * 
     * @param loc
     * @param range
     * @param step
     */
    public HitBlox(final Location loc, final int range, final double step)
    {
        this.world = loc.getWorld();
        this.init(loc, range, step, 0);
    }

    private void init(final Location loc, final double range, final double step, final double viewHeight)
    {
        this.playerLoc = loc;
        this.viewHeight = viewHeight;
        this.playerX = this.playerLoc.getX();
        this.playerY = this.playerLoc.getY() + this.viewHeight;
        this.playerZ = this.playerLoc.getZ();
        this.range = range;
        this.step = step;
        this.length = 0;
        this.rotX = (this.playerLoc.getYaw() + 90) % 360;
        this.rotY = this.playerLoc.getPitch() * -1;
        this.rotYcos = Math.cos(Math.toRadians(this.rotY));
        this.rotYsin = Math.sin(Math.toRadians(this.rotY));
        this.rotXcos = Math.cos(Math.toRadians(this.rotX));
        this.rot_xsin = Math.sin(Math.toRadians(this.rotX));

        this.targetX = (int) Math.floor(this.playerLoc.getX());
        this.targetY = (int) Math.floor(this.playerLoc.getY() + this.viewHeight);
        this.targetZ = (int) Math.floor(this.playerLoc.getZ());
        this.lastX = this.targetX;
        this.lastY = this.targetY;
        this.lastZ = this.targetZ;
    }

    /**
     * Returns the block at the cursor, or null if out of range
     * 
     * @return Block
     */
    public Block getTargetBlock()
    {
        this.fromOffworld();
        while ((this.getNextBlock() != null) && (this.getCurBlock().isEmpty()));
        return this.getCurBlock();
    }

    /**
     * Sets the type of the block at the cursor
     * 
     * @param type
     */
    public void setTargetBlock(final int type)
    {
        while ((this.getNextBlock() != null) && (this.getCurBlock().isEmpty()));
        if (this.getCurBlock() != null)
        {
            this.world.getBlockAt(this.targetX, this.targetY, this.targetZ).setTypeId(type);
        }
    }

    /**
     * Returns the block attached to the face at the cursor, or null if out of
     * range
     * 
     * @return Block
     */
    public Block getFaceBlock()
    {
        while ((this.getNextBlock() != null) && (this.getCurBlock().isEmpty()));
        if (this.getCurBlock() != null)
        {
            return this.getLastBlock();
        } else
        {
            return null;
        }
    }

    /**
     * Sets the type of the block attached to the face at the cursor
     * 
     * @param type
     */
    public void setFaceBlock(final int type)
    {
        while ((this.getNextBlock() != null) && (this.getCurBlock().isEmpty()));
        if (this.getCurBlock() != null)
        {
            this.world.getBlockAt(this.targetX, this.targetY, this.targetZ).setTypeId(type);
        }
    }

    /**
     * Returns STEPS forward along line of vision and returns block
     * 
     * @return Block
     */
    public Block getNextBlock()
    {
        this.lastX = this.targetX;
        this.lastY = this.targetY;
        this.lastZ = this.targetZ;

        do
        {
            this.length += this.step;

            this.hLength = (this.length * this.rotYcos);
            this.yOffset = (this.length * this.rotYsin);
            this.xOffset = (this.hLength * this.rotXcos);
            this.zOffset = (this.hLength * this.rot_xsin);

            this.targetX = (int) Math.floor(this.xOffset + this.playerX);
            this.targetY = (int) Math.floor(this.yOffset + this.playerY);
            this.targetZ = (int) Math.floor(this.zOffset + this.playerZ);

        } while ((this.length <= this.range) && ((this.targetX == this.lastX) && (this.targetY == this.lastY) && (this.targetZ == this.lastZ)));

        if ((this.length > this.range) || (this.targetY > 255) || (this.targetY < 0))
        {
            return null;
        }

        return this.world.getBlockAt(this.targetX, this.targetY, this.targetZ);
    }

    public Block getRangeBlock()
    {
        this.fromOffworld();
        if (this.length > this.range)
        {
            return null;
        } else
        {
            return this.getRange();
        }
    }

    private Block getRange()
    {
        this.lastX = this.targetX;
        this.lastY = this.targetY;
        this.lastZ = this.targetZ;

        do
        {
            this.length += this.step;

            this.hLength = (this.length * this.rotYcos);
            this.yOffset = (this.length * this.rotYsin);
            this.xOffset = (this.hLength * this.rotXcos);
            this.zOffset = (this.hLength * this.rot_xsin);

            this.targetX = (int) Math.floor(this.xOffset + this.playerX);
            this.targetY = (int) Math.floor(this.yOffset + this.playerY);
            this.targetZ = (int) Math.floor(this.zOffset + this.playerZ);

        } while ((this.length <= this.range) && ((this.targetX == this.lastX) && (this.targetY == this.lastY) && (this.targetZ == this.lastZ)));

        if (this.world.getBlockTypeIdAt(this.targetX, this.targetY, this.targetZ) != 0)
        {
            return this.world.getBlockAt(this.targetX, this.targetY, this.targetZ);
        }

        if ((this.length > this.range) || (this.targetY > 255) || (this.targetY < 0))
        {
            return this.world.getBlockAt(this.lastX, this.lastY, this.lastZ);
        } else
        {
            return this.getRange();
        }
    }

    public void fromOffworld()
    {
        if (this.targetY > 255)
        {
            while ((this.targetY > 255) && (this.length <= this.range))
            {
                this.lastX = this.targetX;
                this.lastY = this.targetY;
                this.lastZ = this.targetZ;

                do
                {
                    this.length += this.step;

                    this.hLength = (this.length * this.rotYcos);
                    this.yOffset = (this.length * this.rotYsin);
                    this.xOffset = (this.hLength * this.rotXcos);
                    this.zOffset = (this.hLength * this.rot_xsin);

                    this.targetX = (int) Math.floor(this.xOffset + this.playerX);
                    this.targetY = (int) Math.floor(this.yOffset + this.playerY);
                    this.targetZ = (int) Math.floor(this.zOffset + this.playerZ);

                } while ((this.length <= this.range) && ((this.targetX == this.lastX) && (this.targetY == this.lastY) && (this.targetZ == this.lastZ)));
            }
        } else if (this.targetY < 0)
        {
            while ((this.targetY < 0) && (this.length <= this.range))
            {
                this.lastX = this.targetX;
                this.lastY = this.targetY;
                this.lastZ = this.targetZ;

                do
                {
                    this.length += this.step;

                    this.hLength = (this.length * this.rotYcos);
                    this.yOffset = (this.length * this.rotYsin);
                    this.xOffset = (this.hLength * this.rotXcos);
                    this.zOffset = (this.hLength * this.rot_xsin);

                    this.targetX = (int) Math.floor(this.xOffset + this.playerX);
                    this.targetY = (int) Math.floor(this.yOffset + this.playerY);
                    this.targetZ = (int) Math.floor(this.zOffset + this.playerZ);

                } while ((this.length <= this.range) && ((this.targetX == this.lastX) && (this.targetY == this.lastY) && (this.targetZ == this.lastZ)));
            }
        } else
        {
            return;
        }
    }

    /**
     * Returns the current block along the line of vision
     * 
     * @return Block
     */
    public Block getCurBlock()
    {
        if ((this.length > this.range) || (this.targetY > 255) || (this.targetY < 0))
        {
            return null;
        } else
        {
            return this.world.getBlockAt(this.targetX, this.targetY, this.targetZ);
        }
    }

    /**
     * Sets current block type id
     * 
     * @param type
     */
    public void setCurBlock(final int type)
    {
        if (this.getCurBlock() != null)
        {
            this.world.getBlockAt(this.targetX, this.targetY, this.targetZ).setTypeId(type);
        }
    }

    /**
     * Returns the previous block along the line of vision
     * 
     * @return Block
     */
    public Block getLastBlock()
    {
        if ((this.lastY > 255) || (this.lastY < 0))
        {
            return null;
        }
        return this.world.getBlockAt(this.lastX, this.lastY, this.lastZ);
    }

    /**
     * Sets previous block type id
     * 
     * @param type
     */
    public void setLastBlock(final int type)
    {
        if (this.getLastBlock() != null)
        {
            this.world.getBlockAt(this.lastX, this.lastY, this.lastZ).setTypeId(type);
        }
    }
}
