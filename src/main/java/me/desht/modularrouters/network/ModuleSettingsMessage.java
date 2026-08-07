package me.desht.modularrouters.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import me.desht.modularrouters.item.module.ItemModule;
import me.desht.modularrouters.util.ModuleHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleSettingsMessage implements IMessage {
    private boolean hasRouter;
    private int routerX, routerY, routerZ, slotIndex;
    private NBTTagCompound data;

    public ModuleSettingsMessage() {}

    public ModuleSettingsMessage(TileEntityItemRouter router, int slot, NBTTagCompound data) {
        this.hasRouter = true;
        this.routerX = router.xCoord;
        this.routerY = router.yCoord;
        this.routerZ = router.zCoord;
        this.slotIndex = slot;
        this.data = data;
    }

    public ModuleSettingsMessage(NBTTagCompound data) {
        this.hasRouter = false;
        this.data = data;
    }

    @Override public void toBytes(ByteBuf buf) { buf.writeBoolean(hasRouter); if (hasRouter) { buf.writeInt(routerX); buf.writeInt(routerY); buf.writeInt(routerZ); buf.writeInt(slotIndex); } ByteBufUtils.writeTag(buf, data); }
    @Override public void fromBytes(ByteBuf buf) { hasRouter = buf.readBoolean(); if (hasRouter) { routerX = buf.readInt(); routerY = buf.readInt(); routerZ = buf.readInt(); slotIndex = buf.readInt(); } data = ByteBufUtils.readTag(buf); }

    public static class Handler implements IMessageHandler<ModuleSettingsMessage, IMessage> {
        @Override
        public IMessage onMessage(ModuleSettingsMessage msg, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            ItemStack moduleStack;
            if (msg.hasRouter) {
                TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(player.worldObj, msg.routerX, msg.routerY, msg.routerZ);
                if (router == null || msg.slotIndex < 0 || msg.slotIndex >= TileEntityItemRouter.N_MODULE_SLOTS
                        || !router.isPermitted(player) || !router.isUseableByPlayer(player)) return null;
                moduleStack = router.getModules().getStackInSlot(msg.slotIndex);
            } else {
                moduleStack = player.getHeldItem();
            }
            if (moduleStack != null && ItemModule.getModule(moduleStack) != null && msg.data != null) {
                NBTTagCompound compound = ModuleHelper.validateNBT(moduleStack);
                for (Object k : msg.data.getKeySet()) {
                    String key = (String) k;
                    compound.setTag(key, msg.data.getTag(key));
                }
                if (msg.hasRouter) {
                    TileEntityItemRouter router = TileEntityItemRouter.getRouterAt(player.worldObj, msg.routerX, msg.routerY, msg.routerZ);
                    if (router != null) {
                        router.recompileNeeded(TileEntityItemRouter.COMPILE_MODULES);
                        router.markDirty();
                    }
                }
            }
            return null;
        }
    }
}
