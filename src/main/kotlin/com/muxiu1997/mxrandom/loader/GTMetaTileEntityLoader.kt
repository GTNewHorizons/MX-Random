package com.muxiu1997.mxrandom.loader

import com.muxiu1997.mxrandom.MODID
import com.muxiu1997.mxrandom.MXRandom.MTE_ID_OFFSET
import com.muxiu1997.mxrandom.metatileentity.MTELargeMolecularAssembler
import cpw.mods.fml.common.event.FMLInitializationEvent
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase

object GTMetaTileEntityLoader {
  lateinit var largeMolecularAssembler: MTELargeMolecularAssembler

  fun load(@Suppress("UNUSED_PARAMETER") e: FMLInitializationEvent) {
    largeMolecularAssembler = register(1, "Large Molecular Assembler")
  }

  private inline fun <reified T : MTEMultiBlockBase> register(
      id: Int,
      name: String,
      unlocalizedName: String? = null
  ): T {
    val constructor =
        T::class.java.getConstructor(Int::class.java, String::class.java, String::class.java)
    val metaTileEntityUnlocalizedName =
        MODID + (unlocalizedName ?: name.replace(" ", "").lowercase())
    return constructor.newInstance(MTE_ID_OFFSET + id, metaTileEntityUnlocalizedName, name)
  }
}
