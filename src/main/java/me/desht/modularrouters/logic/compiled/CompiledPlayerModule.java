package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.logic.InventoryTransfer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.UUID;

public class CompiledPlayerModule extends CompiledModule {
    public static final String NBT_OPERATION = "Operation";
    public static final String NBT_SECTION = "Section";

    public enum Operation { EXTRACT, INSERT }
    public enum Section { MAIN, MAIN_NO_HOTBAR, ARMOR, OFFHAND, ENDER }

    private final Operation operation;
    private final Section section;
    private final String ownerName;
    private final UUID ownerId;

    public CompiledPlayerModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
        operation = enumValue(stack, NBT_OPERATION, Operation.values(), Operation.EXTRACT);
        section = enumValue(stack, NBT_SECTION, Section.values(), Section.MAIN);
        ownerName = ItemModule.getOwnerName(stack);
        ownerId = ItemModule.getOwnerId(stack);
    }

    @Override
    public boolean hasTarget() {
        return ownerName != null || ownerId != null;
    }

    @Override
    public boolean execute(TileEntityItemRouter router) {
        EntityPlayer owner = findOwner(router);
        if (owner == null || section == Section.OFFHAND) return false;
        return executeWithInventory(new PlayerInventoryView(owner.inventory,
                owner.getInventoryEnderChest(), owner, section), router);
    }

    boolean executeWithInventory(IInventory inventory, TileEntityItemRouter router) {
        if (inventory == null || inventory.getSizeInventory() == 0) return false;
        if (operation == Operation.EXTRACT) {
            return !router.isBufferFull()
                    && transferToRouter(inventory, ForgeDirection.UNKNOWN, router) != null;
        }
        ItemStack buffer = router.getBufferItemStack();
        if (buffer == null || getFilter().rejectItem(buffer)) return false;
        int amount = Math.min(buffer.stackSize, getItemsPerTick(router));
        if (getRegulationAmount() > 0) {
            int existing = InventoryTransfer.count(inventory, ForgeDirection.UNKNOWN,
                    buffer, false, true);
            amount = regulatedTransferAmount(amount, existing, getRegulationAmount());
        }
        return amount > 0 && InventoryTransfer.transfer(router.getBuffer(), ForgeDirection.UNKNOWN,
                inventory, ForgeDirection.UNKNOWN, buffer, amount) > 0;
    }

    private EntityPlayer findOwner(TileEntityItemRouter router) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server != null && server.getConfigurationManager() != null) {
            EntityPlayer player = findOwner(server.getConfigurationManager().playerEntityList);
            if (player != null) return player;
        }
        return router.getWorldObj() == null ? null : findOwner(router.getWorldObj().playerEntities);
    }

    private EntityPlayer findOwner(List players) {
        for (Object value : players) {
            if (!(value instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer) value;
            if (matchesOwner(ownerId, ownerName, player.getUniqueID(),
                    player.getCommandSenderName())) return player;
        }
        return null;
    }

    static boolean matchesOwner(UUID ownerId, String ownerName, UUID playerId, String playerName) {
        return ownerId != null ? ownerId.equals(playerId)
                : ownerName != null && ownerName.equals(playerName);
    }

    static int regulatedTransferAmount(int requested, int existing, int regulation) {
        return regulation <= 0 ? requested : Math.max(0, Math.min(requested, regulation - existing));
    }

    static class PlayerInventoryView implements IInventory {
        private final InventoryPlayer inventory;
        private final InventoryEnderChest ender;
        private final EntityPlayer owner;
        private final Section section;

        PlayerInventoryView(InventoryPlayer inventory, InventoryEnderChest ender,
                            EntityPlayer owner, Section section) {
            this.inventory = inventory;
            this.ender = ender;
            this.owner = owner;
            this.section = section;
        }

        @Override
        public int getSizeInventory() {
            switch (section) {
                case MAIN: return 36;
                case MAIN_NO_HOTBAR: return 27;
                case ARMOR: return 4;
                case ENDER: return ender == null ? 0 : ender.getSizeInventory();
                default: return 0;
            }
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (section == Section.ENDER) return ender.getStackInSlot(slot);
            return inventory.getStackInSlot(mappedSlot(slot));
        }

        @Override
        public ItemStack decrStackSize(int slot, int amount) {
            return section == Section.ENDER ? ender.decrStackSize(slot, amount)
                    : inventory.decrStackSize(mappedSlot(slot), amount);
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int slot) {
            return section == Section.ENDER ? ender.getStackInSlotOnClosing(slot)
                    : inventory.getStackInSlotOnClosing(mappedSlot(slot));
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            if (section == Section.ENDER) ender.setInventorySlotContents(slot, stack);
            else inventory.setInventorySlotContents(mappedSlot(slot), stack);
        }

        @Override public String getInventoryName() { return "player." + section.name().toLowerCase(); }
        @Override public boolean isCustomInventoryName() { return false; }
        @Override public int getInventoryStackLimit() { return section == Section.ARMOR ? 1 : 64; }
        @Override public void markDirty() {
            if (section == Section.ENDER) ender.markDirty(); else inventory.markDirty();
        }
        @Override public boolean isUseableByPlayer(EntityPlayer player) { return true; }
        @Override public void openChest() { if (section == Section.ENDER) ender.openChest(); }
        @Override public void closeChest() { if (section == Section.ENDER) ender.closeChest(); }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            if (section != Section.ARMOR) return true;
            if (stack == null) return false;
            if (stack.getItem() instanceof ItemArmor) {
                return slot == 3 - ((ItemArmor) stack.getItem()).armorType;
            }
            return owner != null && stack.getItem().isValidArmor(stack, 3 - slot, owner);
        }

        private int mappedSlot(int slot) {
            switch (section) {
                case MAIN: return slot;
                case MAIN_NO_HOTBAR: return slot + 9;
                case ARMOR: return slot + 36;
                default: throw new IndexOutOfBoundsException("no slot " + slot + " in " + section);
            }
        }
    }

    private static <T extends Enum<T>> T enumValue(ItemStack stack, String key, T[] values, T fallback) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(key)) return fallback;
        int ordinal = stack.getTagCompound().getByte(key);
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
    }

    public Operation getOperation() { return operation; }
    public Section getSection() { return section; }
    public String getOwnerName() { return ownerName; }
}
