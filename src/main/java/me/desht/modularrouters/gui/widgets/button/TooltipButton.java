package me.desht.modularrouters.gui.widgets.button;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class TooltipButton extends GuiButton implements ITooltipButton {
    private final List<String> tooltip = new ArrayList<String>();
    private final Map<Integer, List<String>> stateTooltips = new HashMap<Integer, List<String>>();
    private int tooltipState = -1;

    public TooltipButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
    }

    public TooltipButton addTooltipKey(String key, Object... args) {
        GuiTooltipBridge.append(tooltip, key, args);
        return this;
    }

    public TooltipButton addStateTooltipKey(int state, String key, Object... args) {
        List<String> lines = stateTooltips.get(state);
        if (lines == null) {
            lines = new ArrayList<String>();
            stateTooltips.put(state, lines);
        }
        GuiTooltipBridge.append(lines, key, args);
        return this;
    }

    public void setTooltipState(int state) { tooltipState = state; }

    @Override
    public List<String> getTooltip() {
        List<String> stateTooltip = stateTooltips.get(tooltipState);
        return stateTooltip == null || stateTooltip.isEmpty() ? tooltip : stateTooltip;
    }

    private static final class GuiTooltipBridge {
        private static void append(List<String> target, String key, Object... args) {
            me.desht.modularrouters.gui.GuiTooltip.append(target, key, args);
        }
    }
}
