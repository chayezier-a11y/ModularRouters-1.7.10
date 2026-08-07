package me.desht.modularrouters.integration;

import cpw.mods.fml.common.Loader;

public class IntegrationHandler {

    public static boolean wailaLoaded;
    public static boolean cofhLoaded;

    public static void init() {
        wailaLoaded = Loader.isModLoaded("Waila");
        cofhLoaded = Loader.isModLoaded("CoFHCore");
    }
}
