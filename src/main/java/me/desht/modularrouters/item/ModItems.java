package me.desht.modularrouters.item;

import cpw.mods.fml.common.registry.GameRegistry;
import me.desht.modularrouters.item.augment.*;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.smartfilter.ItemSmartFilter;
import me.desht.modularrouters.item.upgrade.ItemUpgrade;
import net.minecraft.item.Item;

public class ModItems {
    public static ItemBase blankModule;
    public static ItemModule module;
    public static ItemBase blankUpgrade;
    public static ItemUpgrade upgrade;
    public static ItemBase overrideCard;
    public static ItemSmartFilter smartFilter;
    public static ItemBase augmentCore;

    // Augment items
    public static ItemAugment fastPickupAugment;
    public static ItemAugment filterRoundRobinAugment;
    public static ItemAugment mimicAugment;
    public static ItemAugment pickupDelayAugment;
    public static ItemAugment pushingAugment;
    public static ItemAugment rangeUpAugment;
    public static ItemAugment rangeDownAugment;
    public static ItemAugment redstoneAugment;
    public static ItemAugment regulatorAugment;
    public static ItemAugment stackAugment;
    public static ItemAugment xpVacuumAugment;

    public static void init() {
        blankModule = register(new ItemBase("blankModule"));
        module = register(new ItemModule(), ItemModule.SUBTYPES);
        blankUpgrade = register(new ItemBase("blankUpgrade"));
        upgrade = register(new ItemUpgrade(), ItemUpgrade.SUBTYPES);
        overrideCard = register(new ItemBase("overrideCard"));
        smartFilter = register(new ItemSmartFilter(), ItemSmartFilter.SUBTYPES);
        augmentCore = register(new ItemBase("augmentCore"));

        // Register augment items
        fastPickupAugment = registerAugment(new FastPickupAugment());
        filterRoundRobinAugment = registerAugment(new FilterRoundRobinAugment());
        mimicAugment = registerAugment(new MimicAugment());
        pickupDelayAugment = registerAugment(new PickupDelayAugment());
        pushingAugment = registerAugment(new PushingAugment());
        rangeUpAugment = registerAugment(new RangeAugments.RangeUpAugment());
        rangeDownAugment = registerAugment(new RangeAugments.RangeDownAugment());
        redstoneAugment = registerAugment(new RedstoneAugment());
        regulatorAugment = registerAugment(new RegulatorAugment());
        stackAugment = registerAugment(new StackAugment());
        xpVacuumAugment = registerAugment(new XPVacuumAugment());
    }

    private static <T extends Item> T register(T item) {
        return register(item, 0);
    }

    private static <T extends Item> T register(T item, int nSubtypes) {
        String name = item.getUnlocalizedName().replace("item.", "");
        GameRegistry.registerItem(item, name);

        if (item instanceof ItemBase) {
            ((ItemBase) item).registerItemModel(nSubtypes);
        }

        return item;
    }

    private static <T extends ItemAugment> T registerAugment(T item) {
        return register(item);
    }
}
