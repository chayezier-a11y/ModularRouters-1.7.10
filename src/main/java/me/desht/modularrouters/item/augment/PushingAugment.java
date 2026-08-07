package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.ExtruderModule1;
import me.desht.modularrouters.item.module.ExtruderModule2;

public class PushingAugment extends ItemAugment {
    public PushingAugment() { super("pushingAugment"); }

    @Override
    public int getMaxAugments(Class<? extends me.desht.modularrouters.item.module.Module> moduleClass) {
        return moduleClass == ExtruderModule1.class || moduleClass == ExtruderModule2.class ? 64 : 0;
    }
}
