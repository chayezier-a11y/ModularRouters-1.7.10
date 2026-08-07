package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class CompiledVoidModule extends CompiledModule {
    public CompiledVoidModule(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    public boolean execute(@Nonnull TileEntityItemRouter router) {
        ItemStack stack = router.getBufferItemStack();
        if (getFilter().allowItem(stack)) {
            int toVoid = Math.min(getItemsPerTick(router), stack.stackSize - getRegulationAmount());
            if (toVoid <= 0) {
                return false;
            }
            router.extractBuffer(toVoid);
            return true;
        }
        return false;
    }
}
