package me.desht.modularrouters.item.module;

import net.minecraft.util.EnumChatFormatting;

/** Formatting shared by the module item tooltip and its specialized entries. */
public final class ModuleInfoFormatter {
    private ModuleInfoFormatter() {}

    public static String formatFlag(String text, boolean enabled) {
        return (enabled ? EnumChatFormatting.DARK_GRAY : EnumChatFormatting.AQUA)
                + text + EnumChatFormatting.RESET;
    }

    public static String rangeColor(int current, int base) {
        if (current > base) return EnumChatFormatting.GREEN.toString();
        if (current < base) return EnumChatFormatting.RED.toString();
        return EnumChatFormatting.AQUA.toString();
    }

    public static String settingLine(String label, String value) {
        return EnumChatFormatting.YELLOW + label + ": " + EnumChatFormatting.AQUA + value;
    }
}
