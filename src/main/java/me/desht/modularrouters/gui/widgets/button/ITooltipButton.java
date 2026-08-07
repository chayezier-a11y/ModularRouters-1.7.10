package me.desht.modularrouters.gui.widgets.button;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public interface ITooltipButton {
    List<String> getTooltip();
}
