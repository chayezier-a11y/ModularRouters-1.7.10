package me.desht.modularrouters.gui.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.MouseOverHelp;
import me.desht.modularrouters.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.gui.widgets.button.TooltipButton;
import me.desht.modularrouters.logic.compiled.CompiledVacuumModule;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@SideOnly(Side.CLIENT)
public class GuiModuleVacuum extends GuiModule {
    private static final int BTN_XP_TYPE = 530;
    private static final int BTN_EJECT = 531;
    private TooltipButton xpTypeButton;
    private TexturedToggleButton ejectButton;
    private boolean xpMode;

    public GuiModuleVacuum(ContainerModule container, EntityPlayer player, ItemStack moduleStack,
                           me.desht.modularrouters.block.tile.TileEntityItemRouter router) {
        super(container, player, moduleStack, router);
    }

    @Override
    public void initGui() {
        super.initGui();
        CompiledVacuumModule initial = new CompiledVacuumModule(null, moduleStack);
        xpMode = initial.isXpMode();
        xpTypeButton = new TooltipButton(BTN_XP_TYPE, guiLeft + 170, guiTop + 28, 16, 16, "B");
        xpTypeButton.addTooltipKey("guiText.tooltip.vacuum.xpBottle");
        ejectButton = new TexturedToggleButton(BTN_EJECT, guiLeft + 167, guiTop + 48,
                16, 16, 112, 16, initial.isAutoEjecting());
        ejectButton.addTooltipKey("guiText.tooltip.vacuum.eject.false");
        ejectButton.addToggledTooltipKey("guiText.tooltip.vacuum.eject.true");
        buttonList.add(xpTypeButton);
        buttonList.add(ejectButton);
        updateSpecialVisibility();
        getMouseOverHelp().addHelpRegion(guiLeft + 125, guiTop + 24, guiLeft + 187, guiTop + 45,
                "guiText.popup.vacuum.xp", new MouseHelpVisibility());
        getMouseOverHelp().addHelpRegion(guiLeft + 125, guiTop + 46, guiLeft + 187, guiTop + 65,
                "guiText.popup.vacuum.eject", new MouseHelpVisibility());
    }

    private void updateSpecialVisibility() {
        boolean visible = xpMode && new me.desht.modularrouters.item.augment.ItemAugment.AugmentCounter(moduleStack)
                .getAugmentCount(me.desht.modularrouters.item.ModItems.xpVacuumAugment) > 0;
        xpTypeButton.visible = visible;
        // The local 1.7.10 port has only the solid experience-bottle collector;
        // the original fluid-only eject control is hidden for solid types.
        ejectButton.visible = false;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_EJECT) {
            ejectButton.toggle();
            sendToServer();
        } else {
            super.actionPerformed(button);
        }
    }

    @Override
    protected NBTTagCompound buildSettings() {
        NBTTagCompound tag = super.buildSettings();
        tag.setByte(CompiledVacuumModule.NBT_XP_COLLECTION_TYPE, (byte) 0);
        tag.setBoolean(CompiledVacuumModule.NBT_AUTO_EJECT, ejectButton.isToggled());
        return tag;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        updateSpecialVisibility();
    }

    private class MouseHelpVisibility implements MouseOverHelp.Visibility {
        @Override
        public boolean isVisible() { return xpTypeButton != null && xpTypeButton.visible; }
    }
}
