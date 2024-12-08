package com.muxiu1997.mxrandom.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import com.muxiu1997.mxrandom.MXRandom;
import com.muxiu1997.mxrandom.Utils;
import com.muxiu1997.mxrandom.api.IConfigurableMetaTileEntity;
import com.muxiu1997.mxrandom.api.network.IMessageBothSideHandler;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MessageSyncMetaTileEntityConfig implements IMessage {

    private int x;
    private int y;
    private int z;
    private int dimID;
    private ByteBuf configData;
    private boolean openGui;

    @SuppressWarnings("unused")
    public MessageSyncMetaTileEntityConfig() {}

    public MessageSyncMetaTileEntityConfig(IConfigurableMetaTileEntity mte, boolean openGui) {
        IGregTechTileEntity igtte = mte.getBaseMetaTileEntity();
        this.x = igtte.getXCoord();
        this.y = igtte.getYCoord();
        this.z = igtte.getZCoord();
        this.dimID = igtte.getWorld().provider.dimensionId;
        this.configData = Unpooled.buffer();
        mte.writeConfigToBytes(this.configData);
        this.openGui = openGui;
    }

    public MessageSyncMetaTileEntityConfig(int x, int y, int z, int dimID, ByteBuf configData, boolean openGui) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimID = dimID;
        this.configData = configData;
        this.openGui = openGui;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.dimID = buf.readInt();
        this.configData = buf.readBytes(buf.readInt());
        this.openGui = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeInt(this.dimID);
        buf.writeInt(this.configData.readableBytes());
        buf.writeBytes(this.configData);
        buf.writeBoolean(this.openGui);
    }

    public static class Handler implements IMessageBothSideHandler<MessageSyncMetaTileEntityConfig, IMessage> {

        @Override
        public IMessage handleClientSideMessage(MessageSyncMetaTileEntityConfig message, MessageContext ctx) {
            World world = DimensionManager.getWorld(message.dimID);
            if (world == null) {
                return null;
            }

            IMetaTileEntity mte = Utils.getMetaTileEntity(world, message.x, message.y, message.z);
            if (mte instanceof IConfigurableMetaTileEntity icmte) {
                icmte.readConfigFromBytes(message.configData);
                if (message.openGui) {
                    Minecraft.getMinecraft().thePlayer
                            .openGui(MXRandom.MODID, 1, world, message.x, message.y, message.z);
                }
            }
            return null;
        }

        @Override
        public IMessage handleServerSideMessage(MessageSyncMetaTileEntityConfig message, MessageContext ctx) {
            World world = DimensionManager.getWorld(message.dimID);
            if (world == null) {
                return null;
            }

            IMetaTileEntity mte = Utils.getMetaTileEntity(world, message.x, message.y, message.z);
            if (mte instanceof IConfigurableMetaTileEntity icmte) {
                ByteBuf configData = message.configData.copy();
                icmte.readConfigFromBytes(message.configData);
                message.openGui = false;
                message.configData = configData;
                MXRandom.network.sendToAll(message);
            }
            return null;
        }
    }
}
