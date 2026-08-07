package me.desht.modularrouters.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.gui.widgets.button.TexturedCycleButton;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class EnergyDirectionButton extends TexturedCycleButton<TileEntityItemRouter.EnergyDirection> {
    private static final int[] TEXTURES = {144, 224, 176};
    private final TileEntityItemRouter router;

    public EnergyDirectionButton(int id, int x, int y, TileEntityItemRouter router) {
        super(id, x, y, 14, 14, router.getEnergyDirection(), 0, TEXTURES);
        this.router = router;
    }

    @Override
    protected boolean drawStandardBackground() {
        return false;
    }

    @Override
    public List<String> getTooltip() {
        List<String> result = new ArrayList<String>();
        result.add(me.desht.modularrouters.util.MiscUtil.translate(
                "guiText.tooltip.energy." + getState().name().toLowerCase()));
        result.add(me.desht.modularrouters.util.MiscUtil.translate(
                "guiText.tooltip.energy.rate", router.getEnergyXferRate()));
        return result;
    }
}
