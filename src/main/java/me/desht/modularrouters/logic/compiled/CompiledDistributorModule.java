package me.desht.modularrouters.logic.compiled;

import cpw.mods.fml.common.network.NetworkRegistry;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade.UpgradeType;
import me.desht.modularrouters.logic.InventoryTransfer;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.ParticleBeamMessage;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
    private final boolean pulling;

    public CompiledDistributorModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        NBTTagCompound compound = stack.getTagCompound();
        int strategy = compound == null ? 0 : compound.getInteger(NBT_STRATEGY);
        distributionStrategy = strategy >= 0 && strategy < DistributionStrategy.values().length
                ? DistributionStrategy.values()[strategy] : DistributionStrategy.ROUND_ROBIN;
        pulling = compound != null && compound.getBoolean(NBT_PULLING);
    }

    @Override
    List<ModuleTarget> setupTargets(TileEntityItemRouter router, ItemStack stack) {
        Set<ModuleTarget> stored = TargetedModule.getTargets(stack);
        if (stored.isEmpty()) return Collections.emptyList();
        List<ModuleTarget> result = new ArrayList<ModuleTarget>(stored);
        if (router != null) {
            Collections.sort(result, new Comparator<ModuleTarget>() {
                @Override
                public int compare(ModuleTarget first, ModuleTarget second) {
                    return Double.compare(distanceSquared(router, first), distanceSquared(router, second));
                }
            });
        }
        return result;
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        return pulling ? executePull(router) : super.execute(router);
    }

    private boolean executePull(TileEntityItemRouter router) {
        if (router.isBufferFull()) return false;
        ModuleTarget target = getEffectiveTarget(router);
        if (!isLoadedSameWorld(router, target)) return false;
        TileEntity tile = router.getWorldObj().getTileEntity(target.getX(), target.getY(), target.getZ());
        if (!(tile instanceof IInventory)) return false;
        ItemStack transferred = transferToRouter((IInventory) tile, target.getFacing(), router);
        if (transferred == null) return false;
        playParticles(router, target, transferred);
        return true;
    }

    @Override
    public ModuleTarget getEffectiveTarget(TileEntityItemRouter router) {
        List<ModuleTarget> targets = getTargets();
        if (targets == null || targets.isEmpty()) return null;
        ItemStack buffer = router.peekBuffer(getItemsPerTick(router));
        boolean[] eligible = new boolean[targets.size()];
        for (int i = 0; i < targets.size(); i++) {
            eligible[i] = isEligible(router, targets.get(i), buffer);
        }
        int randomIndex = router.getWorldObj() == null ? 0
                : router.getWorldObj().rand.nextInt(Math.max(1, targets.size()));
        int selected = chooseTargetIndex(distributionStrategy, nextTarget, eligible, randomIndex);
        if (selected < 0) return null;
        if (distributionStrategy == DistributionStrategy.ROUND_ROBIN) nextTarget = selected;
        return targets.get(selected);
    }

    static int chooseTargetIndex(DistributionStrategy strategy, int current,
                                 boolean[] eligible, int randomIndex) {
        if (eligible == null || eligible.length == 0) return -1;
        switch (strategy) {
            case NEAREST_FIRST:
                for (int i = 0; i < eligible.length; i++) if (eligible[i]) return i;
                return -1;
            case FURTHEST_FIRST:
                for (int i = eligible.length - 1; i >= 0; i--) if (eligible[i]) return i;
                return -1;
            case RANDOM:
                int eligibleCount = 0;
                for (boolean value : eligible) if (value) eligibleCount++;
                if (eligibleCount == 0) return -1;
                int wanted = Math.abs(randomIndex) % eligibleCount;
                for (int i = 0; i < eligible.length; i++) {
                    if (eligible[i] && wanted-- == 0) return i;
                }
                return -1;
            case ROUND_ROBIN:
            default:
                for (int offset = 1; offset <= eligible.length; offset++) {
                    int index = (current + offset) % eligible.length;
                    if (eligible[index]) return index;
                }
                return -1;
        }
    }

    private boolean isEligible(TileEntityItemRouter router, ModuleTarget target, ItemStack buffer) {
        if (!isLoadedSameWorld(router, target)) return false;
        TileEntity tile = router.getWorldObj().getTileEntity(target.getX(), target.getY(), target.getZ());
        if (!(tile instanceof IInventory)) return false;
        IInventory inventory = (IInventory) tile;
        if (pulling) {
            for (int slot : InventoryTransfer.accessibleSlots(inventory, target.getFacing())) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack != null && InventoryTransfer.canExtract(inventory, slot, stack, target.getFacing())
                        && !getFilter().rejectItem(stack)) return true;
            }
            return false;
        }
        return buffer != null && !getFilter().rejectItem(buffer)
                && InventoryTransfer.insert(inventory, target.getFacing(), buffer, true) > 0;
    }

    private boolean isLoadedSameWorld(TileEntityItemRouter router, ModuleTarget target) {
        World world = router.getWorldObj();
        return target != null && world != null && target.isSameWorld(world)
                && world.blockExists(target.getX(), target.getY(), target.getZ());
    }

    private static double distanceSquared(TileEntityItemRouter router, ModuleTarget target) {
        long dx = (long) target.getX() - router.xCoord;
        long dy = (long) target.getY() - router.yCoord;
        long dz = (long) target.getZ() - router.zCoord;
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    void playParticles(TileEntityItemRouter router, ModuleTarget target, ItemStack stack) {
        if (!pulling) {
            super.playParticles(router, target, stack);
            return;
        }
        if (!Config.pullerParticles || router.getUpgradeCount(UpgradeType.MUFFLER) >= 2
                || ModularRouters.network == null) return;
        ModularRouters.network.sendToAllAround(
                new ParticleBeamMessage(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
                        router.xCoord + 0.5, router.yCoord + 0.5, router.zCoord + 0.5,
                        getBeamColor(), 0.08f),
                new NetworkRegistry.TargetPoint(router.getWorldObj().provider.dimensionId,
                        router.xCoord + 0.5, router.yCoord + 0.5, router.zCoord + 0.5, 64));
    }

    @Override
    protected int getBeamColor() {
        return pulling ? 0x6080FF : super.getBeamColor();
    }

    public boolean isPulling() { return pulling; }
    public DistributionStrategy getDistributionStrategy() { return distributionStrategy; }
}
