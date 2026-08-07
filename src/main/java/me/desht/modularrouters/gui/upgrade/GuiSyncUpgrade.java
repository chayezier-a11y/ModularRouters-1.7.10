package me.desht.modularrouters.gui.upgrade;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.ContainerModule;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GuiSyncUpgrade extends GuiContainer {
    private final EntityPlayer player;
    private final ItemStack upgradeStack;

    private static final ResourceLocation texture = new ResourceLocation(ModularRouters.modId, "textures/gui/syncUpgrade.png");

    public GuiSyncUpgrade(ContainerModule container, EntityPlayer player, ItemStack stack) {
        super(container);
        this.player = player;
        this.upgradeStack = stack;
        this.xSize = 176;
        this.ySize = 100;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(texture);
        int k = (width - xSize) / 2;
        int l = (height - ySize) / 2;
        drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString("Sync Upgrade Tuning", 8, 10, 0x404040);
    }
}
