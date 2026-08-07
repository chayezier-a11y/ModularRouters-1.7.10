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
    private TileEntityItemRouter.EnergyDirection energyDirection;

    public RouterSettingsMessage() {}

    public RouterSettingsMessage(TileEntityItemRouter router) {
        this.x = router.xCoord;
        this.y = router.yCoord;
        this.z = router.zCoord;
        this.redstoneBehaviour = router.getRedstoneBehaviour();
        this.ecoMode = router.getEcoMode();
        this.energyDirection = router.getEnergyDirection();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(redstoneBehaviour.ordinal());
        buf.writeBoolean(ecoMode);
        buf.writeByte(energyDirection.ordinal());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        redstoneBehaviour = decodeRedstone(buf.readByte());
        ecoMode = buf.readBoolean();
        energyDirection = decodeEnergyDirection(buf.readByte());
    }

    public RouterRedstoneBehaviour getRedstoneBehaviour() {
        return redstoneBehaviour;
    }

    public TileEntityItemRouter.EnergyDirection getEnergyDirection() {
        return energyDirection;
    }

    private static RouterRedstoneBehaviour decodeRedstone(int ordinal) {
        RouterRedstoneBehaviour[] values = RouterRedstoneBehaviour.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : RouterRedstoneBehaviour.ALWAYS;
    }

    private static TileEntityItemRouter.EnergyDirection decodeEnergyDirection(int ordinal) {
        TileEntityItemRouter.EnergyDirection[] values = TileEntityItemRouter.EnergyDirection.values();
        return ordinal >= 0 && ordinal < values.length
                ? values[ordinal] : TileEntityItemRouter.EnergyDirection.NONE;
    }

    public static class Handler implements IMessageHandler<RouterSettingsMessage, IMessage> {
        @Override
        public IMessage onMessage(RouterSettingsMessage message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(player.worldObj, message.x, message.y, message.z);
            if (router != null && router.isPermitted(player) && router.isUseableByPlayer(player)) {
                router.setRedstoneBehaviour(message.redstoneBehaviour);
                router.setEcoMode(message.ecoMode);
                router.setEnergyDirection(message.energyDirection);
            }
            return null;
        }
    }
}
