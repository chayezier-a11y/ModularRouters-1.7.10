package me.desht.modularrouters.gui.widgets.button;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class TexturedCycleButton<T extends Enum<T>> extends TexturedButton {
    private static final net.minecraft.util.ResourceLocation WIDGETS = new net.minecraft.util.ResourceLocation(
            ModularRouters.modId, "textures/gui/widgets.png");
    private final int[] textureX;
    private final int textureY;
    private T state;
    private final Map<Integer, List<String>> stateTooltips = new HashMap<Integer, List<String>>();

    public TexturedCycleButton(int id, int x, int y, int width, int height,
                               T initial, int textureY, int[] textureX) {
        super(id, x, y, width, height, WIDGETS, textureX[initial.ordinal()], textureY);
        this.state = initial;
        this.textureY = textureY;
        this.textureX = textureX;
    }

    public T getState() {
        return state;
    }

    public void setState(T state) {
        this.state = state;
    }

    public T cycle(boolean forward) {
        T[] values = state.getDeclaringClass().getEnumConstants();
        int next = state.ordinal() + (forward ? 1 : -1);
        if (next >= values.length) next = 0;
        if (next < 0) next = values.length - 1;
        state = values[next];
        return state;
    }

    public TexturedCycleButton<T> addStateTooltipKey(T value, String key, Object... args) {
        List<String> lines = stateTooltips.get(value.ordinal());
        if (lines == null) {
            lines = new java.util.ArrayList<String>();
            stateTooltips.put(value.ordinal(), lines);
        }
        me.desht.modularrouters.gui.GuiTooltip.append(lines, key, args);
        return this;
    }

    @Override
    public List<String> getTooltip() {
        List<String> result = stateTooltips.get(state.ordinal());
        return result == null || result.isEmpty() ? super.getTooltip() : result;
    }

    @Override
    protected int getTextureX() { return textureX[state.ordinal()]; }

    @Override
    protected int getTextureY() { return textureY; }
}
