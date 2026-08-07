package me.desht.modularrouters.gui.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.widgets.button.TexturedCycleButton;
import me.desht.modularrouters.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.logic.compiled.CompiledActivatorModule;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@SideOnly(Side.CLIENT)
public class GuiModuleActivator extends GuiModule {
    private static final int BTN_ACTION = 400;
    private static final int BTN_SNEAK = 401;
    private static final int BTN_LOOK = 402;
    private static final int BTN_ENTITY_MODE = 403;

    private ActionButton actionButton;
    private LookButton lookButton;
    private EntityModeButton entityModeButton;
    private SneakButton sneakButton;

    public GuiModuleActivator(ContainerModule container, EntityPlayer player, ItemStack moduleStack,
                              me.desht.modularrouters.block.tile.TileEntityItemRouter router) {
        super(container, player, moduleStack, router);
    }

    @Override
    public void initGui() {
        super.initGui();
        CompiledActivatorModule initial = new CompiledActivatorModule(null, moduleStack);
        actionButton = new ActionButton(BTN_ACTION, guiLeft + 167, guiTop + 20, initial.getActionType());
        sneakButton = new SneakButton(BTN_SNEAK, guiLeft + 167, guiTop + 40, initial.isSneaking());
        lookButton = new LookButton(BTN_LOOK, guiLeft + 167, guiTop + 60, initial.getLookDirection());
        entityModeButton = new EntityModeButton(BTN_ENTITY_MODE, guiLeft + 167, guiTop + 60,
                initial.getEntityMode());
        buttonList.add(actionButton);
        buttonList.add(sneakButton);
        buttonList.add(lookButton);
        buttonList.add(entityModeButton);
        updateActionVisibility();

        getMouseOverHelp().addHelpRegion(guiLeft + 130, guiTop + 18, guiLeft + 183, guiTop + 37,
                "guiText.popup.activator.action");
        getMouseOverHelp().addHelpRegion(guiLeft + 130, guiTop + 39, guiLeft + 183, guiTop + 56,
                "guiText.popup.activator.sneak");
        getMouseOverHelp().addHelpRegion(guiLeft + 130, guiTop + 59, guiLeft + 183, guiTop + 76,
                "guiText.popup.activator.look");
    }

    private void updateActionVisibility() {
        boolean entity = actionButton != null && actionButton.getState() != CompiledActivatorModule.ActionType.RIGHT_CLICK;
        if (lookButton != null) lookButton.visible = !entity;
        if (entityModeButton != null) entityModeButton.visible = entity;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_ACTION) {
            actionButton.cycle(!isShiftKeyDown());
            updateActionVisibility();
            sendToServer();
        } else if (button.id == BTN_SNEAK) {
            sneakButton.toggle();
            sendToServer();
        } else if (button.id == BTN_LOOK) {
            lookButton.cycle(!isShiftKeyDown());
            sendToServer();
        } else if (button.id == BTN_ENTITY_MODE) {
            entityModeButton.cycle(!isShiftKeyDown());
            sendToServer();
        } else {
            super.actionPerformed(button);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        fontRendererObj.drawString(I18n.format("guiText.tooltip.activator.action"), 132, 23, 0x404040);
        fontRendererObj.drawString(I18n.format("guiText.tooltip.activator.sneak"), 132, 43, 0x404040);
        fontRendererObj.drawString(I18n.format(actionButton != null && actionButton.getState()
                != CompiledActivatorModule.ActionType.RIGHT_CLICK
                ? "guiText.tooltip.activator.entityMode" : "guiText.tooltip.activator.lookDirection"),
                132, 63, 0x404040);
    }

    @Override
    protected NBTTagCompound buildSettings() {
        NBTTagCompound tag = super.buildSettings();
        tag.setByte(CompiledActivatorModule.NBT_ACTION_TYPE, (byte) actionButton.getState().ordinal());
        tag.setByte(CompiledActivatorModule.NBT_LOOK_DIRECTION, (byte) lookButton.getState().ordinal());
        tag.setByte(CompiledActivatorModule.NBT_ENTITY_MODE, (byte) entityModeButton.getState().ordinal());
        tag.setBoolean(CompiledActivatorModule.NBT_SNEAKING, sneakButton.isToggled());
        return tag;
    }

    private static class ActionButton extends TexturedCycleButton<CompiledActivatorModule.ActionType> {
        ActionButton(int id, int x, int y, CompiledActivatorModule.ActionType initial) {
            super(id, x, y, 16, 16, initial, 0, new int[] {48, 64, 80});
            addStateTooltipKey(CompiledActivatorModule.ActionType.RIGHT_CLICK,
                    "guiText.tooltip.activator.action.RIGHT_CLICK");
            addStateTooltipKey(CompiledActivatorModule.ActionType.USE_ITEM_ON_ENTITY,
                    "guiText.tooltip.activator.action.USE_ITEM_ON_ENTITY");
            addStateTooltipKey(CompiledActivatorModule.ActionType.ATTACK_ENTITY,
                    "guiText.tooltip.activator.action.ATTACK_ENTITY");
        }
    }

    private static class LookButton extends TexturedCycleButton<CompiledActivatorModule.LookDirection> {
        LookButton(int id, int x, int y, CompiledActivatorModule.LookDirection initial) {
            super(id, x, y, 16, 16, initial, 0, new int[] {144, 160, 176});
            for (CompiledActivatorModule.LookDirection direction : CompiledActivatorModule.LookDirection.values()) {
                addStateTooltipKey(direction, "guiText.tooltip.activator.look." + direction.name());
            }
        }
    }

    private static class EntityModeButton extends TexturedCycleButton<CompiledActivatorModule.EntityMode> {
        EntityModeButton(int id, int x, int y, CompiledActivatorModule.EntityMode initial) {
            super(id, x, y, 16, 16, initial, 16, new int[] {192, 176, 160});
            for (CompiledActivatorModule.EntityMode mode : CompiledActivatorModule.EntityMode.values()) {
                addStateTooltipKey(mode, "guiText.tooltip.activator.entityMode." + mode.name());
            }
        }

        @Override
        protected int getTextureY() {
            return getState() == CompiledActivatorModule.EntityMode.NEAREST ? 16 : 32;
        }
    }

    private static class SneakButton extends TexturedToggleButton {
        SneakButton(int id, int x, int y, boolean initial) {
            super(id, x, y, 16, 16, 112, 16, initial);
            addTooltipKey("guiText.tooltip.activator.sneak.false");
            addToggledTooltipKey("guiText.tooltip.activator.sneak.true");
        }

        @Override
        protected int getTextureX() {
            return isToggled() ? 192 : 112;
        }
    }
}
