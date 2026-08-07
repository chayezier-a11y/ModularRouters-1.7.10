package me.desht.modularrouters.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RouterSettingsMessageTest {
    @Test
    public void invalidEnumBytesUseSafeFallbacks() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(1).writeInt(2).writeInt(3);
        buffer.writeByte(127);
        buffer.writeBoolean(false);
        buffer.writeByte(127);
        RouterSettingsMessage message = new RouterSettingsMessage();

        message.fromBytes(buffer);

        assertEquals(RouterRedstoneBehaviour.ALWAYS, message.getRedstoneBehaviour());
        assertEquals(TileEntityItemRouter.EnergyDirection.NONE, message.getEnergyDirection());
    }

    @Test
    public void roundTripsEnergyDirection() {
        TileEntityItemRouter router = new TileEntityItemRouter();
        router.setEnergyDirection(TileEntityItemRouter.EnergyDirection.TO_ROUTER);
        RouterSettingsMessage source = new RouterSettingsMessage(router);
        ByteBuf buffer = Unpooled.buffer();
        source.toBytes(buffer);
        RouterSettingsMessage restored = new RouterSettingsMessage();

        restored.fromBytes(buffer);

        assertEquals(TileEntityItemRouter.EnergyDirection.TO_ROUTER, restored.getEnergyDirection());
    }
}
