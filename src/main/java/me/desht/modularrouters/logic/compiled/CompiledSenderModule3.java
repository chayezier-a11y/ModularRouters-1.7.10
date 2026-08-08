package me.desht.modularrouters.logic.compiled;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;

public class CompiledSenderModule3 extends CompiledSenderModule2 {
    public CompiledSenderModule3(TileEntityItemRouter router, ItemStack stack) {
        super(router, stack);
    }

    @Override
    boolean isRangeLimited() {
        return false;
    }

    @Override
    protected int getBeamColor() {
        return 0x800080;
    }
}
