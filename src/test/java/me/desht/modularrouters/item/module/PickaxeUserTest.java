package me.desht.modularrouters.item.module;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PickaxeUserTest {
    @Test
    public void breakerAndExtruderDefaultToIronPickaxe() {
        ItemStack breaker = moduleStack(ItemModule.ModuleType.BREAKER);
        ItemStack extruder = moduleStack(ItemModule.ModuleType.EXTRUDER1);

        assertEquals(Items.iron_pickaxe, new BreakerModule().getPickaxe(breaker).getItem());
        assertEquals(Items.iron_pickaxe, new ExtruderModule1().getPickaxe(extruder).getItem());
    }

    @Test
    public void storedPickaxePreservesMetadataAndNbt() {
        ItemStack moduleStack = moduleStack(ItemModule.ModuleType.BREAKER);
        ItemStack pickaxe = new ItemStack(Items.iron_pickaxe, 1, 7);
        pickaxe.setTagCompound(new NBTTagCompound());
        pickaxe.getTagCompound().setString("marker", "kept");
        IPickaxeUser user = new BreakerModule();

        user.setPickaxe(moduleStack, pickaxe);
        ItemStack restored = user.getPickaxe(moduleStack);

        assertNotNull(restored);
        assertEquals(Items.iron_pickaxe, restored.getItem());
        NBTTagCompound stored = moduleStack.getTagCompound().getCompoundTag(IPickaxeUser.NBT_PICKAXE);
        assertEquals(7, stored.getShort("Damage"));
        assertEquals("kept", stored.getCompoundTag("tag").getString("marker"));
    }

    private static ItemStack moduleStack(ItemModule.ModuleType type) {
        return new ItemStack(new ItemModule(), 1, type.ordinal());
    }
}
