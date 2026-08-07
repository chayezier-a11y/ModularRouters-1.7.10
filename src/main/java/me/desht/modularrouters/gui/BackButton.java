package me.desht.modularrouters.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.gui.widgets.button.TexturedButton;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class BackButton extends TexturedButton {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ModularRouters.modId, "textures/gui/widgets.png");

    public BackButton(int id, int x, int y) {
        super(id, x, y, 16, 16, TEXTURE, 96, 0);
    }

    @Override
    protected boolean drawStandardBackground() {
        return false;
    }
}
