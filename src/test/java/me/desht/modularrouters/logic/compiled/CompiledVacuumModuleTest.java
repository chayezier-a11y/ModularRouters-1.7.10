package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompiledVacuumModuleTest {
    @Test
    public void readsXpVacuumSettingsFromModuleNbt() {
        ItemStack stack = new ItemStack(new ItemModule(), 1, ItemModule.ModuleType.VACUUM.ordinal());
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte(CompiledVacuumModule.NBT_XP_COLLECTION_TYPE, (byte) 0);
        tag.setBoolean(CompiledVacuumModule.NBT_AUTO_EJECT, true);
        stack.setTagCompound(tag);

        CompiledVacuumModule module = new CompiledVacuumModule(null, stack);

        assertEquals(CompiledVacuumModule.XPCollectionType.BOTTLE, module.getXPCollectionType());
        assertTrue(module.isAutoEjecting());
    }
}
