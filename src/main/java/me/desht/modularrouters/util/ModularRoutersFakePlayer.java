package me.desht.modularrouters.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public class ModularRoutersFakePlayer extends FakePlayer {
    private static final UUID FAKE_UUID = UUID.fromString("c3e6871e-31a9-4aed-9cb3-39e4b6b5e5d6");
    private static final GameProfile FAKE_PROFILE = new GameProfile(FAKE_UUID, "[ModularRouters]");

    public ModularRoutersFakePlayer(World world) {
        super((WorldServer) world, FAKE_PROFILE);
    }
}
