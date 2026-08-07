package me.desht.modularrouters.recipe;

import cpw.mods.fml.common.registry.GameRegistry;
import me.desht.modularrouters.block.ModBlocks;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.ItemModule.ModuleType;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter.FilterType;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import me.desht.modularrouters.item.upgrade.ItemUpgrade.UpgradeType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ModRecipes {

    public static void init() {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.itemRouter, 4),
                "IBI", "BMB", "IBI",
                'I', "ingotIron",
                'B', Blocks.iron_bars,
                'M', ModItems.blankModule));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.templateFrame),
                "SSS", "SPS", "SSS",
                'S', "stickWood",
                'P', Items.paper));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.blankModule, 6),
                " R ", "PPP", "GGG",
                'R', Items.redstone,
                'P', Items.paper,
                'G', Items.gold_nugget));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.blankUpgrade, 6),
                "PPG", "PLG", " PG",
                'P', Items.paper,
                'L', new ItemStack(Items.dye, 1, 4),
                'G', Items.gold_nugget));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.augmentCore, 4),
                " P ", "PGP", " P ",
                'P', Items.paper,
                'G', Items.gold_nugget));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.overrideCard),
                "P", "I",
                'P', Items.paper,
                'I', "ingotIron"));

        addModuleRecipes();
        addUpgradeRecipes();
        addFilterRecipes();
        addAugmentRecipes();
    }

    private static void addModuleRecipes() {
        module(ModuleType.BREAKER, Items.iron_pickaxe);
        module(ModuleType.DROPPER, Blocks.dropper);
        module(ModuleType.PLACER, Blocks.piston);
        module(ModuleType.PULLER1, Blocks.hopper);
        module(ModuleType.SENDER1, Items.ender_pearl);
        GameRegistry.addRecipe(new ShapelessOreRecipe(module(ModuleType.SENDER2), module(ModuleType.SENDER1), Items.redstone, Items.ender_pearl));
        GameRegistry.addRecipe(new ShapelessOreRecipe(module(ModuleType.SENDER3), module(ModuleType.SENDER2), Items.ender_pearl, Items.diamond));
        module(ModuleType.VACUUM, Blocks.hopper, Items.ender_pearl);
        module(ModuleType.VOID, Items.lava_bucket);
        module(ModuleType.DETECTOR, Items.comparator);
        module(ModuleType.FLINGER, Items.bow);
        module(ModuleType.PLAYER, Items.ender_pearl, Blocks.chest);
        module(ModuleType.EXTRUDER1, Blocks.piston, Blocks.cobblestone);
        module(ModuleType.FLUID1, Items.bucket);
        GameRegistry.addRecipe(new ShapelessOreRecipe(module(ModuleType.PULLER2), module(ModuleType.PULLER1), Items.diamond));
        GameRegistry.addRecipe(new ShapelessOreRecipe(module(ModuleType.EXTRUDER2), module(ModuleType.EXTRUDER1), Items.diamond));
        module(ModuleType.ACTIVATOR, Items.iron_sword);
        module(ModuleType.DISTRIBUTOR, Items.diamond, Items.ender_pearl);
        GameRegistry.addRecipe(new ShapelessOreRecipe(module(ModuleType.CREATIVE), module(ModuleType.SENDER3), Items.nether_star));
        module(ModuleType.ENERGY_DISTRIBUTOR, Items.redstone, Items.glowstone_dust);
        module(ModuleType.ENERGY_OUTPUT, Blocks.redstone_block);
        GameRegistry.addRecipe(new ShapelessOreRecipe(module(ModuleType.FLUID2), module(ModuleType.FLUID1), Items.diamond));
    }

    private static void addUpgradeRecipes() {
        upgrade(UpgradeType.STACK, Blocks.chest);
        upgrade(UpgradeType.SPEED, Items.sugar);
        upgrade(UpgradeType.SECURITY, Items.name_tag);
        upgrade(UpgradeType.CAMOUFLAGE, new ItemStack(Items.dye, 1, 15));
        upgrade(UpgradeType.SYNC, Items.clock);
        upgrade(UpgradeType.FLUID, Items.bucket);
        upgrade(UpgradeType.MUFFLER, Blocks.wool);
        upgrade(UpgradeType.BLAST, Blocks.obsidian);
        upgrade(UpgradeType.ENERGY, Blocks.redstone_block);
    }

    private static void addFilterRecipes() {
        filter(FilterType.BULKITEM, Blocks.chest);
        filter(FilterType.MOD, Items.book);
        filter(FilterType.REGEX, Items.writable_book);
        filter(FilterType.INSPECTION, Items.comparator);
    }

    private static void addAugmentRecipes() {
        augment("fastPickupAugment", Items.sugar);
        augment("filterRoundRobinAugment", Items.comparator);
        augment("mimicAugment", Blocks.stone);
        augment("pickupDelayAugment", Items.feather);
        augment("pushingAugment", Blocks.piston);
        augment("rangeUpAugment", Items.ender_pearl);
        augment("rangeDownAugment", Items.gunpowder);
        augment("redstoneAugment", Items.redstone);
        augment("regulatorAugment", new ItemStack(Items.dye, 1, 4));
        augment("stackAugment", Blocks.chest);
        augment("xpVacuumAugment", Items.glass_bottle);
    }

    private static void augment(String name, Object ingredient) {
        ItemAugment aug = ItemAugment.getAugment(name);
        if (aug != null) {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(aug), ModItems.augmentCore, ingredient));
        }
    }

    private static void module(ModuleType type, Object ingredient) {
        GameRegistry.addRecipe(new ShapelessOreRecipe(module(type), ModItems.blankModule, ingredient));
    }

    private static void module(ModuleType type, Object ingredient1, Object ingredient2) {
        GameRegistry.addRecipe(new ShapelessOreRecipe(module(type), ModItems.blankModule, ingredient1, ingredient2));
    }

    private static ItemStack module(ModuleType type) {
        return ItemModule.makeItemStack(type);
    }

    private static void upgrade(UpgradeType type, Object ingredient) {
        GameRegistry.addRecipe(new ShapelessOreRecipe(upgrade(type), ModItems.blankUpgrade, ingredient));
    }

    private static ItemStack upgrade(UpgradeType type) {
        return ItemUpgrade.makeItemStack(type, 1);
    }

    private static void filter(FilterType type, Object ingredient) {
        GameRegistry.addRecipe(new ShapelessOreRecipe(ItemSmartFilter.makeItemStack(type), ModItems.blankModule, ingredient));
    }
}
