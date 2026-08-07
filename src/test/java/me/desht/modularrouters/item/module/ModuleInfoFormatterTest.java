package me.desht.modularrouters.item.module;

import net.minecraft.util.EnumChatFormatting;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModuleInfoFormatterTest {
    @Test
    public void formatsFilterFlagsWithOriginalColors() {
        assertEquals(EnumChatFormatting.DARK_GRAY.toString() + "NBT" + EnumChatFormatting.RESET,
                ModuleInfoFormatter.formatFlag("NBT", true));
        assertEquals(EnumChatFormatting.AQUA.toString() + "NBT" + EnumChatFormatting.RESET,
                ModuleInfoFormatter.formatFlag("NBT", false));
    }

    @Test
    public void choosesRangeColorRelativeToBase() {
        assertEquals(EnumChatFormatting.GREEN.toString(), ModuleInfoFormatter.rangeColor(13, 12));
        assertEquals(EnumChatFormatting.RED.toString(), ModuleInfoFormatter.rangeColor(11, 12));
        assertEquals(EnumChatFormatting.AQUA.toString(), ModuleInfoFormatter.rangeColor(12, 12));
    }

    @Test
    public void formatsSettingLineWithOriginalColors() {
        assertEquals(EnumChatFormatting.YELLOW.toString() + "Direction: " + EnumChatFormatting.AQUA + "Front",
                ModuleInfoFormatter.settingLine("Direction", "Front"));
    }
}
