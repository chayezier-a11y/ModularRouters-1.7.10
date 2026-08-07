package me.desht.modularrouters.gui.widgets.button;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

@SideOnly(Side.CLIENT)
public class ToggleButton extends GuiButton {
    private boolean toggled;
    private final String textOn, textOff;

    public ToggleButton(int id, int x, int y, int w, int h, String textOn, String textOff, boolean initial) {
        super(id, x, y, w, h, "");
        this.textOn = textOn;
        this.textOff = textOff;
        this.toggled = initial;
        updateDisplay();
    }

    public boolean isToggled() { return toggled; }
    public void setToggled(boolean t) { toggled = t; updateDisplay(); }

    public void toggle() { toggled = !toggled; updateDisplay(); }

    private void updateDisplay() {
        displayString = toggled ? textOn : textOff;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            toggle();
            return true;
        }
        return false;
    }
}
