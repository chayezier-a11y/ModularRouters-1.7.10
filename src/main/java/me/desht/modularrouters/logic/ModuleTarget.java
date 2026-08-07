package me.desht.modularrouters.logic;

import me.desht.modularrouters.item.module.Module;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ModuleTarget {
    private static final String NBT_DIMENSION = "Dim";
    private static final String NBT_X = "X";
    private static final String NBT_Y = "Y";
    private static final String NBT_Z = "Z";
    private static final String NBT_FACE = "Face";
    private static final String NBT_NAME = "Name";

    private final int dimension;
    private final int x, y, z;
    private final ForgeDirection facing;
    private final Module.RelativeDirection direction;
    private final String blockName;

    public ModuleTarget(int x, int y, int z, ForgeDirection facing, Module.RelativeDirection direction) {
        this(0, x, y, z, facing, direction, "");
    }

    public ModuleTarget(int dimension, int x, int y, int z, ForgeDirection facing, String blockName) {
        this(dimension, x, y, z, facing, null, blockName);
    }

    private ModuleTarget(int dimension, int x, int y, int z, ForgeDirection facing,
                         Module.RelativeDirection direction, String blockName) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.facing = facing == null ? ForgeDirection.UNKNOWN : facing;
        this.direction = direction;
        this.blockName = blockName == null ? "" : blockName;
    }

    public int getDimension() { return dimension; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public ForgeDirection getFacing() { return facing; }
    public Module.RelativeDirection getDirection() { return direction; }
    public String getBlockName() { return blockName; }

    public boolean isSameWorld(World world) {
        return world != null && world.provider.dimensionId == dimension;
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(NBT_DIMENSION, dimension);
        tag.setInteger(NBT_X, x);
        tag.setInteger(NBT_Y, y);
        tag.setInteger(NBT_Z, z);
        tag.setByte(NBT_FACE, (byte) facing.ordinal());
        tag.setString(NBT_NAME, blockName);
        return tag;
    }

    public static ModuleTarget fromNBT(NBTTagCompound tag) {
        ForgeDirection[] directions = ForgeDirection.values();
        int face = tag.getByte(NBT_FACE);
        ForgeDirection facing = face >= 0 && face < directions.length
                ? directions[face] : ForgeDirection.UNKNOWN;
        return new ModuleTarget(
                tag.getInteger(NBT_DIMENSION),
                tag.getInteger(NBT_X),
                tag.getInteger(NBT_Y),
                tag.getInteger(NBT_Z),
                facing,
                tag.getString(NBT_NAME)
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ModuleTarget)) return false;
        ModuleTarget other = (ModuleTarget) obj;
        return dimension == other.dimension && x == other.x && y == other.y && z == other.z
                && facing == other.facing;
    }

    @Override
    public int hashCode() {
        int result = dimension;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + z;
        result = 31 * result + facing.ordinal();
        return result;
    }
}
