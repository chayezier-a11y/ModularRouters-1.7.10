package me.desht.modularrouters.gui.widgets.button;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class TexturedButton extends GuiButton implements ITooltipButton {
    private final ResourceLocation texture;
    private final int texU, texV;
    private final int texW, texH;
    protected final List<String> tooltip = new ArrayList<String>();

    public TexturedButton(int id, int x, int y, int w, int h, ResourceLocation texture, int texU, int texV) {
        super(id, x, y, w, h, "");
        this.texture = texture;
        this.texU = texU;
        this.texV = texV;
        this.texW = w;
        this.texH = h;
    }

    public TexturedButton addTooltip(String... lines) {
        for (String line : lines) tooltip.add(line);
        return this;
    }

    public TexturedButton addTooltipKey(String key, Object... args) {
        me.desht.modularrouters.gui.GuiTooltip.append(tooltip, key, args);
        return this;
    }

    protected int getTextureX() { return texU; }

    protected int getTextureY() { return texV; }

    protected boolean drawStandardBackground() { return true; }

    @Override
    public List<String> getTooltip() { return tooltip; }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;
        mc.getTextureManager().bindTexture(texture);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        int background = !enabled ? 0 : hovered ? 2 : 1;
        if (drawStandardBackground()) {
            drawTexturedModalRect(xPosition, yPosition, background * 16, 0, texW, texH);
        }
        drawTexturedModalRect(xPosition, yPosition, getTextureX(), getTextureY(), texW, texH);
    }
}
