package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompiledFluidModuleTest {
    @Test
    public void readsFluidSettingsFromModuleNbt() {
        ItemStack stack = new ItemStack(new ItemModule(), 1, ItemModule.ModuleType.FLUID1.ordinal());
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setInteger(CompiledFluidModule1.NBT_MAX_TRANSFER, 250);
        stack.getTagCompound().setByte(CompiledFluidModule1.NBT_FLUID_DIRECTION, (byte) 1);
        stack.getTagCompound().setBoolean(CompiledFluidModule1.NBT_FORCE_EMPTY, true);
        stack.getTagCompound().setBoolean(CompiledFluidModule1.NBT_REGULATE_ABSOLUTE, true);

        CompiledFluidModule1 module = new CompiledFluidModule1(null, stack);

        assertEquals(250, module.getMaxTransfer());
        assertEquals(me.desht.modularrouters.item.module.FluidModule1.FluidDirection.OUT,
                module.getFluidDirection());
        assertTrue(module.isForceEmpty());
        assertTrue(module.isRegulateAbsolute());
    }
}
