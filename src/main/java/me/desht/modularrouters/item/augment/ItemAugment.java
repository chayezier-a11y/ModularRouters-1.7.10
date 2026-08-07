package me.desht.modularrouters.item.augment;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.container.handler.AugmentHandler;
import me.desht.modularrouters.item.ItemBase;
import me.desht.modularrouters.item.module.ItemModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

public abstract class ItemAugment extends ItemBase {
    private static final Map<String, ItemAugment> augmentRegistry = new HashMap<>();

    public ItemAugment(String name) {
        super(name);
        augmentRegistry.put(name, this);
    }

    public static ItemAugment getAugment(String name) {
        return augmentRegistry.get(name);
    }

    public abstract int getMaxAugments(Class<? extends me.desht.modularrouters.item.module.Module> moduleClass);

    public String getExtraInfo(int c, ItemStack moduleStack) {
        return "";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        addExtraInformation(stack, player, list, par4);
    }

    @SideOnly(Side.CLIENT)
    protected void addExtraInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
    }

    public static class AugmentCounter {
        private final Map<String, Integer> counts = new HashMap<>();

        public AugmentCounter(ItemStack moduleStack) {
            refresh(moduleStack);
        }

        public void refresh(ItemStack moduleStack) {
            AugmentHandler h = new AugmentHandler(moduleStack, null);
            counts.clear();
            for (int i = 0; i < h.getSizeInventory(); i++) {
                ItemStack augmentStack = h.getStackInSlot(i);
                if (augmentStack != null && augmentStack.getItem() instanceof ItemAugment) {
                    String key = augmentStack.getUnlocalizedName();
                    counts.put(key, counts.getOrDefault(key, 0) + augmentStack.stackSize);
                }
            }
        }

        @SuppressWarnings("unchecked")
        public Collection<ItemAugment> getAugments() {
            List<ItemAugment> result = new ArrayList<>();
            for (String key : counts.keySet()) {
                ItemAugment a = augmentRegistry.get(key.replace("item.", ""));
                if (a != null) result.add(a);
            }
            return result;
        }

        public int getAugmentCount(ItemAugment type) {
            if (type == null) return 0;
            return counts.getOrDefault(type.getUnlocalizedName(), 0);
        }

        public int getAugmentCount(Item type) {
            if (type == null || !(type instanceof ItemAugment)) return 0;
            return counts.getOrDefault(((ItemAugment) type).getUnlocalizedName(), 0);
        }
    }
}
