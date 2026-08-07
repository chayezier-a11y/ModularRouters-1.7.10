package me.desht.modularrouters.integration;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class XPCollection {
    private static boolean detected = false;
    private static Class<?> xpType = EntityXPOrb.class;

    public static void detectXPTypes() {
        detected = true;
    }

    public static boolean isXPOrb(Object entity) {
        return entity instanceof EntityXPOrb;
    }

    public static int getXpValue(EntityXPOrb orb) {
        return orb.xpValue;
    }
}
