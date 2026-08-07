package me.desht.modularrouters.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import net.minecraft.entity.player.EntityPlayer;

public class OpenGuiMessage implements IMessage {
    private int guiId;

    public OpenGuiMessage() {}

    public OpenGuiMessage(int guiId) {
        this.guiId = guiId;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(guiId);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        guiId = buf.readInt();
    }

    public static class Handler implements IMessageHandler<OpenGuiMessage, IMessage> {
        @Override
        public IMessage onMessage(OpenGuiMessage message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            player.openGui(ModularRouters.instance, message.guiId, player.worldObj,
                    (int) player.posX, (int) player.posY, (int) player.posZ);
            return null;
        }
    }
}
