package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.item.upgrade.ItemUpgrade.UpgradeType;
import me.desht.modularrouters.logic.InventoryTransfer;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.ParticleBeamMessage;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.network.NetworkRegistry;

public class CompiledSenderModule1 extends CompiledModule {
    public CompiledSenderModule1(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        if (router.isBufferEmpty()) return false;

        ItemStack toSend = router.peekBuffer(getItemsPerTick(router));
        if (toSend == null || getFilter().rejectItem(toSend)) return false;

        PositionedInventory target = findTargetInventory(router);
        if (!target.isValid()) return false;

        int amount = getItemsPerTick(router);
        if (getRegulationAmount() > 0) {
            int existing = InventoryTransfer.count(target.inventory, target.side, toSend, false, true);
            amount = Math.min(amount, getRegulationAmount() - existing);
            if (amount <= 0) return false;
        }

        int sent = transferIntoInventory(router, target.inventory, target.side, amount);
        if (sent > 0) {
            playParticles(router, target.position, toSend.copy());
            return true;
        }
        return false;
    }

    static boolean insertIntoInventory(TileEntityItemRouter router, IInventory inv, int amount) {
        return insertIntoInventory(router, inv, ForgeDirection.UNKNOWN, amount);
    }

    static boolean insertIntoInventory(TileEntityItemRouter router, IInventory inv,
                                       ForgeDirection side, int amount) {
        return transferIntoInventory(router, inv, side, amount) > 0;
    }

    static int transferIntoInventory(TileEntityItemRouter router, IInventory inv,
                                     ForgeDirection side, int amount) {
        ItemStack stack = router.peekBuffer(amount);
        if (stack == null) return 0;
        return InventoryTransfer.transfer(router.getBuffer(), ForgeDirection.UNKNOWN,
                inv, side, stack, amount);
    }

    PositionedInventory findTargetInventory(TileEntityItemRouter router) {
        ModuleTarget start = getTarget();
        if (start == null) return PositionedInventory.INVALID;
        ForgeDirection facing = getAbsoluteDirection(router);
        World world = router.getWorldObj();
        for (int distance = 1; distance <= getRange(); distance++) {
            int x = router.xCoord + facing.offsetX * distance;
            int y = router.yCoord + facing.offsetY * distance;
            int z = router.zCoord + facing.offsetZ * distance;
            if (!world.blockExists(x, y, z)) return PositionedInventory.INVALID;
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof IInventory) {
                return new PositionedInventory((IInventory) tile,
                        new ModuleTarget(world.provider.dimensionId, x, y, z,
                                facing.getOpposite(), ""));
            }
            if (!isPassable(world, x, y, z, facing.getOpposite())) return PositionedInventory.INVALID;
        }
        return PositionedInventory.INVALID;
    }

    private boolean isPassable(World world, int x, int y, int z, ForgeDirection side) {
        net.minecraft.block.Block block = world.getBlock(x, y, z);
        return !block.isOpaqueCube() || !block.renderAsNormalBlock();
    }

    void playParticles(TileEntityItemRouter router, ModuleTarget target, ItemStack stack) {
        if (!Config.senderParticles || router.getUpgradeCount(UpgradeType.MUFFLER) >= 2
                || ModularRouters.network == null) return;
        ModularRouters.network.sendToAllAround(
                new ParticleBeamMessage(router.xCoord + 0.5, router.yCoord + 0.5, router.zCoord + 0.5,
                        target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
                        getBeamColor(), 0.08f),
                new NetworkRegistry.TargetPoint(router.getWorldObj().provider.dimensionId,
                        router.xCoord + 0.5, router.yCoord + 0.5, router.zCoord + 0.5, 64));
    }

    protected int getBeamColor() { return 0xFFC000; }

    static class PositionedInventory {
        static final PositionedInventory INVALID = new PositionedInventory(null, null);
        final IInventory inventory;
        final ForgeDirection side;
        final ModuleTarget position;

        PositionedInventory(IInventory inventory, ModuleTarget position) {
            this.inventory = inventory;
            this.side = position == null ? ForgeDirection.UNKNOWN : position.getFacing();
            this.position = position;
        }

        boolean isValid() { return inventory != null && position != null; }
    }
}
