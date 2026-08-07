package me.desht.modularrouters.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.block.tile.TileEntityItemRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class RouterUpgradesSyncMessage implements IMessage {
    private int x, y, z;
    private NBTTagCompound upgradeData;

    public RouterUpgradesSyncMessage() {}

    public RouterUpgradesSyncMessage(TileEntityItemRouter router) {
        this.x = router.xCoord;
        this.y = router.yCoord;
        this.z = router.zCoord;
        this.upgradeData = new NBTTagCompound();
        router.getUpgrades().writeToNBT(upgradeData);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeTag(buf, upgradeData);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        upgradeData = ByteBufUtils.readTag(buf);
    }

    public static class Handler implements IMessageHandler<RouterUpgradesSyncMessage, IMessage> {
        @Override
        public IMessage onMessage(RouterUpgradesSyncMessage msg, MessageContext ctx) {
            TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(msg.x, msg.y, msg.z);
            if (te instanceof TileEntityItemRouter) {
                TileEntityItemRouter router = (TileEntityItemRouter) te;
                router.getUpgrades().readFromNBT(msg.upgradeData);
                router.recompileNeeded(TileEntityItemRouter.COMPILE_UPGRADES);
            }
            return null;
        }
    }
}
