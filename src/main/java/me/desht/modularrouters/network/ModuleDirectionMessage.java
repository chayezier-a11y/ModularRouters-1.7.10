package me.desht.modularrouters.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.item.module.Module;
import me.desht.modularrouters.item.module.Module.RelativeDirection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ModuleDirectionMessage implements IMessage {
    private int directionOrdinal;

    public ModuleDirectionMessage() {}

    public ModuleDirectionMessage(RelativeDirection dir) {
        this.directionOrdinal = dir.ordinal();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(directionOrdinal);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        directionOrdinal = buf.readByte();
    }

    public static class Handler implements IMessageHandler<ModuleDirectionMessage, IMessage> {
        @Override
        public IMessage onMessage(ModuleDirectionMessage message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            ItemStack stack = player.getHeldItem();
            if (stack != null && stack.getItem() instanceof ItemModule) {
                Module module = ItemModule.getModule(stack);
                if (module != null) {
                    RelativeDirection[] dirs = RelativeDirection.values();
                    if (message.directionOrdinal >= 0 && message.directionOrdinal < dirs.length) {
                        module.setDirection(stack, dirs[message.directionOrdinal]);
                    }
                }
            }
            return null;
        }
    }
}
