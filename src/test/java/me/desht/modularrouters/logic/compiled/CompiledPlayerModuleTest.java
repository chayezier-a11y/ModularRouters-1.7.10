package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CompiledPlayerModuleTest {
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
}
