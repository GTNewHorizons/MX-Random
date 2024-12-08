package com.muxiu1997.mxrandom.loader;

import com.muxiu1997.mxrandom.metatileentity.MTELargeMolecularAssembler;

public class GTMetaTileEntityLoader {

    public static final int MTE_ID_OFFSET = 14100;

    public static MTELargeMolecularAssembler largeMolecularAssembler;

    public static void load() {
        largeMolecularAssembler = new MTELargeMolecularAssembler(
                MTE_ID_OFFSET + 1,
                "largemolecularassembler",
                "Large Molecular Assembler");
    }
}
