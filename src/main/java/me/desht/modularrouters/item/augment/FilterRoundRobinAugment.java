package me.desht.modularrouters.item.augment;

public class FilterRoundRobinAugment extends ItemAugment {
    public FilterRoundRobinAugment() { super("filterRoundRobinAugment"); }

    @Override
    public int getMaxAugments(Class<? extends me.desht.modularrouters.item.module.Module> moduleClass) {
        return 1;
    }
}
