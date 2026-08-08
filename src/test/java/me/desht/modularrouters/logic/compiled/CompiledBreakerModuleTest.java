package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import me.desht.modularrouters.item.ItemBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void storedPickaxeControlsHarvestPermission() {
        assertTrue(CompiledBreakerModule.canHarvest(0, "pickaxe",
                new ItemStack(new HarvestPickaxe(2))));
        assertFalse(CompiledBreakerModule.canHarvest(3, "pickaxe",
                new ItemStack(new HarvestPickaxe(2))));
    }

    private static class HarvestPickaxe extends ItemBase {
        private final int level;

        private HarvestPickaxe(int level) {
            super("breakerHarvestPickaxe" + level);
            this.level = level;
        }

        @Override
        public int getHarvestLevel(ItemStack stack, String toolClass) {
            return "pickaxe".equals(toolClass) ? level : -1;
        }
    }
}
