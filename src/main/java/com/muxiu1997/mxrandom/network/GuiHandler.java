package com.muxiu1997.mxrandom.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.muxiu1997.mxrandom.Utils;
import com.muxiu1997.mxrandom.api.IConfigurableMetaTileEntity;

import cpw.mods.fml.common.network.IGuiHandler;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;

public class GuiHandler implements IGuiHandler {

    private static final int ID_CONFIG_META_TILE_ENTITY = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == ID_CONFIG_META_TILE_ENTITY) {
            IMetaTileEntity mte = Utils.getMetaTileEntity(world, x, y, z);
            if (mte instanceof IConfigurableMetaTileEntity icmte) {
                return icmte.getServerGuiElement(ID, player);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == ID_CONFIG_META_TILE_ENTITY) {
            IMetaTileEntity mte = Utils.getMetaTileEntity(world, x, y, z);
            if (mte instanceof IConfigurableMetaTileEntity icmte) {
                return icmte.getClientGuiElement(ID, player);
            }
        }
        return null;
    }
}
