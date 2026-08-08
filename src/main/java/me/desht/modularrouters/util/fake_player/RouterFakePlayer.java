package me.desht.modularrouters.util.fake_player;

import com.mojang.authlib.GameProfile;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public class RouterFakePlayer extends FakePlayer {
    public static final UUID FAKE_UUID = UUID.fromString("c3e6871e-31a9-4aed-9cb3-39e4b6b5e5d6");
    public static final GameProfile FAKE_PROFILE = new GameProfile(FAKE_UUID, "[ModularRouters]");

    private final TileEntityItemRouter router;
    private ItemStack prevHeldStack;

    public RouterFakePlayer(TileEntityItemRouter router) {
        super((WorldServer) router.getWorldObj(), FAKE_PROFILE);
        this.router = router;
        this.prevHeldStack = null;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    public TileEntityItemRouter getRouter() {
        return router;
    }

    public void prepare(ItemStack held, boolean sneaking) {
        setPosition(router.xCoord + 0.5, router.yCoord + 0.5, router.zCoord + 0.5);
        setSneaking(sneaking);
        setCurrentItemOrArmor(0, held);
    }
}
