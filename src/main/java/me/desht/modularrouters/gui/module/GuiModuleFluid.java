package me.desht.modularrouters.gui.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.gui.widgets.button.TexturedCycleButton;
import me.desht.modularrouters.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.gui.widgets.button.TooltipButton;
import me.desht.modularrouters.item.module.FluidModule1;
import me.desht.modularrouters.logic.compiled.CompiledFluidModule1;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.entity.RenderItem;

@SideOnly(Side.CLIENT)
public class GuiModuleFluid extends GuiModule {
    private static final int BTN_DIRECTION = 440;
    private static final int BTN_FORCE_EMPTY = 441;
    private static final int BTN_REGULATION_TYPE = 442;
    private GuiTextField maxTransferField;
    private FluidDirectionButton directionButton;
    private ForceEmptyButton forceEmptyButton;
    private TooltipButton regulationTypeButton;
    private boolean regulateAbsolute;

    public GuiModuleFluid(ContainerModule container, EntityPlayer player, ItemStack moduleStack,
                          me.desht.modularrouters.block.tile.TileEntityItemRouter router) {
        super(container, player, moduleStack, router);
    }

    @Override
    public void initGui() {
        super.initGui();
        CompiledFluidModule1 initial = new CompiledFluidModule1(null, moduleStack);
        maxTransferField = new GuiTextField(fontRendererObj, guiLeft + 152, guiTop + 23, 34, 12);
        maxTransferField.setMaxStringLength(7);
        maxTransferField.setEnableBackgroundDrawing(false);
        maxTransferField.setText(Integer.toString(initial.getMaxTransfer()));
        regulateAbsolute = initial.isRegulateAbsolute();

        regulatorTextField.xPosition = guiLeft + 128;
        regulatorTextField.yPosition = guiTop + 90;
        regulatorTextField.width = 40;
        regulatorTextField.setMaxStringLength(9);
        for (Object object : buttonList) {
            GuiButton button = (GuiButton) object;
            if (button.id == 217) {
                button.xPosition = guiLeft + 110;
                button.yPosition = guiTop + 88;
            }
        }

        TexturedButton info = new TexturedButton(443, guiLeft + 130, guiTop + 19, 16, 16,
                new ResourceLocation(ModularRouters.modId, "textures/gui/widgets.png"), 0, 16);
        info.addTooltipKey("guiText.tooltip.fluidTransferTooltip");
        info.addTooltipKey("guiText.tooltip.numberFieldTooltip");
        buttonList.add(info);

        directionButton = new FluidDirectionButton(BTN_DIRECTION, guiLeft + 148, guiTop + 44,
                initial.getFluidDirection());
        forceEmptyButton = new ForceEmptyButton(BTN_FORCE_EMPTY, guiLeft + 168, guiTop + 69,
                initial.isForceEmpty());
        regulationTypeButton = new TooltipButton(BTN_REGULATION_TYPE, guiLeft + 170, guiTop + 89,
                18, 14, "");
        regulationTypeButton.addTooltipKey("guiText.tooltip.fluidRegulationType");
        buttonList.add(directionButton);
        buttonList.add(forceEmptyButton);
        buttonList.add(regulationTypeButton);
        updateRegulationLabel();

        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 17, guiLeft + 183, guiTop + 35,
                "guiText.popup.fluid.maxTransfer");
        getMouseOverHelp().addHelpRegion(guiLeft + 126, guiTop + 42, guiLeft + 185, guiTop + 61,
                "guiText.popup.fluid.direction");
        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 67, guiLeft + 185, guiTop + 86,
                "guiText.popup.fluid.forceEmpty");
    }

    private void updateRegulationLabel() {
        if (regulationTypeButton != null) regulationTypeButton.displayString = regulateAbsolute ? "mB" : "%";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        drawTexturedModalRect(guiLeft + 146, guiTop + 20, 0, 212, 35, 14);
        RenderItem renderer = RenderItem.getInstance();
        renderer.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(),
                new ItemStack(Items.bucket), guiLeft + 128, guiTop + 19);
        renderer.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(),
                new ItemStack(Items.water_bucket), guiLeft + 168, guiTop + 44);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_DIRECTION) {
            directionButton.cycle(!isShiftKeyDown());
            sendToServer();
        } else if (button.id == BTN_FORCE_EMPTY) {
            forceEmptyButton.toggle();
            sendToServer();
        } else if (button.id == BTN_REGULATION_TYPE) {
            regulateAbsolute = !regulateAbsolute;
            updateRegulationLabel();
            sendToServer();
        } else {
            super.actionPerformed(button);
        }
    }

    @Override
    protected NBTTagCompound buildSettings() {
        NBTTagCompound tag = super.buildSettings();
        tag.setInteger(CompiledFluidModule1.NBT_MAX_TRANSFER, maxTransferValue());
        tag.setByte(CompiledFluidModule1.NBT_FLUID_DIRECTION, (byte) directionButton.getState().ordinal());
        tag.setBoolean(CompiledFluidModule1.NBT_FORCE_EMPTY, forceEmptyButton.isToggled());
        tag.setBoolean(CompiledFluidModule1.NBT_REGULATE_ABSOLUTE, regulateAbsolute);
        return tag;
    }

    private int maxTransferValue() {
        try { return Math.max(0, Integer.parseInt(maxTransferField.getText())); }
        catch (NumberFormatException e) { return 0; }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (maxTransferField != null) maxTransferField.updateCursorCounter();
        if (regulationTypeButton != null) regulationTypeButton.visible = regulatorTextField.getVisible();
        if (forceEmptyButton != null) forceEmptyButton.visible = directionButton.getState() == FluidModule1.FluidDirection.OUT;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (maxTransferField != null) maxTransferField.drawTextBox();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (maxTransferField != null) maxTransferField.mouseClicked(mouseX, mouseY, button);
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (maxTransferField != null && maxTransferField.textboxKeyTyped(c, keyCode)) {
            sendToServer();
            return;
        }
        super.keyTyped(c, keyCode);
    }

    private static class FluidDirectionButton extends TexturedCycleButton<FluidModule1.FluidDirection> {
        FluidDirectionButton(int id, int x, int y, FluidModule1.FluidDirection initial) {
            super(id, x, y, 16, 16, initial, 16, new int[] {160, 176});
            addStateTooltipKey(FluidModule1.FluidDirection.IN, "guiText.tooltip.fluid.direction.IN");
            addStateTooltipKey(FluidModule1.FluidDirection.OUT, "guiText.tooltip.fluid.direction.OUT");
        }
    }

    private static class ForceEmptyButton extends TexturedToggleButton {
        ForceEmptyButton(int id, int x, int y, boolean initial) {
            super(id, x, y, 16, 16, 112, 16, initial);
            addTooltipKey("guiText.tooltip.fluidForceEmpty.false");
            addToggledTooltipKey("guiText.tooltip.fluidForceEmpty.true");
        }

        @Override
        protected int getTextureX() { return isToggled() ? 192 : 112; }
    }
}
