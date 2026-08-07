package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;

public class SyncUpgrade extends Upgrade {
    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        int tunedValue = 0;
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("TunedValue")) {
            tunedValue = stack.getTagCompound().getInteger("TunedValue");
        }
        router.setTunedSyncValue(tunedValue);
    }
}
