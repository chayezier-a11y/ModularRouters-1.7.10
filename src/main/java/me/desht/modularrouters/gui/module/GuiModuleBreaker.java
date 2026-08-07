package me.desht.modularrouters.gui.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.widgets.button.TooltipButton;
import me.desht.modularrouters.logic.compiled.CompiledBreakerModule;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GuiModuleBreaker extends GuiModule {
    private static final int BTN_MATCH_TYPE = 540;
    private MatchTypeButton matchTypeButton;

    public GuiModuleBreaker(ContainerModule container, EntityPlayer player, ItemStack moduleStack,
                            me.desht.modularrouters.block.tile.TileEntityItemRouter router) {
        super(container, player, moduleStack, router);
    }

    @Override
    public void initGui() {
        super.initGui();
        CompiledBreakerModule initial = new CompiledBreakerModule(null, moduleStack);
        matchTypeButton = new MatchTypeButton(BTN_MATCH_TYPE, guiLeft + 147, guiTop + 20,
                initial.getMatchType());
        buttonList.add(matchTypeButton);
        getMouseOverHelp().addHelpRegion(guiLeft + 146, guiTop + 19, guiLeft + 165, guiTop + 38,
                "guiText.popup.breaker.matchType");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_MATCH_TYPE) {
            matchTypeButton.cycle(!isShiftKeyDown());
            sendToServer();
        } else {
            super.actionPerformed(button);
        }
    }

    @Override
    protected NBTTagCompound buildSettings() {
        NBTTagCompound tag = super.buildSettings();
        tag.setByte(CompiledBreakerModule.NBT_MATCH_TYPE, (byte) matchTypeButton.getState().ordinal());
        return tag;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        mc.getTextureManager().bindTexture(new ResourceLocation(ModularRouters.modId, "textures/gui/module.png"));
        drawTexturedModalRect(guiLeft + 147, guiTop + 20, 0, 226, 18, 18);
    }

    private static class MatchTypeButton extends TooltipButton {
        private CompiledBreakerModule.MatchType state;

        MatchTypeButton(int id, int x, int y, CompiledBreakerModule.MatchType initial) {
            super(id, x, y, 20, 20, label(initial));
            state = initial;
            for (CompiledBreakerModule.MatchType type : CompiledBreakerModule.MatchType.values()) {
                addStateTooltipKey(type.ordinal(), "guiText.tooltip.breaker.matchType." + type.name());
            }
            setTooltipState(state.ordinal());
        }

        CompiledBreakerModule.MatchType getState() { return state; }

        void cycle(boolean forward) {
            CompiledBreakerModule.MatchType[] values = CompiledBreakerModule.MatchType.values();
            int next = state.ordinal() + (forward ? 1 : -1);
            if (next >= values.length) next = 0;
            if (next < 0) next = values.length - 1;
            state = values[next];
            displayString = label(state);
            setTooltipState(state.ordinal());
        }

        private static String label(CompiledBreakerModule.MatchType type) {
            return type == CompiledBreakerModule.MatchType.BLOCK ? "B" : "I";
        }
    }
}
