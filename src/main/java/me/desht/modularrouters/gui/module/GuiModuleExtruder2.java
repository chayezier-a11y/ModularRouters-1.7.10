package me.desht.modularrouters.gui.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.widgets.button.TexturedButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GuiModuleExtruder2 extends GuiModule {
    private static final ResourceLocation MODULE_TEXTURE = new ResourceLocation(
            ModularRouters.modId, "textures/gui/module.png");
    private static final int BTN_TEMPLATE_INFO = 510;

    public GuiModuleExtruder2(ContainerModule container, EntityPlayer player, ItemStack moduleStack,
                              me.desht.modularrouters.block.tile.TileEntityItemRouter router) {
        super(container, player, moduleStack, router);
    }

    @Override
    public void initGui() {
        super.initGui();
        TexturedButton info = new TexturedButton(BTN_TEMPLATE_INFO, guiLeft + 173, guiTop + 70,
                16, 16, new ResourceLocation(ModularRouters.modId, "textures/gui/widgets.png"), 128, 0) {
            @Override
            protected boolean drawStandardBackground() { return false; }
        };
        info.addTooltipKey("guiText.tooltip.extruder2.template");
        buttonList.add(info);
        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 16, guiLeft + 181, guiTop + 69,
                "guiText.popup.extruder2.template");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        mc.getTextureManager().bindTexture(MODULE_TEXTURE);
        drawTexturedModalRect(guiLeft + 128, guiTop + 16, 202, 52, 54, 54);
    }
}
