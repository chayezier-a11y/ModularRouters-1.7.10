package me.desht.modularrouters.gui.widgets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

@SideOnly(Side.CLIENT)
public abstract class GuiContainerBase extends GuiContainer {

    public GuiContainerBase(Container container) {
        super(container);
    }

    public GuiContainerBase(Container container, int xSize, int ySize) {
        super(container);
        this.xSize = xSize;
        this.ySize = ySize;
    }

    protected int getGuiLeft() {
        return (width - xSize) / 2;
    }

    protected int getGuiTop() {
        return (height - ySize) / 2;
    }
}
