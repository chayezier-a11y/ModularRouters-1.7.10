package me.desht.modularrouters.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class FilterSettingsMessage implements IMessage {
    private NBTTagCompound data;

    public FilterSettingsMessage() {}

    public FilterSettingsMessage(NBTTagCompound data) {
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

    public static class Handler implements IMessageHandler<FilterSettingsMessage, IMessage> {
        @Override
        public IMessage onMessage(FilterSettingsMessage message, MessageContext ctx) {
            return null;
        }
    }
}
