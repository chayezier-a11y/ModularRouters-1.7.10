package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CompiledDetectorModuleTest {
    @Test
    public void detectorSignalIsClampedAndInvalidValuesUseDefault() {
        ItemStack stack = moduleStack();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(CompiledDetectorModule.NBT_SIGNAL_LEVEL, 99);
        stack.setTagCompound(tag);
        assertEquals(15, new CompiledDetectorModule(null, stack).getSignalLevel());

        ItemStack invalid = moduleStack();
        invalid.setTagCompound(new NBTTagCompound());
        invalid.getTagCompound().setInteger(CompiledDetectorModule.NBT_SIGNAL_LEVEL, -4);
        assertEquals(0, new CompiledDetectorModule(null, invalid).getSignalLevel());
    }

    @Test
    public void detectorEmissionRemainsEnabledUntilLastDetectorCleansUp() {
        TileEntityItemRouter router = new TileEntityItemRouter();
        router.registerDetector();
        router.registerDetector();
        router.unregisterDetector();
        assertEquals(0, router.getRedstoneLevel(0, false));
        router.unregisterDetector();
        assertEquals(-1, router.getRedstoneLevel(0, false));
    }

    private static ItemStack moduleStack() {
        return new ItemStack(new ItemModule(), 1, ItemModule.ModuleType.DETECTOR.ordinal());
    }
}
