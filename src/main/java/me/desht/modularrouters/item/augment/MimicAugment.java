package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ExtruderModule2;

public class MimicAugment extends ItemAugment {
    public MimicAugment() { super("mimicAugment"); }

    @Override
    public int getMaxAugments(Class<? extends me.desht.modularrouters.item.module.Module> moduleClass) {
        return moduleClass == ExtruderModule2.class ? 1 : 0;
    }
}
