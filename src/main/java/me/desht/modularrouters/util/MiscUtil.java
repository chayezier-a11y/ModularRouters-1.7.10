package me.desht.modularrouters.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

import java.util.List;

public class MiscUtil {

    public static String translate(String key, Object... args) {
        String s = StatCollector.translateToLocal(key);
        if (args.length > 0) {
            return String.format(s, args);
        }
        return s;
    }

    public static void sendStatusMessage(EntityPlayer player, String key, Object... args) {
        if (!player.worldObj.isRemote) {
            player.addChatMessage(new ChatComponentText(translate(key, args)));
        }
    }

    public static void appendMultiline(List<String> list, String key, Object... args) {
        String s = translate(key, args);
        s = s.replace("${br}", "\n").replace("\\n", "\n");
        for (String line : s.split("\\r?\\n", -1)) {
            list.add(line);
        }
    }
}
