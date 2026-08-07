package me.desht.modularrouters.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import me.desht.modularrouters.client.fx.ParticleBeam;
import net.minecraft.client.Minecraft;

public class ParticleBeamMessage implements IMessage {
    private double startX, startY, startZ;
    private double endX, endY, endZ;
    private int color;
    private float thickness;

    public ParticleBeamMessage() {}

    public ParticleBeamMessage(double sx, double sy, double sz, double ex, double ey, double ez, int color, float thickness) {
        this.startX = sx; this.startY = sy; this.startZ = sz;
        this.endX = ex; this.endY = ey; this.endZ = ez;
        this.color = color;
        this.thickness = thickness;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(startX); buf.writeDouble(startY); buf.writeDouble(startZ);
        buf.writeDouble(endX); buf.writeDouble(endY); buf.writeDouble(endZ);
        buf.writeInt(color);
        buf.writeFloat(thickness);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        startX = buf.readDouble(); startY = buf.readDouble(); startZ = buf.readDouble();
        endX = buf.readDouble(); endY = buf.readDouble(); endZ = buf.readDouble();
        color = buf.readInt();
        thickness = buf.readFloat();
    }

    public static class Handler implements IMessageHandler<ParticleBeamMessage, IMessage> {
        @Override
        public IMessage onMessage(ParticleBeamMessage message, MessageContext ctx) {
            new ParticleBeam(message.startX, message.startY, message.startZ,
                    message.endX, message.endY, message.endZ, message.color, message.thickness);
            return null;
        }
    }
}
