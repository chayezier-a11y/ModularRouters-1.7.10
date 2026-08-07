package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.MiscUtil;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class CompiledVacuumModule extends CompiledModule {
    public static final String NBT_XP_FLUID_TYPE = "XPFluidType";
    public static final String NBT_XP_COLLECTION_TYPE = NBT_XP_FLUID_TYPE;
    public static final String NBT_AUTO_EJECT = "AutoEject";

    public enum XPCollectionType { BOTTLE }

    private final boolean fastPickup;
    private final boolean xpMode;
    private final boolean autoEjecting;
    private final XPCollectionType xpCollectionType;
    private int xpBuffered = 0;

    public CompiledVacuumModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        fastPickup = getAugmentCount(ItemAugment.getAugment("fastPickupAugment")) > 0;
        xpMode = getAugmentCount(ItemAugment.getAugment("xpVacuumAugment")) > 0;
        NBTTagCompound tag = ModuleHelper.validateNBT(stack);
        int type = tag.getByte(NBT_XP_COLLECTION_TYPE);
        xpCollectionType = type >= 0 && type < XPCollectionType.values().length
                ? XPCollectionType.values()[type] : XPCollectionType.BOTTLE;
        autoEjecting = tag.getBoolean(NBT_AUTO_EJECT);
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        if (xpMode) {
            return handleXpMode(router);
        } else {
            return handleItemMode(router);
        }
    }

    private boolean handleItemMode(TileEntityItemRouter router) {
        if (router.isBufferFull()) {
            return false;
        }

        ItemStack bufferStack = router.getBufferItemStack();
        int range = getRange();
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
                router.xCoord - range, router.yCoord - range, router.zCoord - range,
                router.xCoord + range + 1, router.yCoord + range + 1, router.zCoord + range + 1);

        @SuppressWarnings("unchecked")
        List<EntityItem> items = router.getWorldObj().getEntitiesWithinAABB(EntityItem.class, aabb);
        int toPickUp = getItemsPerTick(router);

        for (EntityItem item : items) {
            if (item.isDead || (!fastPickup && item.delayBeforeCanPickup > 0)) {
                continue;
            }
            ItemStack stackOnGround = item.getEntityItem();
            if (stackOnGround == null) continue;

            if ((bufferStack == null || canStack(bufferStack, stackOnGround)) && getFilter().allowItem(stackOnGround)) {
                int inRouter = bufferStack != null ? bufferStack.stackSize : 0;
                int spaceInRouter = getRegulationAmount() > 0 ?
                        Math.min(stackOnGround.getMaxStackSize(), getRegulationAmount()) - inRouter :
                        stackOnGround.getMaxStackSize() - inRouter;
                int vacuumed = Math.min(Math.min(toPickUp, spaceInRouter), stackOnGround.stackSize);

                ItemStack toInsertStack = stackOnGround.copy();
                toInsertStack.stackSize = vacuumed;

                ItemStack remaining = router.insertBuffer(toInsertStack);
                int inserted = vacuumed - (remaining != null ? remaining.stackSize : 0);
                stackOnGround.stackSize -= inserted;
                toPickUp -= inserted;

                if (stackOnGround.stackSize <= 0) {
                    item.setDead();
                }
                if (toPickUp <= 0) {
                    break;
                }
            }
        }
        return toPickUp < getItemsPerTick(router);
    }

    private boolean handleXpMode(TileEntityItemRouter router) {
        ItemStack buffer = router.getBufferItemStack();
        ItemStack bottle = new ItemStack(Items.experience_bottle);
        if (buffer != null && (!buffer.isItemEqual(bottle) || !ItemStack.areItemStackTagsEqual(buffer, bottle))) {
            return false;
        }
        int capacity = buffer == null ? bottle.getMaxStackSize() * 3
                : (buffer.getMaxStackSize() - buffer.stackSize) * 3;
        if (capacity <= xpBuffered) return false;

        int range = getRange();
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
                router.xCoord - range, router.yCoord - range, router.zCoord - range,
                router.xCoord + range + 1, router.yCoord + range + 1, router.zCoord + range + 1);

        @SuppressWarnings("unchecked")
        List<EntityXPOrb> orbs = router.getWorldObj().getEntitiesWithinAABB(EntityXPOrb.class, aabb);
        if (orbs.isEmpty()) return false;

        int collected = 0;
        for (EntityXPOrb orb : orbs) {
            if (orb.isDead) continue;
            if (capacity - xpBuffered < orb.getXpValue()) break;
            xpBuffered += orb.getXpValue();
            orb.setDead();
            collected++;
            while (xpBuffered >= 3) {
                ItemStack remainder = router.insertBuffer(new ItemStack(Items.experience_bottle));
                if (remainder != null) return collected > 0;
                xpBuffered -= 3;
            }
            if (collected >= getItemsPerTick(router)) break;
        }
        return collected > 0;
    }

    private boolean canStack(ItemStack a, ItemStack b) {
        return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    @Override
    public List<ModuleTarget> setupTargets(TileEntityItemRouter router, ItemStack stack) {
        if (router == null) return null;
        me.desht.modularrouters.item.module.Module.RelativeDirection dir = getDirection();
        int offset = dir == me.desht.modularrouters.item.module.Module.RelativeDirection.NONE ? 0 : getRange() + 1;
        ForgeDirection facing = router.getAbsoluteFacing(dir);
        int x = router.xCoord + facing.offsetX * offset;
        int y = router.yCoord + facing.offsetY * offset;
        int z = router.zCoord + facing.offsetZ * offset;
        return Collections.singletonList(new ModuleTarget(x, y, z, facing.getOpposite(), null));
    }

    public boolean isXpMode() {
        return xpMode;
    }

    public XPCollectionType getXPCollectionType() { return xpCollectionType; }

    public boolean isAutoEjecting() { return autoEjecting; }
}
