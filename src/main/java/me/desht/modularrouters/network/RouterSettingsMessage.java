package me.desht.modularrouters.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.logic.RouterRedstoneBehaviour;
import net.minecraft.entity.player.EntityPlayer;

public class RouterSettingsMessage implements IMessage {
    private int x, y, z;
    private RouterRedstoneBehaviour redstoneBehaviour;
    private boolean ecoMode;

    public RouterSettingsMessage() {}

    public RouterSettingsMessage(TileEntityItemRouter router) {
        this.x = router.xCoord;
        this.y = router.yCoord;
        this.z = router.zCoord;
        this.redstoneBehaviour = router.getRedstoneBehaviour();
        this.ecoMode = router.getEcoMode();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(redstoneBehaviour.ordinal());
        buf.writeBoolean(ecoMode);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        redstoneBehaviour = RouterRedstoneBehaviour.values()[buf.readByte()];
        ecoMode = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<RouterSettingsMessage, IMessage> {
        @Override
        public IMessage onMessage(RouterSettingsMessage message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(player.worldObj, message.x, message.y, message.z);
            if (router != null && router.isPermitted(player) && router.isUseableByPlayer(player)) {
                router.setRedstoneBehaviour(message.redstoneBehaviour);
                router.setEcoMode(message.ecoMode);
            }
            return null;
        }
    }
}
