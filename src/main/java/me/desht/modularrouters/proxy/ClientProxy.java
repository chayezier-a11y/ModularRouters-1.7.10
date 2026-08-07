package me.desht.modularrouters.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.client.fx.FXSparkle;
import me.desht.modularrouters.config.Config;
import me.desht.modularrouters.gui.GuiItemRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    private static boolean noclipEnabled = false;
    private static boolean corruptSparkle = false;

    @Override
    public void preInit() {
        super.preInit();
        MinecraftForge.EVENT_BUS.register(new Config.ConfigEventHandler());
        FMLCommonHandler.instance().bus().register(new Config.ConfigEventHandler());
    }

    @Override
    public void init() { super.init(); }

    @Override
    public void postInit() { super.postInit(); }

    @Override
    public void registerItemRenderer(Item item, int meta, String id) {
        // 1.7.10 uses the IIcon system: registerIcons() + getIconFromDamage()
        // No additional ModelResourceLocation registration needed
    }

    @Override
    public void setSparkleFXNoClip(boolean noclip) { noclipEnabled = noclip; }

    @Override
    public void setSparkleFXCorrupt(boolean corrupt) { corruptSparkle = corrupt; }

    @Override
    public void sparkleFX(World world, double x, double y, double z, float r, float g, float b, float size, int m, boolean fake) {
        if (!doParticle(world) && !fake) return;
        FXSparkle sparkle = new FXSparkle(world, x, y, z, size, r, g, b, m);
        sparkle.fake = sparkle.noClip = fake;
        if (noclipEnabled) sparkle.noClip = true;
        if (corruptSparkle) sparkle.corrupt = true;
        Minecraft.getMinecraft().effectRenderer.addEffect(sparkle);
    }

    private boolean doParticle(World world) {
        if (!world.isRemote) return false;
        float chance = 1F;
        if (Minecraft.getMinecraft().gameSettings.particleSetting == 1) chance = 0.6F;
        else if (Minecraft.getMinecraft().gameSettings.particleSetting == 2) chance = 0.2F;
        return chance == 1F || Math.random() < chance;
    }

    @Override
    public World theClientWorld() { return Minecraft.getMinecraft().theWorld; }

    @Override
    public TileEntityItemRouter getOpenItemRouter() {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiItemRouter) {
            return ((GuiItemRouter) Minecraft.getMinecraft().currentScreen).router;
        }
        return null;
    }
}