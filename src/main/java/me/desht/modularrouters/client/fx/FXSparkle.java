package me.desht.modularrouters.client.fx;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class FXSparkle extends EntityFX {
    public boolean fake;
    public boolean noClip;
    public boolean corrupt;
    private final int type;

    public FXSparkle(World world, double x, double y, double z, float size, float r, float g, float b, int type) {
        super(world, x, y, z, 0, 0, 0);
        this.type = type;
        this.particleRed = r;
        this.particleGreen = g;
        this.particleBlue = b;
        this.particleAlpha = 0.5F;
        this.particleScale = size;
        this.motionX = (rand.nextFloat() - 0.5) * 0.02;
        this.motionY = (rand.nextFloat() - 0.5) * 0.02;
        this.motionZ = (rand.nextFloat() - 0.5) * 0.02;
        this.particleMaxAge = 8 + rand.nextInt(12);
        this.noClip = false;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        particleScale *= 0.95F;
    }

    @Override
    public void renderParticle(Tessellator tessellator, float partialTicks, float rx, float ry, float rz, float rw, float rw2) {
        float u = (float) (particleTextureIndexX + (type * 16) % 256) / 256F;
        float v = (float) (particleTextureIndexY + (type / 16) * 16) / 256F;
        float u2 = u + 16F / 256F;
        float v2 = v + 16F / 256F;

        float scale = 0.1F * particleScale;

        float x = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
        float y = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
        float z = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

        tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha);
        tessellator.addVertexWithUV(x - rx * scale - rw * scale, y - rz * scale, z - ry * scale - rw2 * scale, u, v2);
        tessellator.addVertexWithUV(x - rx * scale + rw * scale, y + rz * scale, z - ry * scale + rw2 * scale, u, v);
        tessellator.addVertexWithUV(x + rx * scale + rw * scale, y + rz * scale, z + ry * scale + rw2 * scale, u2, v);
        tessellator.addVertexWithUV(x + rx * scale - rw * scale, y - rz * scale, z + ry * scale - rw2 * scale, u2, v2);
    }
}
