package me.desht.modularrouters.gui;

import me.desht.modularrouters.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.gui.GuiButton;

import java.util.ArrayList;
import java.util.List;

public final class GuiTooltip {
    private GuiTooltip() {}

    public static List<String> split(String text) {
        List<String> result = new ArrayList<String>();
        if (text == null || text.length() == 0) return result;

        String[] lines = text.split("\\s*\\$\\{br\\}\\s*|\\r?\\n");
        for (String line : lines) {
            result.add(line.trim());
        }
        return result;
    }

    public static void append(List<String> target, String key, Object... args) {
        target.addAll(split(MiscUtil.translate(key, args)));
    }

    public static List<String> getHoveredTooltip(List buttonList, int mouseX, int mouseY) {
        for (Object obj : buttonList) {
            GuiButton button = (GuiButton) obj;
            if (button.visible && button.enabled
                    && mouseX >= button.xPosition && mouseY >= button.yPosition
                    && mouseX < button.xPosition + button.width
                    && mouseY < button.yPosition + button.height
                    && button instanceof ITooltipButton) {
                List<String> tooltip = ((ITooltipButton) button).getTooltip();
                if (tooltip != null && !tooltip.isEmpty()) return tooltip;
            }
        }
        return null;
    }
}
