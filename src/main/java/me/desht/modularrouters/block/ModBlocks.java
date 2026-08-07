package me.desht.modularrouters.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ModBlocks {
    public static BlockItemRouter itemRouter;
    public static BlockTemplateFrame templateFrame;

    public static void init() {
        itemRouter = register(new BlockItemRouter());
        templateFrame = register(new BlockTemplateFrame());
    }

    private static <T extends Block> T register(T block, ItemBlock itemBlock) {
        GameRegistry.registerBlock(block, itemBlock.getClass(), block.getUnlocalizedName().substring(5));
        if (block instanceof BlockBase) {
            ((BlockBase) block).registerItemModel(itemBlock);
        }
        return block;
    }

    private static <T extends Block> T register(T block) {
        ItemBlock itemBlock = new ItemBlock(block);
        return register(block, itemBlock);
    }
}
