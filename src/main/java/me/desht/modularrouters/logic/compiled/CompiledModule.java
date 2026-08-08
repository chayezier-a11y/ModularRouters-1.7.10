package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.IRangedModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.Module.ModuleFlags;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.logic.InventoryTransfer;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import me.desht.modularrouters.logic.filter.Filter;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class CompiledModule {
    private final Module module;
    private final Filter filter;
    private final Module.RelativeDirection direction;
    private final List<ModuleTarget> targets;
    private final ModuleTarget explicitTarget;
    private final RouterRedstoneBehaviour behaviour;
    private final ModuleHelper.Termination termination;
    private final ForgeDirection facing;
    private final int regulationAmount;
    private final ItemAugment.AugmentCounter augmentCounter;
    private final int range, rangeSquared;

    @Nullable
    public Module getModule() { return module; }

    /**
     * Base constructor for compiled modules. Can be called for both installed and uninstalled modules;
     * when the module is not installed in a router, null can be passed.
     */
    protected CompiledModule(@Nullable TileEntityItemRouter router, ItemStack stack) {
        module = ItemModule.getModule(stack);
        if (module == null) {
            throw new IllegalArgumentException("expected module item, got " + stack);
        }

        augmentCounter = new ItemAugment.AugmentCounter(stack);
        direction = module.getDirection(stack, Module.RelativeDirection.FRONT);
        explicitTarget = module instanceof TargetedModule ? TargetedModule.getTarget(stack) : null;
        range = module instanceof IRangedModule ?
                ((IRangedModule) module).getCurrentRange(getRangeModifier()) : 0;
        rangeSquared = range * range;
        targets = setupTargets(router, stack);
        filter = new Filter(stack, router != null ? getEffectiveTarget(router) : null);
        termination = ModuleHelper.getTermination(stack);
        behaviour = ModuleHelper.getRedstoneBehaviour(stack);
        regulationAmount = ModuleHelper.getRegulatorAmount(stack);
        facing = router == null ? null : router.getAbsoluteFacing(getDirection());
    }

    /**
     * Execute this installed module. Should only be called by the router.
     */
    public abstract boolean execute(@Nonnull TileEntityItemRouter router);

    @Nonnull
    public Filter getFilter() {
        return filter;
    }

    public Module.RelativeDirection getDirection() {
        return direction;
    }

    ModuleTarget getTarget() {
        return targets == null || targets.isEmpty() ? null : targets.get(0);
    }

    List<ModuleTarget> getTargets() {
        return targets;
    }

    public boolean hasTarget() { return getTarget() != null; }

    /**
     * @deprecated Use getTarget() instead
     */
    public ModuleTarget getBoundTarget() {
        return getTarget();
    }

    /** Returns the target explicitly bound by sneaking and clicking an inventory. */
    public ModuleTarget getExplicitTarget() {
        return explicitTarget;
    }

    public boolean hasExplicitTarget() {
        return explicitTarget != null;
    }

    public ModuleHelper.Termination termination() {
        return termination;
    }

    RouterRedstoneBehaviour getRedstoneBehaviour() {
        return behaviour;
    }

    public int getRegulationAmount() {
        return augmentCounter.getAugmentCount(ItemAugment.getAugment("regulatorAugment")) > 0 ? regulationAmount : 0;
    }

    int getAugmentCount(Item augmentType) {
        return augmentCounter.getAugmentCount(augmentType);
    }

    ForgeDirection getFacing() {
        return facing;
    }

    public ForgeDirection getAbsoluteDirection(TileEntityItemRouter router) {
        return router.getAbsoluteFacing(getDirection());
    }

    public void onCompiled(TileEntityItemRouter router) {
        if (behaviour == RouterRedstoneBehaviour.PULSE) {
            router.setHasPulsedModules(true);
        }
    }

    public void cleanup(TileEntityItemRouter router) {}

    int getItemsPerTick(TileEntityItemRouter router) {
        int n = augmentCounter.getAugmentCount(ItemAugment.getAugment("stackAugment"));
        return n > 0 ? Math.min(1 << n, 64) : router.getItemsPerTick();
    }

    int getRange() {
        return range;
    }

    int getRangeSquared() {
        return rangeSquared;
    }

    private int getRangeModifier() {
        return getAugmentCount(ItemAugment.getAugment("rangeUpAugment"))
                - getAugmentCount(ItemAugment.getAugment("rangeDownAugment"));
    }

    public ModuleTarget getEffectiveTarget(TileEntityItemRouter router) {
        return getTarget();
    }

    public boolean shouldRun(boolean powered, boolean pulsed) {
        return getRedstoneBehaviour().shouldRun(powered, pulsed);
    }

    boolean isRegulationOK(TileEntityItemRouter router, boolean inbound) {
        if (regulationAmount == 0) return true;
        int items = router.getBufferItemStack() != null ? router.getBufferItemStack().stackSize : 0;
        return inbound && regulationAmount > items || !inbound && regulationAmount < items;
    }

    public void onNeighbourChange(TileEntityItemRouter router) {}

    public int getEnergyCost() {
        return module.getEnergyCost(null);
    }

    protected boolean shouldStoreRawFilterItems() {
        return false;
    }

    public boolean careAboutItemAttributes() {
        return false;
    }

    List<ModuleTarget> setupTargets(TileEntityItemRouter router, ItemStack stack) {
        if (router == null || (module.isDirectional() && direction == Module.RelativeDirection.NONE)) {
            return null;
        }
        if (explicitTarget != null) {
            return Collections.singletonList(explicitTarget);
        }
        ForgeDirection facing = router.getAbsoluteFacing(getDirection());
        int x = router.xCoord + facing.offsetX;
        int y = router.yCoord + facing.offsetY;
        int z = router.zCoord + facing.offsetZ;
        return Collections.singletonList(new ModuleTarget(router.getWorldObj().provider.dimensionId,
                x, y, z, facing.getOpposite(), ""));
    }

    /**
     * Try to transfer items from the given IInventory to the router.
     */
    protected ItemStack transferToRouter(net.minecraft.inventory.IInventory handler, TileEntityItemRouter router) {
        return transferToRouter(handler, ForgeDirection.UNKNOWN, router);
    }

    protected ItemStack transferToRouter(net.minecraft.inventory.IInventory handler,
                                         ForgeDirection sourceSide, TileEntityItemRouter router) {
        int nToTake = getItemsPerTick(router);
        ItemStack wanted = router.peekBuffer(1);
        if (wanted != null && getFilter().rejectItem(wanted)) return null;

        if (wanted == null) {
            for (int slot : InventoryTransfer.accessibleSlots(handler, sourceSide)) {
                ItemStack candidate = handler.getStackInSlot(slot);
                if (candidate != null && InventoryTransfer.canExtract(handler, slot, candidate, sourceSide)
                        && !getFilter().rejectItem(candidate)
                        && leavesRegulatedAmount(handler, sourceSide, candidate, nToTake)) {
                    wanted = candidate.copy();
                    wanted.stackSize = 1;
                    break;
                }
            }
        } else if (!leavesRegulatedAmount(handler, sourceSide, wanted, nToTake)) {
            return null;
        }

        if (wanted == null) return null;
        int moved = InventoryTransfer.transfer(handler, sourceSide, router.getBuffer(),
                ForgeDirection.UNKNOWN, wanted, nToTake);
        if (moved <= 0) return null;
        wanted.stackSize = moved;
        return wanted;
    }

    private boolean leavesRegulatedAmount(net.minecraft.inventory.IInventory handler,
                                           ForgeDirection sourceSide, ItemStack stack, int amount) {
        return getRegulationAmount() <= 0
                || InventoryTransfer.count(handler, sourceSide, stack, false, false) - amount
                >= getRegulationAmount();
    }
}
