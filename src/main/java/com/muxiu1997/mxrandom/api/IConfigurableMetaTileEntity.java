package com.muxiu1997.mxrandom.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;

import com.muxiu1997.mxrandom.MXRandom;
import com.muxiu1997.mxrandom.network.message.MessageSyncMetaTileEntityConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import io.netty.buffer.ByteBuf;

public interface IConfigurableMetaTileEntity {

    IGregTechTileEntity getBaseMetaTileEntity();

    void readConfigFromBytes(ByteBuf buf);

    void writeConfigToBytes(ByteBuf buf);

    default void applyConfigChanges() {
        IGregTechTileEntity te = getBaseMetaTileEntity();
        te.markDirty();
        if (te.isServerSide()) {
            MXRandom.network.sendToServer(new MessageSyncMetaTileEntityConfig(this, false));
        } else if (te.isClientSide()) {
            MXRandom.network.sendToAll(new MessageSyncMetaTileEntityConfig(this, false));
        }
    }

    Object getServerGuiElement(int ID, EntityPlayer player);

    @SideOnly(Side.CLIENT)
    Object getClientGuiElement(int ID, EntityPlayer player);

    void onScrewdriverRightClick(ForgeDirection side, EntityPlayer player, float x, float y, float z);
}
