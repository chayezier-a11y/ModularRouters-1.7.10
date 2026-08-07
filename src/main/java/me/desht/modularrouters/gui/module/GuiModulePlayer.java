package me.desht.modularrouters.gui.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.container.ContainerModule;
import me.desht.modularrouters.gui.widgets.button.TexturedCycleButton;
import me.desht.modularrouters.gui.widgets.button.TooltipButton;
import me.desht.modularrouters.logic.compiled.CompiledPlayerModule;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class GuiModulePlayer extends GuiModule {
    private static final int BTN_OPERATION = 520;
    private static final int BTN_SECTION = 521;
    private OperationButton operationButton;
    private SectionButton sectionButton;

    public GuiModulePlayer(ContainerModule container, EntityPlayer player, ItemStack moduleStack,
                           me.desht.modularrouters.block.tile.TileEntityItemRouter router) {
        super(container, player, moduleStack, router);
    }

    @Override
    public void initGui() {
        super.initGui();
        CompiledPlayerModule initial = new CompiledPlayerModule(null, moduleStack);
        operationButton = new OperationButton(BTN_OPERATION, guiLeft + 148, guiTop + 32,
                initial.getOperation());
        sectionButton = new SectionButton(BTN_SECTION, guiLeft + 169, guiTop + 32,
                initial.getSection());
        buttonList.add(operationButton);
        buttonList.add(sectionButton);
        getMouseOverHelp().addHelpRegion(guiLeft + 127, guiTop + 29, guiLeft + 187, guiTop + 50,
                "guiText.popup.player.control");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_OPERATION) {
            operationButton.cycle(!isShiftKeyDown());
            sendToServer();
        } else if (button.id == BTN_SECTION) {
            sectionButton.cycle(!isShiftKeyDown());
            sendToServer();
        } else {
            super.actionPerformed(button);
        }
    }

    @Override
    protected NBTTagCompound buildSettings() {
        NBTTagCompound tag = super.buildSettings();
        tag.setByte(CompiledPlayerModule.NBT_OPERATION, (byte) operationButton.getState().ordinal());
        tag.setByte(CompiledPlayerModule.NBT_SECTION, (byte) sectionButton.getState().ordinal());
        return tag;
    }

    private static class OperationButton extends TexturedCycleButton<CompiledPlayerModule.Operation> {
        OperationButton(int id, int x, int y, CompiledPlayerModule.Operation initial) {
            super(id, x, y, 16, 16, initial, 16, new int[] {160, 176});
            for (CompiledPlayerModule.Operation operation : CompiledPlayerModule.Operation.values()) {
                addStateTooltipKey(operation, "guiText.tooltip.player.operation." + operation.name());
            }
        }
    }

    private static class SectionButton extends TooltipButton {
        private CompiledPlayerModule.Section state;

        SectionButton(int id, int x, int y, CompiledPlayerModule.Section initial) {
            super(id, x, y, 18, 18, label(initial));
            state = initial;
            for (CompiledPlayerModule.Section section : CompiledPlayerModule.Section.values()) {
                addStateTooltipKey(section.ordinal(), "guiText.tooltip.player.section." + section.name());
            }
            setTooltipState(state.ordinal());
        }

        CompiledPlayerModule.Section getState() { return state; }

        void cycle(boolean forward) {
            CompiledPlayerModule.Section[] values = CompiledPlayerModule.Section.values();
            int next = state.ordinal() + (forward ? 1 : -1);
            if (next >= values.length) next = 0;
            if (next < 0) next = values.length - 1;
            state = values[next];
            displayString = label(state);
            setTooltipState(state.ordinal());
        }

        private static String label(CompiledPlayerModule.Section section) {
            switch (section) {
                case MAIN: return "M";
                case MAIN_NO_HOTBAR: return "N";
                case ARMOR: return "A";
                case OFFHAND: return "O";
                case ENDER: return "E";
                default: return "?";
            }
        }
    }
}
