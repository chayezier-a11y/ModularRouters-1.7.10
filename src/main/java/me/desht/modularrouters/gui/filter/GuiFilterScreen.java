package me.desht.modularrouters.gui.filter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GuiFilterScreen extends GuiContainer {
    private static final ResourceLocation texture = new ResourceLocation(ModularRouters.modId, "textures/gui/module.png");

    public GuiFilterScreen(Container container) {
        super(container);
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(texture);
        int k = (width - xSize) / 2;
        int l = (height - ySize) / 2;
        drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
    }
}
