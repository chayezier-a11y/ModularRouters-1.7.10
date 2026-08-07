package me.desht.modularrouters.gui.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.item.module.FlingerModule;
import me.desht.modularrouters.logic.compiled.CompiledFlingerModule;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GuiModuleFlinger extends GuiModule {
    private GuiTextField speedField;
    private GuiTextField pitchField;
    private GuiTextField yawField;

    public GuiModuleFlinger(ContainerModule container, EntityPlayer player, ItemStack moduleStack,
                            me.desht.modularrouters.block.tile.TileEntityItemRouter router) {
        super(container, player, moduleStack, router);
    }

    @Override
    public void initGui() {
        super.initGui();
        CompiledFlingerModule initial = new CompiledFlingerModule(null, moduleStack);
        speedField = makeField(guiLeft + 152, guiTop + 19, initial.getSpeed());
        pitchField = makeField(guiLeft + 152, guiTop + 37, initial.getPitch());
        yawField = makeField(guiLeft + 152, guiTop + 55, initial.getYaw());

        addInfoButton(430, 130, 15, 48, "guiText.tooltip.flinger.speed",
                FlingerModule.MIN_SPEED, FlingerModule.MAX_SPEED);
        addInfoButton(431, 130, 33, 64, "guiText.tooltip.flinger.pitch",
                FlingerModule.MIN_PITCH, FlingerModule.MAX_PITCH);
        addInfoButton(432, 130, 51, 80, "guiText.tooltip.flinger.yaw",
                FlingerModule.MIN_YAW, FlingerModule.MAX_YAW);
        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 13, guiLeft + 186, guiTop + 32,
                "guiText.popup.flinger.speed");
        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 31, guiLeft + 186, guiTop + 50,
                "guiText.popup.flinger.pitch");
        getMouseOverHelp().addHelpRegion(guiLeft + 128, guiTop + 49, guiLeft + 186, guiTop + 68,
                "guiText.popup.flinger.yaw");
    }

    private GuiTextField makeField(int x, int y, float value) {
        GuiTextField field = new GuiTextField(fontRendererObj, x, y, 35, 12);
        field.setMaxStringLength(8);
        field.setEnableBackgroundDrawing(false);
        field.setText(Float.toString(value));
        return field;
    }

    private void addInfoButton(int id, int x, int y, int textureX, String tooltipKey, float min, float max) {
        TexturedButton button = new TexturedButton(id, guiLeft + x, guiTop + y, 16, 16,
                new ResourceLocation(ModularRouters.modId, "textures/gui/widgets.png"), textureX, 0) {
            @Override
            protected boolean drawStandardBackground() { return false; }
        };
        button.addTooltipKey(tooltipKey, min, max);
        button.addTooltipKey("guiText.tooltip.numberFieldTooltip");
        buttonList.add(button);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        drawTexturedModalRect(guiLeft + 148, guiTop + 16, 0, 212, 35, 14);
        drawTexturedModalRect(guiLeft + 148, guiTop + 34, 0, 212, 35, 14);
        drawTexturedModalRect(guiLeft + 148, guiTop + 52, 0, 212, 35, 14);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
    }

    @Override
    protected NBTTagCompound buildSettings() {
        NBTTagCompound tag = super.buildSettings();
        tag.setFloat(CompiledFlingerModule.NBT_SPEED, clamp(readFloat(speedField), FlingerModule.MIN_SPEED, FlingerModule.MAX_SPEED));
        tag.setFloat(CompiledFlingerModule.NBT_PITCH, clamp(readFloat(pitchField), FlingerModule.MIN_PITCH, FlingerModule.MAX_PITCH));
        tag.setFloat(CompiledFlingerModule.NBT_YAW, clamp(readFloat(yawField), FlingerModule.MIN_YAW, FlingerModule.MAX_YAW));
        return tag;
    }

    private float readFloat(GuiTextField field) {
        try { return Float.parseFloat(field.getText()); }
        catch (NumberFormatException e) { return 0.0f; }
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (speedField != null) speedField.updateCursorCounter();
        if (pitchField != null) pitchField.updateCursorCounter();
        if (yawField != null) yawField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (speedField != null) speedField.drawTextBox();
        if (pitchField != null) pitchField.drawTextBox();
        if (yawField != null) yawField.drawTextBox();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (speedField != null) speedField.mouseClicked(mouseX, mouseY, button);
        if (pitchField != null) pitchField.mouseClicked(mouseX, mouseY, button);
        if (yawField != null) yawField.mouseClicked(mouseX, mouseY, button);
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (speedField != null && speedField.textboxKeyTyped(c, keyCode)) { sendToServer(); return; }
        if (pitchField != null && pitchField.textboxKeyTyped(c, keyCode)) { sendToServer(); return; }
        if (yawField != null && yawField.textboxKeyTyped(c, keyCode)) { sendToServer(); return; }
        super.keyTyped(c, keyCode);
    }
}
