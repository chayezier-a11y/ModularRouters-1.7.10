package me.desht.modularrouters.gui.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.BackButton;
import me.desht.modularrouters.gui.GuiTooltip;
import me.desht.modularrouters.gui.MouseOverHelp;
import me.desht.modularrouters.gui.RedstoneBehaviourButton;
import me.desht.modularrouters.gui.widgets.button.TexturedButton;
import me.desht.modularrouters.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.item.ModItems;
import me.desht.modularrouters.item.augment.ItemAugment;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.Module.ModuleFlags;
import me.desht.modularrouters.item.module.Module.RelativeDirection;
import me.desht.modularrouters.item.module.TargetedModule;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.network.ModuleSettingsMessage;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class GuiModule extends GuiContainer {
    protected final EntityPlayer player;
    protected final ItemStack moduleStack;
    protected final TileEntityItemRouter router;

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ModularRouters.modId, "textures/gui/module.png");
    private static final int GUI_WIDTH = 192;
    private static final int GUI_HEIGHT = 198;
    private static final int BUTTON_SIZE = 16;

    private static final int BTN_DIR_NONE = 200;
    private static final int BTN_DIR_UP = 201;
    private static final int BTN_DIR_LEFT = 202;
    private static final int BTN_DIR_FRONT = 203;
    private static final int BTN_DIR_RIGHT = 204;
    private static final int BTN_DIR_DOWN = 205;
    private static final int BTN_DIR_BACK = 206;
    private static final int BTN_BLACKLIST = 210;
    private static final int BTN_IGNORE_META = 211;
    private static final int BTN_IGNORE_NBT = 212;
    private static final int BTN_IGNORE_TAGS = 213;
    private static final int BTN_MATCH_ALL = 214;
    private static final int BTN_TERMINATION = 215;
    private static final int BTN_REDSTONE = 216;
    private static final int BTN_REGULATOR_HELP = 217;
    private static final int BTN_MOUSE_OVER_HELP = 218;
    private static final int BTN_BACK = 219;

    private static final int[] DIRECTION_IDS = {
            BTN_DIR_NONE, BTN_DIR_UP, BTN_DIR_LEFT, BTN_DIR_FRONT,
            BTN_DIR_RIGHT, BTN_DIR_DOWN, BTN_DIR_BACK
    };
    private static final RelativeDirection[] DIRECTIONS = {
            RelativeDirection.NONE, RelativeDirection.UP, RelativeDirection.LEFT,
            RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.DOWN,
            RelativeDirection.BACK
    };
    private static final int[] TERMINATION_TEXTURES = {128, 144, 224};

    private final Map<Integer, TexturedToggleButton> toggleButtons =
            new HashMap<Integer, TexturedToggleButton>();
    private me.desht.modularrouters.gui.widgets.button.TexturedCycleButton<ModuleHelper.Termination> terminationButton;
    private RedstoneBehaviourButton redstoneButton;
    protected GuiTextField regulatorTextField;
    private TexturedToggleButton mouseOverHelpButton;
    private final MouseOverHelp mouseOverHelp = new MouseOverHelp();
    private int sendDelay;

    public GuiModule(ContainerModule container, EntityPlayer player, ItemStack moduleStack,
                     TileEntityItemRouter router) {
        super(container);
        this.player = player;
        this.moduleStack = moduleStack;
        this.router = router;
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
        container.setOnFilterChanged(new Runnable() {
            @Override
            public void run() {
                sendToServer();
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        Module module = ItemModule.getModule(moduleStack);
        if (module == null) return;

        addToggleButton(BTN_BLACKLIST, ModuleFlags.BLACKLIST, 0, 32, 7, 75);
        addToggleButton(BTN_IGNORE_META, ModuleFlags.IGNORE_META, 32, 32, 7, 93);
        addToggleButton(BTN_IGNORE_NBT, ModuleFlags.IGNORE_NBT, 64, 32, 25, 75);
        addToggleButton(BTN_IGNORE_TAGS, ModuleFlags.IGNORE_TAGS, 96, 32, 25, 93);
        addToggleButton(BTN_MATCH_ALL, ModuleFlags.MATCH_ALL, 208, 16, 45, 75);

        terminationButton = new me.desht.modularrouters.gui.widgets.button.TexturedCycleButton<ModuleHelper.Termination>(
                BTN_TERMINATION, guiLeft + 45, guiTop + 93, BUTTON_SIZE, BUTTON_SIZE,
                ModuleHelper.getTermination(moduleStack), 32, TERMINATION_TEXTURES);
        for (ModuleHelper.Termination termination : ModuleHelper.Termination.values()) {
            terminationButton.addStateTooltipKey(termination,
                    "guiText.tooltip.terminate." + termination.name() + ".header");
            terminationButton.addStateTooltipKey(termination,
                    "guiText.tooltip.terminate." + termination.name());
        }
        buttonList.add(terminationButton);

        if (module.isDirectional()) {
            addDirectionButton(BTN_DIR_NONE, RelativeDirection.NONE, 70, 18);
            addDirectionButton(BTN_DIR_UP, RelativeDirection.UP, 87, 18);
            addDirectionButton(BTN_DIR_LEFT, RelativeDirection.LEFT, 70, 35);
            addDirectionButton(BTN_DIR_FRONT, RelativeDirection.FRONT, 87, 35);
            addDirectionButton(BTN_DIR_RIGHT, RelativeDirection.RIGHT, 104, 35);
            addDirectionButton(BTN_DIR_DOWN, RelativeDirection.DOWN, 87, 52);
            addDirectionButton(BTN_DIR_BACK, RelativeDirection.BACK, 104, 52);
        }

        redstoneButton = new RedstoneBehaviourButton(
                BTN_REDSTONE, guiLeft + 170, guiTop + 93, BUTTON_SIZE, BUTTON_SIZE,
                ModuleHelper.getRedstoneBehaviour(moduleStack));
        buttonList.add(redstoneButton);

        regulatorTextField = new GuiTextField(fontRendererObj, guiLeft + 166, guiTop + 75, 20, 12);
        regulatorTextField.setMaxStringLength(2);
        regulatorTextField.setText(Integer.toString(ModuleHelper.getRegulatorAmount(moduleStack)));
        regulatorTextField.setEnableBackgroundDrawing(false);
        updateAugmentVisibility();

        TexturedButton regulatorHelp = new TexturedButton(BTN_REGULATOR_HELP, guiLeft + 150, guiTop + 73,
                BUTTON_SIZE, BUTTON_SIZE,
                new ResourceLocation(ModularRouters.modId, "textures/gui/widgets.png"), 112, 0);
        regulatorHelp.addTooltipKey(module.isFluidModule()
                ? "guiText.tooltip.fluidRegulatorTooltip" : "guiText.tooltip.regulatorTooltip");
        regulatorHelp.addTooltipKey("guiText.tooltip.numberFieldTooltip");
        buttonList.add(regulatorHelp);

        mouseOverHelpButton = new TexturedToggleButton(BTN_MOUSE_OVER_HELP, guiLeft + 175, guiTop + 1,
                BUTTON_SIZE, BUTTON_SIZE, 192, 0, false) {
            @Override
            protected boolean drawStandardBackground() { return false; }
        };
        mouseOverHelpButton.addTooltipKey("guiText.tooltip.mouseOverHelp.false");
        mouseOverHelpButton.addToggledTooltipKey("guiText.tooltip.mouseOverHelp.true");
        buttonList.add(mouseOverHelpButton);

        if (router != null) {
            buttonList.add(new BackButton(BTN_BACK, guiLeft + 2, guiTop + 1));
        }

        mouseOverHelp.addHelpRegion(guiLeft + 7, guiTop + 16, guiLeft + 60, guiTop + 69,
                "guiText.popup.filter");
        mouseOverHelp.addHelpRegion(guiLeft + 5, guiTop + 73, guiLeft + 62, guiTop + 110,
                "guiText.popup.filterControl");
        mouseOverHelp.addHelpRegion(guiLeft + 68, guiTop + 16, guiLeft + 121, guiTop + 69,
                module.isDirectional() ? "guiText.popup.direction" : "guiText.popup.noDirection");
        mouseOverHelp.addHelpRegion(guiLeft + 77, guiTop + 74, guiLeft + 112, guiTop + 109,
                "guiText.popup.augments");
    }

    private void addToggleButton(int id, ModuleFlags flag, int textureX, int textureY, int x, int y) {
        TexturedToggleButton button = new TexturedToggleButton(id, guiLeft + x, guiTop + y,
                BUTTON_SIZE, BUTTON_SIZE, textureX, textureY, ModuleHelper.checkFlag(moduleStack, flag));
        if (flag == ModuleFlags.MATCH_ALL) {
            button.addTooltipKey("guiText.tooltip.matchAll.false");
            button.addToggledTooltipKey("guiText.tooltip.matchAll.true");
        } else {
            String tooltipFlag = flag == ModuleFlags.IGNORE_META ? "IGNORE_DAMAGE" : flag.name();
            button.addTooltipKey("guiText.tooltip." + tooltipFlag + ".1");
            button.addToggledTooltipKey("guiText.tooltip." + tooltipFlag + ".2");
        }
        toggleButtons.put(id, button);
        buttonList.add(button);
    }

    private void addDirectionButton(int id, RelativeDirection direction, int x, int y) {
        RelativeDirection current = ModuleHelper.getDirectionFromNBT(moduleStack);
        TexturedToggleButton button = new TexturedToggleButton(id, guiLeft + x, guiTop + y,
                BUTTON_SIZE, BUTTON_SIZE, direction.ordinal() * 32, 48, direction == current);
        button.addTooltipKey("guiText.tooltip." + direction.name());
        button.addToggledTooltipKey("guiText.tooltip." + direction.name());
        buttonList.add(button);
    }

    private void updateAugmentVisibility() {
        if (regulatorTextField == null || redstoneButton == null) return;
        ItemAugment.AugmentCounter counter = new ItemAugment.AugmentCounter(moduleStack);
        boolean hasRedstone = counter.getAugmentCount(ModItems.redstoneAugment) > 0;
        boolean hasRegulator = counter.getAugmentCount(ModItems.regulatorAugment) > 0;
        redstoneButton.visible = hasRedstone;
        regulatorTextField.setVisible(hasRegulator);
        for (Object obj : buttonList) {
            GuiButton button = (GuiButton) obj;
            if (button.id == BTN_REGULATOR_HELP) button.visible = hasRegulator;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        for (int i = 0; i < DIRECTION_IDS.length; i++) {
            if (button.id == DIRECTION_IDS[i]) {
                Module module = ItemModule.getModule(moduleStack);
                if (module != null) module.setDirection(moduleStack, DIRECTIONS[i]);
                refreshDirectionButtons(DIRECTIONS[i]);
                sendToServer();
                return;
            }
        }

        if (button.id == BTN_BLACKLIST) {
            toggleFlag(ModuleFlags.BLACKLIST, BTN_BLACKLIST);
        } else if (button.id == BTN_IGNORE_META) {
            toggleFlag(ModuleFlags.IGNORE_META, BTN_IGNORE_META);
        } else if (button.id == BTN_IGNORE_NBT) {
            toggleFlag(ModuleFlags.IGNORE_NBT, BTN_IGNORE_NBT);
        } else if (button.id == BTN_IGNORE_TAGS) {
            toggleFlag(ModuleFlags.IGNORE_TAGS, BTN_IGNORE_TAGS);
        } else if (button.id == BTN_MATCH_ALL) {
            toggleFlag(ModuleFlags.MATCH_ALL, BTN_MATCH_ALL);
        } else if (button.id == BTN_TERMINATION) {
            terminationButton.cycle(!isShiftKeyDown());
            sendToServer();
        } else if (button.id == BTN_REDSTONE) {
            redstoneButton.cycle(!isShiftKeyDown());
            sendToServer();
        } else if (button.id == BTN_MOUSE_OVER_HELP) {
            mouseOverHelpButton.toggle();
        } else if (button.id == BTN_BACK && router != null) {
            player.openGui(ModularRouters.instance, ModularRouters.GUI_ROUTER,
                    player.worldObj, router.xCoord, router.yCoord, router.zCoord);
        }
    }

    private void toggleFlag(ModuleFlags flag, int buttonId) {
        TexturedToggleButton button = toggleButtons.get(buttonId);
        if (button != null) button.toggle();
        sendToServer();
    }

    private void refreshDirectionButtons(RelativeDirection selected) {
        for (int i = 0; i < DIRECTION_IDS.length; i++) {
            for (Object obj : buttonList) {
                GuiButton button = (GuiButton) obj;
                if (button.id == DIRECTION_IDS[i]) {
                    ((TexturedToggleButton) button).setToggled(DIRECTIONS[i] == selected);
                    break;
                }
            }
        }
    }

    protected NBTTagCompound buildSettings() {
        NBTTagCompound tag = ModuleHelper.validateNBT(moduleStack);
        byte flags = tag.getByte(ModuleHelper.NBT_FLAGS);
        for (ModuleFlags flag : new ModuleFlags[] {
                ModuleFlags.BLACKLIST, ModuleFlags.IGNORE_META, ModuleFlags.IGNORE_NBT,
                ModuleFlags.IGNORE_TAGS, ModuleFlags.MATCH_ALL }) {
            TexturedToggleButton button = toggleButtons.get(flag == ModuleFlags.BLACKLIST ? BTN_BLACKLIST :
                    flag == ModuleFlags.IGNORE_META ? BTN_IGNORE_META :
                    flag == ModuleFlags.IGNORE_NBT ? BTN_IGNORE_NBT :
                    flag == ModuleFlags.IGNORE_TAGS ? BTN_IGNORE_TAGS : BTN_MATCH_ALL);
            if (button != null && button.isToggled()) flags |= flag.getMask();
            else flags &= ~flag.getMask();
        }
        tag.setByte(ModuleHelper.NBT_FLAGS, flags);
        if (terminationButton != null) ModuleHelper.setTermination(moduleStack, terminationButton.getState());
        if (redstoneButton != null) tag.setByte("RedstoneBehaviour", (byte) redstoneButton.getState().ordinal());
        if (regulatorTextField != null && regulatorTextField.getVisible()) {
            try {
                Module module = ItemModule.getModule(moduleStack);
                int maximum = module != null && module.isFluidModule() ? Integer.MAX_VALUE : 64;
                tag.setInteger(ModuleHelper.NBT_REGULATOR_AMOUNT,
                        Math.max(0, Math.min(maximum, Integer.parseInt(regulatorTextField.getText()))));
            } catch (NumberFormatException e) {
                tag.setInteger(ModuleHelper.NBT_REGULATOR_AMOUNT, 0);
            }
        }
        return (NBTTagCompound) tag.copy();
    }

    protected void sendToServer() {
        NBTTagCompound data = buildSettings();
        if (router != null) {
            ModularRouters.network.sendToServer(new ModuleSettingsMessage(router,
                    router.getModuleConfigSlot(player), data));
        } else {
            ModularRouters.network.sendToServer(new ModuleSettingsMessage(data));
        }
    }

    protected MouseOverHelp getMouseOverHelp() {
        return mouseOverHelp;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        updateAugmentVisibility();
        if (mouseOverHelpButton != null) mouseOverHelp.setActive(mouseOverHelpButton.isToggled());
        if (regulatorTextField != null) regulatorTextField.updateCursorCounter();
        if (sendDelay > 0 && --sendDelay <= 0) sendToServer();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (regulatorTextField != null && regulatorTextField.getVisible()) {
            regulatorTextField.drawTextBox();
        }
        List<String> tooltip = GuiTooltip.getHoveredTooltip(buttonList, mouseX, mouseY);
        if (tooltip != null) drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
        mouseOverHelp.draw(fontRendererObj, mouseX, mouseY, guiLeft, guiTop, xSize);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (regulatorTextField != null && regulatorTextField.getVisible()) {
            regulatorTextField.mouseClicked(mouseX, mouseY, button);
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (regulatorTextField != null && regulatorTextField.getVisible()
                && regulatorTextField.textboxKeyTyped(c, keyCode)) {
            sendDelay = 5;
            return;
        }
        if ((keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_E) && router != null) {
            player.openGui(ModularRouters.instance, ModularRouters.GUI_ROUTER,
                    player.worldObj, router.xCoord, router.yCoord, router.zCoord);
            return;
        }
        if (c == Config.configKey || c == 'c') {
            // Smart-filter configuration is handled by the filter GUI in later versions.
        }
        super.keyTyped(c, keyCode);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        Module module = ItemModule.getModule(moduleStack);
        if (module != null && !module.isDirectional()) {
            drawTexturedModalRect(guiLeft + 69, guiTop + 17, 204, 0, 52, 52);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = moduleStack.getDisplayName();
        if (router != null) title += " " + I18n.format("guiText.label.installed");
        fontRendererObj.drawString(title, xSize / 2 - fontRendererObj.getStringWidth(title) / 2, 5, 0x404040);

        Module module = ItemModule.getModule(moduleStack);
        if (module instanceof TargetedModule) {
            TargetedModule targeted = (TargetedModule) module;
            if (targeted.hasTarget(moduleStack)) {
                ModuleTarget target = targeted.getTarget(moduleStack);
                fontRendererObj.drawString(I18n.format("itemText.target.bound",
                        target.getX(), target.getY(), target.getZ()), 68, 69, 0x404040);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        sendToServer();
        super.onGuiClosed();
    }
}
