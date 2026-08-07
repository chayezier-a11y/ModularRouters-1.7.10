package me.desht.modularrouters.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class BeamData {
    private final int destX, destY, destZ;
    private final int color;
    private final int duration;
    private final ItemStack stack;
    private boolean itemFade;
    private boolean reversed;
    private int ticksLived;

    public BeamData(int duration, int x, int y, int z, ItemStack stack, int color) {
        this.duration = duration;
        this.destX = x;
        this.destY = y;
        this.destZ = z;
        this.stack = stack != null ? stack.copy() : null;
        this.color = color;
        this.ticksLived = 0;
    }

    public BeamData(int duration, int x, int y, int z, int color) {
        this(duration, x, y, z, null, color);
    }

    public BeamData reverseItems() { this.reversed = true; return this; }
    public BeamData fadeItems() { this.itemFade = true; return this; }

    public Vec3 getStart(double baseX, double baseY, double baseZ) {
        return reversed ? Vec3.createVectorHelper(destX + 0.5, destY + 0.5, destZ + 0.5)
                : Vec3.createVectorHelper(baseX, baseY, baseZ);
    }

    public Vec3 getEnd(double baseX, double baseY, double baseZ) {
        return reversed ? Vec3.createVectorHelper(baseX, baseY, baseZ)
                : Vec3.createVectorHelper(destX + 0.5, destY + 0.5, destZ + 0.5);
    }

    public ItemStack getStack() { return stack; }
    public boolean isItemFade() { return itemFade; }
    public boolean isReversed() { return reversed; }
    public int getColor() { return color; }

    public float getProgress(float partialTicks) {
        return MathHelper.clamp_float((ticksLived - 1 + partialTicks) / duration, 0f, 1f);
    }

    public void tick() { ticksLived++; }
    public boolean isExpired() { return ticksLived > duration; }

    public int[] getRGB() {
        return new int[] { color >> 16 & 0xff, color >> 8 & 0xff, color & 0xff };
    }

    public int getDestX() { return destX; }
    public int getDestY() { return destY; }
    public int getDestZ() { return destZ; }
}
