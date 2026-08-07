package me.desht.modularrouters.gui.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.widgets.button.TexturedCycleButton;
import me.desht.modularrouters.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.logic.compiled.CompiledDistributorModule;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@SideOnly(Side.CLIENT)
public class GuiModuleDistributor extends GuiModule {
    private static final int BTN_STRATEGY = 420;
    private static final int BTN_PULLING = 421;
    private StrategyButton strategyButton;
    private PullingButton pullingButton;

    public GuiModuleDistributor(ContainerModule container, EntityPlayer player, ItemStack moduleStack,
                                me.desht.modularrouters.block.tile.TileEntityItemRouter router) {
        super(container, player, moduleStack, router);
    }

    @Override
    public void initGui() {
        super.initGui();
        CompiledDistributorModule initial = new CompiledDistributorModule(null, moduleStack);
        strategyButton = new StrategyButton(BTN_STRATEGY, guiLeft + 147, guiTop + 23,
                initial.getDistributionStrategy());
        pullingButton = new PullingButton(BTN_PULLING, guiLeft + 147, guiTop + 43, initial.isPulling());
        buttonList.add(strategyButton);
        buttonList.add(pullingButton);
        getMouseOverHelp().addHelpRegion(guiLeft + 125, guiTop + 21, guiLeft + 165, guiTop + 41,
                "guiText.popup.distributor.strategy");
        getMouseOverHelp().addHelpRegion(guiLeft + 125, guiTop + 41, guiLeft + 165, guiTop + 61,
                "guiText.popup.distributor.direction");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_STRATEGY) {
            strategyButton.cycle(!isShiftKeyDown());
            sendToServer();
        } else if (button.id == BTN_PULLING) {
            pullingButton.toggle();
            sendToServer();
        } else {
            super.actionPerformed(button);
        }
    }

    @Override
    protected NBTTagCompound buildSettings() {
        NBTTagCompound tag = super.buildSettings();
        tag.setInteger(CompiledDistributorModule.NBT_STRATEGY, strategyButton.getState().ordinal());
        tag.setBoolean(CompiledDistributorModule.NBT_PULLING, pullingButton.isToggled());
        return tag;
    }

    private static class StrategyButton extends TexturedCycleButton<CompiledDistributorModule.DistributionStrategy> {
        StrategyButton(int id, int x, int y, CompiledDistributorModule.DistributionStrategy initial) {
            super(id, x, y, 16, 16, initial, 32, new int[] {160, 176, 192, 208});
            for (CompiledDistributorModule.DistributionStrategy strategy
                    : CompiledDistributorModule.DistributionStrategy.values()) {
                addStateTooltipKey(strategy, "guiText.tooltip.distributor.strategy." + strategy.name());
            }
        }
    }

    private static class PullingButton extends TexturedToggleButton {
        PullingButton(int id, int x, int y, boolean initial) {
            super(id, x, y, 16, 16, 176, 16, initial);
            addTooltipKey("guiText.tooltip.distributor.direction.OUT");
            addToggledTooltipKey("guiText.tooltip.distributor.direction.IN");
        }

        @Override
        protected int getTextureX() {
            return isToggled() ? 160 : 176;
        }
    }
}
