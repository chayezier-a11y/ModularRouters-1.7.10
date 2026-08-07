package me.desht.modularrouters.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class SyncUpgradeSettingsMessage implements IMessage {
    private NBTTagCompound data;

    public SyncUpgradeSettingsMessage() {}

    public SyncUpgradeSettingsMessage(NBTTagCompound data) {
        this.data = data;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, data);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        data = ByteBufUtils.readTag(buf);
    }

    public static class Handler implements IMessageHandler<SyncUpgradeSettingsMessage, IMessage> {
        @Override
        public IMessage onMessage(SyncUpgradeSettingsMessage message, MessageContext ctx) {
            return null;
        }
    }
}
