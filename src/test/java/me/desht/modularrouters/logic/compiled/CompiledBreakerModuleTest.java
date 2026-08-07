package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CompiledBreakerModuleTest {
    @Test
    public void readsBreakerMatchTypeFromModuleNbt() {
        ItemStack stack = new ItemStack(new ItemModule(), 1, ItemModule.ModuleType.BREAKER.ordinal());
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte(CompiledBreakerModule.NBT_MATCH_TYPE, (byte) 1);
        stack.setTagCompound(tag);

        CompiledBreakerModule module = new CompiledBreakerModule(null, stack);

        assertEquals(CompiledBreakerModule.MatchType.BLOCK, module.getMatchType());
    }
}
