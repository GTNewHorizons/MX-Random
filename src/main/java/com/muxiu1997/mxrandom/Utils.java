package com.muxiu1997.mxrandom;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class Utils {

    public static IMetaTileEntity getMetaTileEntity(World w, int x, int y, int z) {
        TileEntity te = w.getTileEntity(x, y, z);
        if (te instanceof IGregTechTileEntity igtte) {
            return igtte.getMetaTileEntity();
        }
        return null;
    }
}
