package com.muxiu1997.mxrandom.network.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import com.muxiu1997.mxrandom.metatileentity.MTELargeMolecularAssembler;

public class ContainerConfigLargeMolecularAssembler extends Container {

    public final MTELargeMolecularAssembler LMA;

    public ContainerConfigLargeMolecularAssembler(MTELargeMolecularAssembler LMA) {
        this.LMA = LMA;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
