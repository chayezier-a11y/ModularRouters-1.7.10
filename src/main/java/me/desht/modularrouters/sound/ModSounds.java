package me.desht.modularrouters.sound;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class ModSounds {

    public static void init() {
        // Sound events would be registered here
    }

    public static void playSound(World world, double x, double y, double z, String soundName, float volume, float pitch) {
        world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, ModularRouters.modId + ":" + soundName, volume, pitch);
    }
}
