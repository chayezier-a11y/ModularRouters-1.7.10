package me.desht.modularrouters.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.entity.player.EntityPlayer;

public class ModuleConfigMessage implements IMessage {
    private int routerX, routerY, routerZ;
    private int slotIndex;

    public ModuleConfigMessage() {}

    public ModuleConfigMessage(int routerX, int routerY, int routerZ, int slotIndex) {
        this.routerX = routerX; this.routerY = routerY; this.routerZ = routerZ;
        this.slotIndex = slotIndex;
    }

    @Override public void toBytes(ByteBuf buf) { buf.writeInt(routerX); buf.writeInt(routerY); buf.writeInt(routerZ); buf.writeInt(slotIndex); }
    @Override public void fromBytes(ByteBuf buf) { routerX = buf.readInt(); routerY = buf.readInt(); routerZ = buf.readInt(); slotIndex = buf.readInt(); }

    public static class Handler implements IMessageHandler<ModuleConfigMessage, IMessage> {
        @Override
        public IMessage onMessage(ModuleConfigMessage msg, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(player.worldObj, msg.routerX, msg.routerY, msg.routerZ);
            if (router != null && msg.slotIndex >= 0 && msg.slotIndex < TileEntityItemRouter.N_MODULE_SLOTS
                    && router.isPermitted(player) && router.isUseableByPlayer(player)) {
                router.playerConfiguringModule(player, msg.slotIndex);
                player.openGui(ModularRouters.instance, ModularRouters.GUI_MODULE_INSTALLED,
                        player.worldObj, msg.routerX, msg.routerY, msg.routerZ);
            }
            return null;
        }
    }
}
