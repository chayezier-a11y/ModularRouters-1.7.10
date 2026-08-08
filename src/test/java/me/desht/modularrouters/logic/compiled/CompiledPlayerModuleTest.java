package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

public class CompiledPlayerModuleTest {
    private static final ItemBase TEST_ITEM = new ItemBase("playerModuleTestItem");

    @Test
    public void readsPlayerOperationAndSection() {
        ItemStack stack = new ItemStack(new ItemModule(), 1, ItemModule.ModuleType.PLAYER.ordinal());
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte(CompiledPlayerModule.NBT_OPERATION, (byte) 1);
        tag.setByte(CompiledPlayerModule.NBT_SECTION, (byte) 3);
        tag.setString("Owner", "test-player");
        stack.setTagCompound(tag);

        CompiledPlayerModule module = new CompiledPlayerModule(null, stack);

        assertEquals(CompiledPlayerModule.Operation.INSERT, module.getOperation());
        assertEquals(CompiledPlayerModule.Section.OFFHAND, module.getSection());
    }

    @Test
    public void sectionViewsMapMainNoHotbarArmorEnderAndOffhand() {
        InventoryPlayer inventory = new InventoryPlayer(null);
        InventoryEnderChest ender = new InventoryEnderChest();
        inventory.mainInventory[0] = new ItemStack(TEST_ITEM, 1, 0);
        inventory.mainInventory[9] = new ItemStack(TEST_ITEM, 2, 0);
        inventory.mainInventory[35] = new ItemStack(TEST_ITEM, 3, 0);
        ender.setInventorySlotContents(0, new ItemStack(TEST_ITEM, 4, 0));

        CompiledPlayerModule.PlayerInventoryView main = view(inventory, ender,
                CompiledPlayerModule.Section.MAIN);
        CompiledPlayerModule.PlayerInventoryView noHotbar = view(inventory, ender,
                CompiledPlayerModule.Section.MAIN_NO_HOTBAR);
        CompiledPlayerModule.PlayerInventoryView armor = view(inventory, ender,
                CompiledPlayerModule.Section.ARMOR);
        CompiledPlayerModule.PlayerInventoryView offhand = view(inventory, ender,
                CompiledPlayerModule.Section.OFFHAND);
        CompiledPlayerModule.PlayerInventoryView enderView = view(inventory, ender,
                CompiledPlayerModule.Section.ENDER);

        assertEquals(36, main.getSizeInventory());
        assertEquals(1, main.getStackInSlot(0).stackSize);
        assertEquals(27, noHotbar.getSizeInventory());
        assertEquals(2, noHotbar.getStackInSlot(0).stackSize);
        assertEquals(3, noHotbar.getStackInSlot(26).stackSize);
        assertEquals(4, armor.getSizeInventory());
        assertEquals(0, offhand.getSizeInventory());
        assertEquals(27, enderView.getSizeInventory());
        assertEquals(4, enderView.getStackInSlot(0).stackSize);
    }

    @Test
    public void extractsFromNoHotbarWithoutTouchingHotbar() {
        InventoryPlayer inventory = new InventoryPlayer(null);
        inventory.mainInventory[0] = new ItemStack(TEST_ITEM, 3, 0);
        inventory.mainInventory[9] = new ItemStack(TEST_ITEM, 3, 0);
        TileEntityItemRouter router = new TileEntityItemRouter();
        CompiledPlayerModule module = module(CompiledPlayerModule.Operation.EXTRACT,
                CompiledPlayerModule.Section.MAIN_NO_HOTBAR);

        assertTrue(module.executeWithInventory(view(inventory, new InventoryEnderChest(),
                CompiledPlayerModule.Section.MAIN_NO_HOTBAR), router));
        assertEquals(3, inventory.mainInventory[0].stackSize);
        assertEquals(2, inventory.mainInventory[9].stackSize);
        assertEquals(1, router.getBufferItemStack().stackSize);
    }

    @Test
    public void insertsOnlyMatchingArmorIntoItsEmptySlot() {
        InventoryPlayer inventory = new InventoryPlayer(null);
        TileEntityItemRouter router = new TileEntityItemRouter();
        ItemArmor helmet = new ItemArmor(ItemArmor.ArmorMaterial.IRON, 0, 0);
        router.getBuffer().setInventorySlotContents(0, new ItemStack(helmet, 1, 0));
        CompiledPlayerModule module = module(CompiledPlayerModule.Operation.INSERT,
                CompiledPlayerModule.Section.ARMOR);

        assertTrue(module.executeWithInventory(view(inventory, new InventoryEnderChest(),
                CompiledPlayerModule.Section.ARMOR), router));
        assertNull(router.getBufferItemStack());
        assertEquals(helmet, inventory.armorInventory[3].getItem());
        assertNull(inventory.armorInventory[0]);
    }

    @Test
    public void offhandIsNoOpAndRegulatorCapsInsertAmount() {
        InventoryPlayer inventory = new InventoryPlayer(null);
        TileEntityItemRouter router = new TileEntityItemRouter();
        router.getBuffer().setInventorySlotContents(0, new ItemStack(TEST_ITEM, 3, 0));
        CompiledPlayerModule module = module(CompiledPlayerModule.Operation.INSERT,
                CompiledPlayerModule.Section.OFFHAND);

        assertFalse(module.executeWithInventory(view(inventory, new InventoryEnderChest(),
                CompiledPlayerModule.Section.OFFHAND), router));
        assertEquals(2, CompiledPlayerModule.regulatedTransferAmount(8, 3, 5));
        assertEquals(0, CompiledPlayerModule.regulatedTransferAmount(8, 5, 5));
        assertEquals(8, CompiledPlayerModule.regulatedTransferAmount(8, 20, 0));
    }

    @Test
    public void uuidBindingTakesPrecedenceWithLegacyNameFallback() {
        UUID owner = UUID.fromString("11111111-2222-3333-4444-555555555555");
        UUID other = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

        assertTrue(CompiledPlayerModule.matchesOwner(owner, "old", owner, "new"));
        assertFalse(CompiledPlayerModule.matchesOwner(owner, "same", other, "same"));
        assertTrue(CompiledPlayerModule.matchesOwner(null, "legacy", other, "legacy"));
        assertFalse(CompiledPlayerModule.matchesOwner(null, "legacy", other, "other"));
    }

    private static CompiledPlayerModule module(CompiledPlayerModule.Operation operation,
                                               CompiledPlayerModule.Section section) {
        ItemStack stack = new ItemStack(new ItemModule(), 1, ItemModule.ModuleType.PLAYER.ordinal());
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setByte(CompiledPlayerModule.NBT_OPERATION, (byte) operation.ordinal());
        stack.getTagCompound().setByte(CompiledPlayerModule.NBT_SECTION, (byte) section.ordinal());
        return new CompiledPlayerModule(null, stack);
    }

    private static CompiledPlayerModule.PlayerInventoryView view(InventoryPlayer inventory,
                                                                  InventoryEnderChest ender,
                                                                  CompiledPlayerModule.Section section) {
        return new CompiledPlayerModule.PlayerInventoryView(inventory, ender, null, section);
    }
}
