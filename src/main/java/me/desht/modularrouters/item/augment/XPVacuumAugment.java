package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.VacuumModule;

public class XPVacuumAugment extends ItemAugment {
    public XPVacuumAugment() { super("xpVacuumAugment"); }

    @Override
    public int getMaxAugments(Class<? extends me.desht.modularrouters.item.module.Module> moduleClass) {
        return moduleClass == VacuumModule.class ? 1 : 0;
    }
}
