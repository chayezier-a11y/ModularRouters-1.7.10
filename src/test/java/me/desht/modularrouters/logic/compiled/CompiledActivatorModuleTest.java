package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

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

    @Test
    public void rightClickAllowsAnEmptyHandButItemEntityUseDoesNot() {
        assertFalse(CompiledActivatorModule.requiresHeldItem(
                CompiledActivatorModule.ActionType.RIGHT_CLICK));
        assertTrue(CompiledActivatorModule.requiresHeldItem(
                CompiledActivatorModule.ActionType.USE_ITEM_ON_ENTITY));
        assertFalse(CompiledActivatorModule.requiresHeldItem(
                CompiledActivatorModule.ActionType.ATTACK_ENTITY));
    }

    @Test
    public void roundRobinSelectionStartsAtTheFirstEntity() {
        assertEquals(0, CompiledActivatorModule.nextRoundRobinIndex(0, 3));
        assertEquals(1, CompiledActivatorModule.nextRoundRobinIndex(1, 3));
        assertEquals(2, CompiledActivatorModule.nextRoundRobinIndex(2, 3));
    }
}
