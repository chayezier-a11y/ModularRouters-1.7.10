package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModularRoutersFakePlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

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
        return operation == Operation.EXTRACT ? extract(owner, router) : insert(owner, router);
    }

    private boolean extract(EntityPlayer owner, TileEntityItemRouter router) {
        if (router.isBufferFull()) return false;
        for (int i = 0; i < getSectionSize(owner); i++) {
            ItemStack stack = getSectionStack(owner, i);
            if (stack == null || getFilter().rejectItem(stack)) continue;
            int amount = Math.min(stack.stackSize, getItemsPerTick(router));
            ItemStack taken = decrSectionStack(owner, i, amount);
            if (taken == null) continue;
            ItemStack remaining = router.insertBuffer(taken);
            if (remaining != null) restoreSectionStack(owner, i, remaining);
            return remaining == null || remaining.stackSize < taken.stackSize;
        }
        return false;
    }

    private boolean insert(EntityPlayer owner, TileEntityItemRouter router) {
        ItemStack buffer = router.getBufferItemStack();
        if (buffer == null || getFilter().rejectItem(buffer)) return false;
        int amount = Math.min(buffer.stackSize, getItemsPerTick(router));
        for (int i = 0; i < getSectionSize(owner); i++) {
            ItemStack existing = getSectionStack(owner, i);
            if (existing != null && (!existing.isItemEqual(buffer)
                    || !ItemStack.areItemStackTagsEqual(existing, buffer)
                    || existing.stackSize >= existing.getMaxStackSize())) continue;
            if (existing == null && section == Section.ARMOR && !canEquipInSlot(owner, buffer, i)) continue;
            int limit = existing == null ? buffer.getMaxStackSize() : existing.getMaxStackSize() - existing.stackSize;
            int toSend = Math.min(amount, Math.min(limit, 64));
            if (toSend <= 0) continue;
            ItemStack extracted = router.extractBuffer(toSend);
            if (extracted == null) return false;
            if (existing == null) {
                setSectionStack(owner, i, extracted);
            } else {
                existing.stackSize += extracted.stackSize;
            }
            return true;
        }
        return false;
    }

    private EntityPlayer findOwner(TileEntityItemRouter router) {
        for (Object obj : router.getWorldObj().playerEntities) {
            EntityPlayer player = (EntityPlayer) obj;
            if (ownerId != null && ownerId.equals(player.getUniqueID())) return player;
            if (ownerId == null && ownerName != null && ownerName.equals(player.getCommandSenderName())) return player;
        }
        return null;
    }

    private int getSectionSize(EntityPlayer owner) {
        switch (section) {
            case MAIN: return 36;
            case MAIN_NO_HOTBAR: return 27;
            case ARMOR: return 4;
            case ENDER: return owner.getInventoryEnderChest().getSizeInventory();
            default: return 0;
        }
    }

    private ItemStack getSectionStack(EntityPlayer owner, int index) {
        switch (section) {
            case MAIN: return owner.inventory.getStackInSlot(index);
            case MAIN_NO_HOTBAR: return owner.inventory.getStackInSlot(index + 9);
            case ARMOR: return owner.inventory.armorInventory[index];
            case ENDER: return owner.getInventoryEnderChest().getStackInSlot(index);
            default: return null;
        }
    }

    private ItemStack decrSectionStack(EntityPlayer owner, int index, int amount) {
        switch (section) {
            case MAIN: return owner.inventory.decrStackSize(index, amount);
            case MAIN_NO_HOTBAR: return owner.inventory.decrStackSize(index + 9, amount);
            case ARMOR: return decrArmor(owner, index, amount);
            case ENDER: return owner.getInventoryEnderChest().decrStackSize(index, amount);
            default: return null;
        }
    }

    private ItemStack decrArmor(EntityPlayer owner, int index, int amount) {
        ItemStack stack = owner.inventory.armorInventory[index];
        if (stack == null) return null;
        owner.inventory.armorInventory[index] = null;
        return stack;
    }

    private void restoreSectionStack(EntityPlayer owner, int index, ItemStack stack) {
        ItemStack existing = getSectionStack(owner, index);
        if (existing == null) setSectionStack(owner, index, stack);
        else existing.stackSize += stack.stackSize;
    }

    private void setSectionStack(EntityPlayer owner, int index, ItemStack stack) {
        switch (section) {
            case MAIN: owner.inventory.setInventorySlotContents(index, stack); break;
            case MAIN_NO_HOTBAR: owner.inventory.setInventorySlotContents(index + 9, stack); break;
            case ARMOR: owner.inventory.armorInventory[index] = stack; break;
            case ENDER: owner.getInventoryEnderChest().setInventorySlotContents(index, stack); break;
            default: break;
        }
    }

    private boolean canEquipInSlot(EntityPlayer owner, ItemStack stack, int slot) {
        if (stack.getItem() instanceof ItemArmor) {
            return slot == 3 - ((ItemArmor) stack.getItem()).armorType;
        }
        return stack.getItem().isValidArmor(stack, 3 - slot, owner);
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
