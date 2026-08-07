package me.desht.modularrouters.client.fx;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.client.Minecraft;

@SideOnly(Side.CLIENT)
public class ParticleBeam {
    public ParticleBeam(double sx, double sy, double sz, double ex, double ey, double ez, int color, float thickness) {
        ModularRouters.proxy.setSparkleFXNoClip(true);
        double dx = ex - sx;
        double dy = ey - sy;
        double dz = ez - sz;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int count = (int) (dist * 4);
        float r = ((color >> 16) & 0xFF) / 255F;
        float g = ((color >> 8) & 0xFF) / 255F;
        float b = (color & 0xFF) / 255F;

        for (int i = 0; i < count; i++) {
            double t = (double) i / count;
            double x = sx + dx * t + (Math.random() - 0.5) * thickness;
            double y = sy + dy * t + (Math.random() - 0.5) * thickness;
            double z = sz + dz * t + (Math.random() - 0.5) * thickness;
            ModularRouters.proxy.sparkleFX(Minecraft.getMinecraft().theWorld,
                    x, y, z, r, g, b, 0.6f + (float) Math.random() * 0.3f, 0);
        }
        ModularRouters.proxy.setSparkleFXNoClip(false);
    }
}
