package me.desht.modularrouters.gui.widgets.button;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class TexturedToggleButton extends TexturedButton {
    private static final net.minecraft.util.ResourceLocation WIDGETS = new net.minecraft.util.ResourceLocation(
            ModularRouters.modId, "textures/gui/widgets.png");
    private final int textureX;
    private final int textureY;
    private boolean toggled;
    private final List<String> toggledTooltip = new ArrayList<String>();

    public TexturedToggleButton(int id, int x, int y, int width, int height,
                                int textureX, int textureY, boolean toggled) {
        super(id, x, y, width, height, WIDGETS, textureX, textureY);
        this.textureX = textureX;
        this.textureY = textureY;
        this.toggled = toggled;
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public void toggle() {
        setToggled(!isToggled());
    }

    public TexturedToggleButton addToggledTooltip(String... lines) {
        for (String line : lines) toggledTooltip.add(line);
        return this;
    }

    public TexturedToggleButton addToggledTooltipKey(String key, Object... args) {
        me.desht.modularrouters.gui.GuiTooltip.append(toggledTooltip, key, args);
        return this;
    }

    @Override
    public List<String> getTooltip() {
        return toggled && !toggledTooltip.isEmpty() ? toggledTooltip : super.getTooltip();
    }

    @Override
    protected int getTextureX() { return textureX + (toggled ? 16 : 0); }

    @Override
    protected int getTextureY() { return textureY; }
}
