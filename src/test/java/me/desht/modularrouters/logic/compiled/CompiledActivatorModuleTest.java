package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompiledActivatorModuleTest {
    @Test
    public void readsActivatorSettingsFromModuleNbt() {
        ItemStack stack = new ItemStack(new ItemModule(), 1, ItemModule.ModuleType.ACTIVATOR.ordinal());
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setByte(CompiledActivatorModule.NBT_ACTION_TYPE, (byte) 2);
        stack.getTagCompound().setByte(CompiledActivatorModule.NBT_LOOK_DIRECTION, (byte) 1);
        stack.getTagCompound().setByte(CompiledActivatorModule.NBT_ENTITY_MODE, (byte) 2);
        stack.getTagCompound().setBoolean(CompiledActivatorModule.NBT_SNEAKING, true);

        CompiledActivatorModule module = new CompiledActivatorModule(null, stack);

        assertEquals(CompiledActivatorModule.ActionType.ATTACK_ENTITY, module.getActionType());
        assertEquals(CompiledActivatorModule.LookDirection.ABOVE, module.getLookDirection());
        assertEquals(CompiledActivatorModule.EntityMode.ROUND_ROBIN, module.getEntityMode());
        assertTrue(module.isSneaking());
    }
}
