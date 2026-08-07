package me.desht.modularrouters.gui;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.container.ContainerItemRouter;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.gui.widgets.button.TexturedToggleButton;
import me.desht.modularrouters.gui.widgets.EnergyWidget;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.network.ModuleConfigMessage;
import me.desht.modularrouters.network.RouterSettingsMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GuiItemRouter extends GuiContainer {
    public final TileEntityItemRouter router;
    private final EntityPlayer player;
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ModularRouters.modId, "textures/gui/router.png");
    private static final int BTN_REDSTONE = 300;
    private static final int BTN_ECO = 301;
    private static final int BTN_ENERGY_DIRECTION = 302;
    private static final int WIDGET_ENERGY = 303;
    private RedstoneBehaviourButton redstoneButton;
    private TexturedToggleButton ecoButton;
    private EnergyDirectionButton energyDirectionButton;
    private EnergyWidget energyWidget;

    public GuiItemRouter(EntityPlayer player, TileEntityItemRouter router) {
        super(new ContainerItemRouter(player, router));
        this.player = player;
        this.router = router;
        this.xSize = 176;
        this.ySize = 186;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        redstoneButton = new RedstoneBehaviourButton(
                BTN_REDSTONE, guiLeft + 152, guiTop + 10, 16, 16,
                router.getRedstoneBehaviour());
        ecoButton = new TexturedToggleButton(BTN_ECO, guiLeft + 132, guiTop + 10,
                16, 16, 80, 16, router.getEcoMode());
        ecoButton.addTooltipKey("guiText.tooltip.eco.false");
        ecoButton.addToggledTooltipKey("guiText.tooltip.eco.true",
                Config.ecoTimeout / 20.0f, Config.lowPowerTickRate / 20.0f);
        buttonList.add(redstoneButton);
        buttonList.add(ecoButton);
        energyDirectionButton = new EnergyDirectionButton(BTN_ENERGY_DIRECTION,
                guiLeft - 8, guiTop + 40, router);
        energyWidget = new EnergyWidget(WIDGET_ENERGY, guiLeft - 24, guiTop + 15, router);
        buttonList.add(energyDirectionButton);
        buttonList.add(energyWidget);
        updateEnergyControlVisibility();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_REDSTONE) {
            redstoneButton.cycle(!isShiftKeyDown());
            sendRouterSettings();
        } else if (button.id == BTN_ECO) {
            ecoButton.toggle();
            sendRouterSettings();
        } else if (button.id == BTN_ENERGY_DIRECTION) {
            energyDirectionButton.cycle(!isShiftKeyDown());
            sendRouterSettings();
        }
    }

    private void sendRouterSettings() {
        router.setRedstoneBehaviour(redstoneButton.getState());
        router.setEcoMode(ecoButton.isToggled());
        router.setEnergyDirection(energyDirectionButton.getState());
        ModularRouters.network.sendToServer(new RouterSettingsMessage(router));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = I18n.format("tile.itemRouter.name");
        fontRendererObj.drawString(title, xSize / 2 - fontRendererObj.getStringWidth(title) / 2, 5, 0x404040);
        fontRendererObj.drawString(I18n.format("guiText.label.buffer"), 8, 28, 0x404040);
        fontRendererObj.drawString(I18n.format("guiText.label.upgrades"), ContainerItemRouter.UPGRADE_XPOS, 28, 0x404040);
        fontRendererObj.drawString(I18n.format("guiText.label.modules"), ContainerItemRouter.MODULE_XPOS, 60, 0x404040);
        fontRendererObj.drawString(I18n.format("container.inventory"), 8, ySize - 96 + 4, 0x404040);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateEnergyControlVisibility();
        super.drawScreen(mouseX, mouseY, partialTicks);
        java.util.List<String> tooltip = GuiTooltip.getHoveredTooltip(buttonList, mouseX, mouseY);
        if (tooltip != null) drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
    }

    private void updateEnergyControlVisibility() {
        boolean hasEnergy = router.getEnergyCapacity() > 0;
        energyWidget.visible = hasEnergy;
        net.minecraft.item.ItemStack stack = router.getBufferItemStack();
        energyDirectionButton.visible = hasEnergy && stack != null
                && stack.getItem() instanceof IEnergyContainerItem;
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIndex, int button, int clickType) {
        boolean configureClick = button == 1 || (isShiftKeyDown() && button == 0);
        if (configureClick && configureModule(slot, slotIndex)) {
            return;
        }
        super.handleMouseClick(slot, slotIndex, button, clickType);
    }

    /**
     * GuiContainer does not route middle-clicks through handleMouseClick in 1.7.10.
     * Resolve the slot here so the original module-configure middle-click works.
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 2 && configureModule(getSlotAtPosition(mouseX, mouseY), -1)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean configureModule(Slot slot, int slotIndex) {
        if (slot == null) return false;
        if (slotIndex < 0) slotIndex = slot.slotNumber;
        int moduleStart = ContainerItemRouter.TE_FIRST_SLOT + ContainerItemRouter.MODULE_SLOT_START;
        boolean isModuleSlot = slotIndex >= moduleStart && slotIndex < moduleStart + 9;
        if (!isModuleSlot || !slot.getHasStack() || ItemModule.getModule(slot.getStack()) == null) return false;

        int moduleSlot = slotIndex - moduleStart;
        // The server stores this mapping before opening the GUI. Keep the client-side
        // mapping in sync as well, since GuiHandler needs it to build the client GUI.
        router.playerConfiguringModule(player, moduleSlot);
        ModularRouters.network.sendToServer(new ModuleConfigMessage(
                router.xCoord, router.yCoord, router.zCoord, moduleSlot));
        return true;
    }

    private Slot getSlotAtPosition(int mouseX, int mouseY) {
        int localX = mouseX - guiLeft;
        int localY = mouseY - guiTop;
        for (Object obj : inventorySlots.inventorySlots) {
            Slot slot = (Slot) obj;
            if (localX >= slot.xDisplayPosition - 1 && localX < slot.xDisplayPosition + 17
                    && localY >= slot.yDisplayPosition - 1 && localY < slot.yDisplayPosition + 17) {
                return slot;
            }
        }
        return null;
    }
}
