package me.desht.modularrouters.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.Module.ModuleFlags;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleFlagsMessage implements IMessage {
    private int flagOrdinal;
    private boolean value;

    public ModuleFlagsMessage() {}

    public ModuleFlagsMessage(ModuleFlags flag, boolean value) {
        this.flagOrdinal = flag.ordinal();
        this.value = value;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(flagOrdinal);
        buf.writeBoolean(value);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        flagOrdinal = buf.readByte();
        value = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<ModuleFlagsMessage, IMessage> {
        @Override
        public IMessage onMessage(ModuleFlagsMessage msg, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            ItemStack stack = player.getHeldItem();
            if (stack != null && stack.getItem() instanceof ItemModule) {
                if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
                NBTTagCompound tag = stack.getTagCompound();
                ModuleFlags flag = ModuleFlags.values()[msg.flagOrdinal];
                tag.setBoolean(flag.toString(), msg.value);
                // Update flags byte
                byte flags = tag.hasKey("Flags") ? tag.getByte("Flags") : 0;
                if (msg.value) flags |= flag.getMask();
                else flags &= ~flag.getMask();
                tag.setByte("Flags", flags);
            }
            return null;
        }
    }
}
