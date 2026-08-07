package me.desht.modularrouters.container;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ContainerExtruder2ModuleTest {
    @Test
    public void templateHandlerPersistsOrderedStacks() {
        ItemStack moduleStack = new ItemStack(Blocks.stone, 1, 0);
        ContainerExtruder2Module.TemplateHandler writer =
                new ContainerExtruder2Module.TemplateHandler(moduleStack, null);
        writer.setInventorySlotContents(0, new ItemStack(Blocks.stone, 3, 0));
        writer.setInventorySlotContents(1, new ItemStack(Blocks.dirt, 1, 0));
        writer.save();

        assertEquals(3, writer.getStackInSlot(0).stackSize);
        assertEquals(2, moduleStack.getTagCompound().getTagList("Template", 10).tagCount());
        assertNull(writer.getStackInSlot(2));
    }
}
