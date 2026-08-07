package me.desht.modularrouters.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.gui.widgets.button.TexturedCycleButton;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RedstoneBehaviourButton extends TexturedCycleButton<RouterRedstoneBehaviour> {
    private static final int[] TEXTURES = {0, 16, 32, 48, 64};

    public RedstoneBehaviourButton(int id, int x, int y, int w, int h, RouterRedstoneBehaviour initial) {
        super(id, x, y, w, h, initial, 16, TEXTURES);
    }

    public RouterRedstoneBehaviour getBehaviour() {
        return getState();
    }

    @Override
    public List<String> getTooltip() {
        List<String> result = new ArrayList<String>();
        result.add(me.desht.modularrouters.util.MiscUtil.translate("guiText.tooltip.redstone.label")
                + ": " + me.desht.modularrouters.util.MiscUtil.translate(
                "guiText.tooltip.redstone." + getState().name()));
        return result;
    }
}
