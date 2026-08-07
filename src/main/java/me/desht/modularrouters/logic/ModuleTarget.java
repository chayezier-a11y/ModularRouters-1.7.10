package me.desht.modularrouters.logic;

import me.desht.modularrouters.item.module.Module;
import net.minecraftforge.common.util.ForgeDirection;

public class ModuleTarget {
    private final int x, y, z;
    private final ForgeDirection facing;
    private final Module.RelativeDirection direction;

    public ModuleTarget(int x, int y, int z, ForgeDirection facing, Module.RelativeDirection direction) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.facing = facing;
        this.direction = direction;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public ForgeDirection getFacing() { return facing; }
    public Module.RelativeDirection getDirection() { return direction; }
}
