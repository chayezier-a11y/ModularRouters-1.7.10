package me.desht.modularrouters.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class MouseOverHelp {
    private static final int TEXT_MARGIN = 8;
    private final List<HelpRegion> regions = new ArrayList<HelpRegion>();
    private boolean active;

    public void setActive(boolean active) {
        this.active = active;
    }

    public void addHelpRegion(int x1, int y1, int x2, int y2, String key) {
        addHelpRegion(x1, y1, x2, y2, key, null);
    }

    public void addHelpRegion(int x1, int y1, int x2, int y2, String key, Visibility visibility) {
        List<String> text = new ArrayList<String>();
        text.addAll(GuiTooltip.split(I18n.format(key)));
        regions.add(new HelpRegion(x1, y1, x2, y2, text, visibility));
    }

    public void draw(FontRenderer fontRenderer, int mouseX, int mouseY,
                     int guiLeft, int guiTop, int guiWidth) {
        if (!active) return;
        for (HelpRegion region : regions) {
            if (region.visibility != null && !region.visibility.isVisible()) continue;
            if (mouseX < region.x1 || mouseX >= region.x2 || mouseY < region.y1 || mouseY >= region.y2) {
                continue;
            }
            List<String> displayLines = new ArrayList<String>();
            for (String line : region.text) {
                displayLines.addAll(fontRenderer.listFormattedStringToWidth(line, 220));
            }
            Gui.drawRect(region.x1, region.y1, region.x2, region.y2, 0x6040FFFF);
            int width = 0;
            for (String line : displayLines) width = Math.max(width, fontRenderer.getStringWidth(line));
            int height = displayLines.size() * 9;
            int popupX = region.x1 - guiLeft < guiWidth / 2
                    ? region.x2 + 10 : region.x1 - width - TEXT_MARGIN - 10;
            int popupY = region.y1 + ((region.y2 - region.y1) - height - TEXT_MARGIN) / 2;
            int popupX2 = popupX + width + TEXT_MARGIN;
            int popupY2 = popupY + height + TEXT_MARGIN;
            Gui.drawRect(popupX, popupY, popupX2, popupY2, 0xC0000000);
            Gui.drawRect(popupX, popupY, popupX2, popupY + 1, 0xE0202020);
            Gui.drawRect(popupX, popupY2, popupX2, popupY2 + 1, 0xE0202020);
            Gui.drawRect(popupX, popupY, popupX + 1, popupY2, 0xE0202020);
            Gui.drawRect(popupX2, popupY, popupX2 + 1, popupY2 + 1, 0xE0202020);
            int textY = popupY + TEXT_MARGIN / 2;
            for (String line : displayLines) {
                fontRenderer.drawString(line, popupX + TEXT_MARGIN / 2, textY, 0xE0E0E0);
                textY += 9;
            }
            return;
        }
    }

    public interface Visibility {
        boolean isVisible();
    }

    private static final class HelpRegion {
        private final int x1, y1, x2, y2;
        private final List<String> text;
        private final Visibility visibility;

        private HelpRegion(int x1, int y1, int x2, int y2, List<String> text, Visibility visibility) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.text = text;
            this.visibility = visibility;
        }
    }

}
