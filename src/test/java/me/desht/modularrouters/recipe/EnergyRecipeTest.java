package me.desht.modularrouters.recipe;

import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.item.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class EnergyRecipeTest {
    private ItemBase oldBlankModule;
    private ItemModule oldModule;
    private ItemBase oldBlankUpgrade;
    private ItemUpgrade oldUpgrade;

    @Before
    public void installRecipeItems() {
        oldBlankModule = ModItems.blankModule;
        oldModule = ModItems.module;
        oldBlankUpgrade = ModItems.blankUpgrade;
        oldUpgrade = ModItems.upgrade;
        ModItems.blankModule = new ItemBase("testBlankModule");
        ModItems.module = new ItemModule();
        ModItems.blankUpgrade = new ItemBase("testBlankUpgrade");
        ModItems.upgrade = new ItemUpgrade();
    }

    @After
    public void restoreRecipeItems() {
        ModItems.blankModule = oldBlankModule;
        ModItems.module = oldModule;
        ModItems.blankUpgrade = oldBlankUpgrade;
        ModItems.upgrade = oldUpgrade;
    }

    @Test
    public void energyOutputUsesSevenFiveFourPattern() {
        assertArrayEquals(new Object[] {
                " R ", "GBG", " Q ",
                'R', "blockRedstone", 'G', "ingotGold",
                'B', ModItems.blankModule, 'Q', "gemQuartz"
        }, ModRecipes.energyOutputDefinition());
        assertEquals(ItemModule.ModuleType.ENERGY_OUTPUT.ordinal(),
                ModRecipes.energyOutputResult().getMetadata());
    }

    @Test
    public void energyDistributorCombinesOutputAndDistributorModules() {
        ItemStack[] input = ModRecipes.energyDistributorIngredients();

        assertEquals(ItemModule.ModuleType.ENERGY_OUTPUT.ordinal(), input[0].getMetadata());
        assertEquals(ItemModule.ModuleType.DISTRIBUTOR.ordinal(), input[1].getMetadata());
        assertEquals(ItemModule.ModuleType.ENERGY_DISTRIBUTOR.ordinal(),
                ModRecipes.energyDistributorResult().getMetadata());
    }

    @Test
    public void energyUpgradeUsesSevenFiveFourPattern() {
        assertArrayEquals(new Object[] {
                "QRQ", " B ", "QGQ",
                'Q', "gemQuartz", 'R', "blockRedstone",
                'B', ModItems.blankUpgrade, 'G', "ingotGold"
        }, ModRecipes.energyUpgradeDefinition());
        assertEquals(ItemUpgrade.UpgradeType.ENERGY.ordinal(),
                ModRecipes.energyUpgradeResult().getMetadata());
    }
}
