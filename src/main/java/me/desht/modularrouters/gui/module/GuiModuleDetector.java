package me.desht.modularrouters.gui.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.gui.widgets.button.TooltipButton;
import me.desht.modularrouters.logic.compiled.CompiledDetectorModule;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GuiModuleDetector extends GuiModule {
    private static final int BTN_STRONG = 410;
    private static final int BTN_REDSTONE_INFO = 411;
    private GuiTextField signalField;
    private TooltipButton strongButton;
    private boolean strong;

    public GuiModuleDetector(ContainerModule container, EntityPlayer player, ItemStack moduleStack,
                             me.desht.modularrouters.block.tile.TileEntityItemRouter router) {
        super(container, player, moduleStack, router);
    }

    @Override
    public void initGui() {
        super.initGui();
        CompiledDetectorModule initial = new CompiledDetectorModule(null, moduleStack);
        signalField = new GuiTextField(fontRendererObj, guiLeft + 152, guiTop + 19, 20, 12);
        signalField.setMaxStringLength(2);
        signalField.setEnableBackgroundDrawing(false);
        signalField.setText(Integer.toString(initial.getSignalLevel()));
        strong = initial.isStrongSignal();

        strongButton = new TooltipButton(BTN_STRONG, guiLeft + 138, guiTop + 33, 40, 20,
                strongLabel());
        strongButton.addTooltipKey("guiText.tooltip.detector.strong");
        buttonList.add(strongButton);

        TexturedButton info = new TexturedButton(BTN_REDSTONE_INFO, guiLeft + 132, guiTop + 15,
                16, 16, new ResourceLocation(ModularRouters.modId, "textures/gui/widgets.png"), 0, 16);
        info.addTooltipKey("guiText.tooltip.detectorTooltip");
        info.addTooltipKey("guiText.tooltip.numberFieldTooltip");
        buttonList.add(info);

        getMouseOverHelp().addHelpRegion(guiLeft + 129, guiTop + 14, guiLeft + 172, guiTop + 31,
                "guiText.popup.detector.signalLevel");
        getMouseOverHelp().addHelpRegion(guiLeft + 135, guiTop + 31, guiLeft + 180, guiTop + 54,
                "guiText.popup.detector.weakStrong");
    }

    private String strongLabel() {
        return I18n.format("guiText.label.strongSignal." + strong);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_STRONG) {
            strong = !strong;
            strongButton.displayString = strongLabel();
            sendToServer();
        } else {
            super.actionPerformed(button);
        }
    }

    @Override
    protected NBTTagCompound buildSettings() {
        NBTTagCompound tag = super.buildSettings();
        tag.setInteger(CompiledDetectorModule.NBT_SIGNAL_LEVEL, signalValue());
        tag.setBoolean(CompiledDetectorModule.NBT_STRONG_SIGNAL, strong);
        return tag;
    }

    private int signalValue() {
        try {
            return Math.max(0, Math.min(15, Integer.parseInt(signalField.getText())));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (signalField != null) signalField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (signalField != null) signalField.drawTextBox();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (signalField != null) signalField.mouseClicked(mouseX, mouseY, button);
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (signalField != null && signalField.textboxKeyTyped(c, keyCode)) {
            sendToServer();
            return;
        }
        super.keyTyped(c, keyCode);
    }
}
