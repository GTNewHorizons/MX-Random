package com.muxiu1997.mxrandom.network.message;

import java.io.IOException;

import net.minecraft.client.Minecraft;

import com.muxiu1997.mxrandom.MXRandom;
import com.muxiu1997.mxrandom.client.fx.CraftingFX;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class MessageCraftingFX implements IMessage {

    private int x;
    private int y;
    private int z;
    private int age;
    private IAEItemStack itemStack;

    @SuppressWarnings("unused")
    public MessageCraftingFX() {}

    public MessageCraftingFX(int x, int y, int z, int age, IAEItemStack itemStack) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.age = age;
        this.itemStack = itemStack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.age = buf.readInt();
        try {
            this.itemStack = AEItemStack.loadItemStackFromPacket(buf);
        } catch (IOException e) {
            MXRandom.logger.error("Could not deserialize LMA ItemStack", e);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(age);
        try {
            itemStack.writeToPacket(buf);
        } catch (IOException e) {
            MXRandom.logger.error("Could not serialize LMA ItemStack", e);
        }
    }

    public static class Handler implements IMessageHandler<MessageCraftingFX, IMessage> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(MessageCraftingFX message, MessageContext ctx) {
            if (ctx.side != Side.CLIENT) return null;
            CraftingFX fx = new CraftingFX(
                    Minecraft.getMinecraft().theWorld,
                    message.x,
                    message.y,
                    message.z,
                    message.age,
                    message.itemStack);
            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
            return null;
        }
    }
}
