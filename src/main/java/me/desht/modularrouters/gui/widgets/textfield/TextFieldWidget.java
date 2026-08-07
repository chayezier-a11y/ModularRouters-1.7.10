package me.desht.modularrouters.gui.widgets.textfield;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

@SideOnly(Side.CLIENT)
public class TextFieldWidget extends GuiTextField {
    private final Runnable callback;

    public TextFieldWidget(FontRenderer fontRenderer, int x, int y, int w, int h, Runnable callback) {
        super(fontRenderer, x, y, w, h);
        this.callback = callback;
        setMaxStringLength(50);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        if (callback != null) callback.run();
    }
}
