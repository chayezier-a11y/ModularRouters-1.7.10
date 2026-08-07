package me.desht.modularrouters.item.augment;

import me.desht.modularrouters.item.module.IRangedModule;

public abstract class RangeAugments extends ItemAugment {
    public RangeAugments(String name) { super(name); }

    public static class RangeUpAugment extends RangeAugments {
        public RangeUpAugment() { super("rangeUpAugment"); }

        @Override
        public int getMaxAugments(Class<? extends me.desht.modularrouters.item.module.Module> moduleClass) {
            try {
                me.desht.modularrouters.item.module.Module m = moduleClass.newInstance();
                if (m instanceof IRangedModule) {
                    IRangedModule r = (IRangedModule) m;
                    return r.getHardMaxRange() - r.getBaseRange();
                }
            } catch (Exception ignored) {}
            return 0;
        }
    }

    public static class RangeDownAugment extends RangeAugments {
        public RangeDownAugment() { super("rangeDownAugment"); }

        @Override
        public int getMaxAugments(Class<? extends me.desht.modularrouters.item.module.Module> moduleClass) {
            try {
                me.desht.modularrouters.item.module.Module m = moduleClass.newInstance();
                if (m instanceof IRangedModule) {
                    IRangedModule r = (IRangedModule) m;
                    return r.getBaseRange() - 1;
                }
            } catch (Exception ignored) {}
            return 0;
        }
    }
}
