package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;
import java.util.*;

public class CompiledDistributorModule extends CompiledSenderModule2 {
    public static final String NBT_STRATEGY = "DistStrategy";
    public static final String NBT_PULLING = "Pulling";

    public enum DistributionStrategy {
        ROUND_ROBIN,
        RANDOM,
        NEAREST_FIRST,
        FURTHEST_FIRST
    }

    private final DistributionStrategy distributionStrategy;
    private int nextTarget = 0;
    private boolean pulling = false;

    public CompiledDistributorModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);

        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null) {
            distributionStrategy = compound.hasKey(NBT_STRATEGY) ?
                    DistributionStrategy.values()[compound.getInteger(NBT_STRATEGY)] :
                    DistributionStrategy.ROUND_ROBIN;
            pulling = compound.hasKey(NBT_PULLING) && compound.getBoolean(NBT_PULLING);
        } else {
            distributionStrategy = DistributionStrategy.ROUND_ROBIN;
        }
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        return pulling ? executePull(router) : super.execute(router);
    }

    private boolean executePull(TileEntityItemRouter router) {
        if (router.isBufferFull()) return false;

        ModuleTarget tgt = getEffectiveTarget(router);
        if (tgt == null) return false;

        if (router.getWorldObj().getTileEntity(tgt.getX(), tgt.getY(), tgt.getZ()) instanceof IInventory) {
            IInventory inv = (IInventory) router.getWorldObj().getTileEntity(tgt.getX(), tgt.getY(), tgt.getZ());
            ItemStack taken = transferToRouter(inv, router);
            return taken != null;
        }
        return false;
    }

    @Override
    public ModuleTarget getEffectiveTarget(TileEntityItemRouter router) {
        // Scan for inventories within range, then apply distribution strategy
        int range = getRange();
        ForgeDirection facing = getAbsoluteDirection(router);

        List<ModuleTarget> candidates = new ArrayList<>();
        for (int d = 1; d <= range; d++) {
            int x = router.xCoord + facing.offsetX * d;
            int y = router.yCoord + facing.offsetY * d;
            int z = router.zCoord + facing.offsetZ * d;
            if (router.getWorldObj().getTileEntity(x, y, z) instanceof IInventory) {
                candidates.add(new ModuleTarget(x, y, z, facing.getOpposite(), null));
            }
        }

        if (candidates.isEmpty()) return null;
        if (candidates.size() == 1) return candidates.get(0);

        ItemStack stack = router.peekBuffer(getItemsPerTick(router));
        switch (distributionStrategy) {
            case ROUND_ROBIN:
                for (int i = 1; i <= candidates.size(); i++) {
                    nextTarget++;
                    if (nextTarget >= candidates.size()) nextTarget = 0;
                    ModuleTarget tgt = candidates.get(nextTarget);
                    if (okToInsert(router, tgt, stack)) return tgt;
                }
                break;
            case RANDOM:
                return candidates.get(router.getWorldObj().rand.nextInt(candidates.size()));
            case NEAREST_FIRST:
                for (ModuleTarget tgt : candidates) {
                    if (okToInsert(router, tgt, stack)) return tgt;
                }
                break;
            case FURTHEST_FIRST:
                for (int i = candidates.size() - 1; i >= 0; i--) {
                    if (okToInsert(router, candidates.get(i), stack)) return candidates.get(i);
                }
                break;
        }

        return null;
    }

    private boolean okToInsert(TileEntityItemRouter router, ModuleTarget target, ItemStack stack) {
        if (stack == null) return true;
        if (router.getWorldObj().getTileEntity(target.getX(), target.getY(), target.getZ()) instanceof IInventory) {
            IInventory inv = (IInventory) router.getWorldObj().getTileEntity(target.getX(), target.getY(), target.getZ());
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                if (inv.isItemValidForSlot(i, stack)) {
                    ItemStack existing = inv.getStackInSlot(i);
                    if (existing == null) return true;
                    if (existing.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(existing, stack)
                            && existing.stackSize < Math.min(inv.getInventoryStackLimit(), existing.getMaxStackSize())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isPulling() { return pulling; }
    public DistributionStrategy getDistributionStrategy() { return distributionStrategy; }
}
