package me.desht.modularrouters.gui.widgets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.gui.widgets.button.ITooltipButton;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class EnergyWidget extends GuiButton implements ITooltipButton {
    private final TileEntityItemRouter router;

    public EnergyWidget(int id, int x, int y, TileEntityItemRouter router) {
        super(id, x, y, 14, 64, "");
        this.router = router;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, 0xFF30363A);
        drawRect(xPosition + 1, yPosition + 1, xPosition + width - 1, yPosition + height - 1, 0xFF111416);
        int amount = scaledHeight(router.getEnergyStorage().getEnergyStored(),
                router.getEnergyStorage().getMaxEnergyStored(), height - 2);
        if (amount > 0) {
            drawRect(xPosition + 1, yPosition + height - 1 - amount,
                    xPosition + width - 1, yPosition + height - 1, 0xFFE05245);
        }
    }

    static int scaledHeight(int energy, int capacity, int height) {
        if (energy <= 0 || capacity <= 0 || height <= 0) return 0;
        return Math.min(height, (int) ((long) energy * height / capacity));
    }

    @Override
    public List<String> getTooltip() {
        return Collections.singletonList(MiscUtil.translate("guiText.tooltip.energy.stored",
                router.getEnergyStorage().getEnergyStored(), router.getEnergyStorage().getMaxEnergyStored()));
    }
}
