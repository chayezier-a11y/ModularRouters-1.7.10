package me.desht.modularrouters.util;

import java.util.HashMap;
import java.util.Map;

public class ModNameCache {
    private static final Map<String, String> cache = new HashMap<String, String>();

    public static void init() {
        cache.clear();
    }

    public static String getModName(String modId) {
        if (cache.containsKey(modId)) {
            return cache.get(modId);
        }
        // In 1.7.10, we can use Loader to get mod names
        String name = cpw.mods.fml.common.Loader.instance().getIndexedModList().get(modId) != null
                ? cpw.mods.fml.common.Loader.instance().getIndexedModList().get(modId).getName()
                : modId;
        cache.put(modId, name);
        return name;
    }
}
