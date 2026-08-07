package me.desht.modularrouters.item.upgrade;

import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SecurityUpgrade extends Upgrade {
    @Override
    public void onCompiled(ItemStack stack, TileEntityItemRouter router) {
        // Add permitted players from NBT
    }
}
