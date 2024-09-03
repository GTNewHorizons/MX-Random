@file:Suppress("ClassName")

package com.muxiu1997.mxrandom.metatileentity

import appeng.api.AEApi
import appeng.api.networking.GridFlags
import appeng.api.networking.IGridNode
import appeng.api.networking.crafting.ICraftingPatternDetails
import appeng.api.networking.crafting.ICraftingProvider
import appeng.api.networking.crafting.ICraftingProviderHelper
import appeng.api.networking.events.MENetworkCraftingPatternChange
import appeng.api.networking.security.BaseActionSource
import appeng.api.networking.security.IActionHost
import appeng.api.networking.security.MachineSource
import appeng.api.storage.data.IAEItemStack
import appeng.api.storage.data.IItemList
import appeng.api.util.DimensionalCoord
import appeng.items.misc.ItemEncodedPattern
import appeng.me.GridAccessException
import appeng.me.helpers.AENetworkProxy
import appeng.me.helpers.IGridProxyable
import appeng.util.Platform
import com.gtnewhorizon.structurelib.structure.IStructureDefinition
import com.gtnewhorizon.structurelib.structure.IStructureElementCheckOnly
import com.gtnewhorizon.structurelib.structure.StructureDefinition
import com.gtnewhorizon.structurelib.structure.StructureUtility.*
import com.muxiu1997.mxrandom.MODNAME
import com.muxiu1997.mxrandom.MXRandom.network
import com.muxiu1997.mxrandom.api.IConfigurableMetaTileEntity
import com.muxiu1997.mxrandom.client.gui.GuiConfigLargeMolecularAssembler
import com.muxiu1997.mxrandom.network.container.ContainerConfigLargeMolecularAssembler
import com.muxiu1997.mxrandom.network.message.MessageCraftingFX
import com.muxiu1997.mxrandom.network.message.MessageSyncMetaTileEntityConfig
import cpw.mods.fml.common.network.NetworkRegistry
import gregtech.api.GregTechAPI
import gregtech.api.enums.ItemList
import gregtech.api.enums.Textures.BlockIcons
import gregtech.api.interfaces.ITexture
import gregtech.api.interfaces.metatileentity.IMetaTileEntity
import gregtech.api.interfaces.tileentity.IGregTechTileEntity
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase
import gregtech.api.render.TextureFactory
import gregtech.api.util.GTStructureUtility.ofHatchAdder
import gregtech.api.util.GTUtility
import gregtech.api.util.MultiblockTooltipBuilder
import gregtech.common.items.behaviors.BehaviourDataOrb
import io.netty.buffer.ByteBuf
import java.util.*
import kotlin.math.max
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.EnumChatFormatting
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

@Deprecated("Planned for removal.")
class LargeMolecularAssembler :
    MTEEnhancedMultiBlockBase<LargeMolecularAssembler>,
    IConfigurableMetaTileEntity,
    ICraftingProvider,
    IActionHost,
    IGridProxyable {

  private var casing: Byte = 0
  private var craftingDisplayPoint: CraftingDisplayPoint? = null

  private var cachedDataOrb: ItemStack? = null
  private var cachedAeJobs: LinkedList<ItemStack>? = LinkedList()
  private var aeJobsDirty = false

  private var cachedPatterns: List<ItemStack> = emptyList()
  private var cachedRawPatterns: List<ItemStack> = emptyList()
  private var cachedPatternDetails: List<ICraftingPatternDetails> = emptyList()

  private var requestSource: BaseActionSource? = null
  private var cachedOutputs = AEApi.instance().storage().createItemList()
  private var lastOutputFailed = false
  private var lastOutputTick: Long = 0
  private var tickCounter: Long = 0

  var hiddenCraftingFX = false

  @Suppress("unused")
  constructor(aID: Int, aName: String, aNameRegional: String) : super(aID, aName, aNameRegional)

  constructor(aName: String) : super(aName)

  // region MTEEnhancedMultiBlockBase
  override fun newMetaEntity(iGregTechTileEntity: IGregTechTileEntity): IMetaTileEntity {
    return LargeMolecularAssembler(this.mName)
  }

  override fun getTexture(
      baseMetaTileEntity: IGregTechTileEntity?,
      side: ForgeDirection?,
      facing: ForgeDirection?,
      colorIndex: Int,
      active: Boolean,
      redstone: Boolean
  ): Array<out ITexture> {
    return when (side) {
      facing ->
          arrayOf(
              BlockIcons.getCasingTextureForId(CASING_INDEX),
              TextureFactory.builder().addIcon(BlockIcons.OVERLAY_ME_HATCH).extFacing().build(),
          )
      else ->
          arrayOf(
              BlockIcons.getCasingTextureForId(CASING_INDEX),
          )
    }
  }

  override fun isCorrectMachinePart(aStack: ItemStack?): Boolean = true

  override fun checkRecipe(stack: ItemStack?): Boolean {
    withAeJobs { _, aeJobs ->
      mMaxProgresstime = 20
      var craftingProgressTime = 20
      var craftingEUt = EU_PER_TICK_CRAFTING
      mEUt = -EU_PER_TICK_BASIC
      // Tier EU_PER_TICK_CRAFTING == 2
      var extraTier = max(0, GTUtility.getTier(maxInputVoltage).toInt() - 2)
      // The first two Overclocks reduce the Finish time to 0.5s and 0.25s
      for (i in 0 until 2) {
        if (extraTier <= 0) break
        craftingProgressTime /= 2
        craftingEUt *= 4
        extraTier--
      }
      var times = 2
      // Subsequent Overclocks Double the number of Jobs finished at once to a Max of 256"
      while (times < 256) {
        if (extraTier <= 0) break
        times *= 2
        craftingEUt *= 4
        extraTier--
      }
      val outputs = LinkedList<ItemStack>()
      for (i in 0 until times) {
        if (aeJobs.isEmpty()) break
        outputs.addFirst(aeJobs.removeFirst())
        aeJobsDirty = true
      }
      if (outputs.isNotEmpty()) {
        mEUt = -craftingEUt
        mMaxProgresstime = craftingProgressTime
        mOutputItems = outputs.toTypedArray()
        addCraftingFX(outputs.first)
      }
      mEfficiency = 10000 - (idealStatus - repairStatus) * 1000
      mEfficiencyIncrease = 10000
      return true
    }
    return false
  }

  override fun saveNBTData(nbt: NBTTagCompound) {
    saveAeJobsIfNeeded()
    super.saveNBTData(nbt)
    cachedOutputs.saveNBTData(nbt, NBT_KEY_CACHED_OUTPUTS)
    nbt.setBoolean(NBT_KEY_CONFIG_HIDDEN_CRAFTING_FX, hiddenCraftingFX)
  }

  override fun loadNBTData(nbt: NBTTagCompound) {
    super.loadNBTData(nbt)
    cachedOutputs.loadNBTData(nbt, NBT_KEY_CACHED_OUTPUTS)
    hiddenCraftingFX = nbt.getBoolean(NBT_KEY_CONFIG_HIDDEN_CRAFTING_FX)
  }

  override fun getMaxEfficiency(aStack: ItemStack?): Int = 10000

  override fun getDamageToComponent(aStack: ItemStack?): Int = 0

  override fun explodesOnComponentBreak(aStack: ItemStack?): Boolean = false

  @Suppress("FunctionName")
  override fun createTooltip(): MultiblockTooltipBuilder {
    fun GREEN(thing: Any) = "${EnumChatFormatting.GREEN}$thing${EnumChatFormatting.GRAY}"
    fun WHITE(thing: Any) = "${EnumChatFormatting.WHITE}$thing${EnumChatFormatting.GRAY}"
    fun PURPLE(thing: Any) = "${EnumChatFormatting.DARK_PURPLE}$thing${EnumChatFormatting.GRAY}"
    return MultiblockTooltipBuilder().also {
      it.addMachineType(MACHINE_TYPE)
          .addInfo(EnumChatFormatting.RED.toString() + "DEPRECATED, PLANNED FOR REMOVAL.")
          .addInfo("Need a Data Orb to put in the Controller to work")
          .addInfo("Basic: ${GREEN(EU_PER_TICK_BASIC)} Eu/t, Unaffected by overclocking")
          .addInfo(
              "Crafting: ${GREEN(EU_PER_TICK_CRAFTING)} Eu/t, Finish ${WHITE(2)} Jobs in ${WHITE(1)}s")
          .addInfo("The first two Overclocks:")
          .addInfo("-Reduce the Finish time to ${WHITE(0.5)}s and ${WHITE(0.25)}s")
          .addInfo("Subsequent Overclocks:")
          .addInfo("-Double the number of Jobs finished at once to a Max of ${WHITE(256)}")
          .addInfo("Use the screwdriver to right-click the Controller to open the config GUI")
          .addSeparator()
          .beginStructureBlock(5, 5, 5, true)
          .addController("Front center")
          .addCasingInfoMin("Robust Tungstensteel Machine Casing", MIN_CASING_COUNT, false)
          .addInputBus("Any casing", 1)
          .addEnergyHatch("Any casing", 1)
          .addMaintenanceHatch("Any casing", 1)
          .toolTipFinisher(PURPLE(MODNAME))
    }
  }

  override fun checkMachine(baseMetaTileEntity: IGregTechTileEntity?, stack: ItemStack?): Boolean {
    casing = 0
    return when {
      !checkPiece(
          STRUCTURE_PIECE_MAIN,
          STRUCTURE_HORIZONTAL_OFFSET,
          STRUCTURE_VERTICAL_OFFSET,
          STRUCTURE_DEPTH_OFFSET) -> false
      !checkHatches() -> false
      casing < MIN_CASING_COUNT -> false
      else -> true
    }
  }

  private fun checkHatches(): Boolean {
    return when {
      mMaintenanceHatches.size != 1 -> false
      mEnergyHatches.isEmpty() -> false
      else -> true
    }
  }

  override fun construct(itemStack: ItemStack?, hintsOnly: Boolean) {
    buildPiece(
        STRUCTURE_PIECE_MAIN,
        itemStack,
        hintsOnly,
        STRUCTURE_HORIZONTAL_OFFSET,
        STRUCTURE_VERTICAL_OFFSET,
        STRUCTURE_DEPTH_OFFSET)
  }

  override fun getStructureDefinition(): IStructureDefinition<LargeMolecularAssembler> =
      STRUCTURE_DEFINITION

  override fun addOutput(stack: ItemStack): Boolean {
    cachedOutputs.add(AEApi.instance().storage().createItemStack(stack))
    markDirty()
    return true
  }

  override fun onPostTick(baseMetaTileEntity: IGregTechTileEntity, tick: Long) {
    super.onPostTick(baseMetaTileEntity, tick)
    if (baseMetaTileEntity.isServerSide) {
      flushCachedOutputsIfNeeded(tick)
      saveAeJobsIfNeeded()
      syncAEProxyActive(baseMetaTileEntity)
      issuePatternChangeIfNeeded(tick)
    }
  }

  override fun onScrewdriverRightClick(
      side: ForgeDirection?,
      player: EntityPlayer?,
      x: Float,
      y: Float,
      z: Float
  ) {
    super.onScrewdriverRightClick(side, player, x, y, z)
    if (baseMetaTileEntity.isClientSide || player !is EntityPlayerMP) return
    if (side == baseMetaTileEntity.frontFacing) {
      network.sendTo(MessageSyncMetaTileEntityConfig(this, true), player)
    }
  }
  // endregion

  private inline fun withAeJobs(
      action: (dataOrb: ItemStack, aeJobs: LinkedList<ItemStack>) -> Unit
  ) {
    if (mInventory[1] === cachedDataOrb && cachedDataOrb != null) {
      action(cachedDataOrb!!, cachedAeJobs!!)
      return
    }
    if (!ItemList.Tool_DataOrb.isStackEqual(mInventory[1], false, true)) {
      cachedDataOrb = null
      cachedAeJobs = null
      return
    }
    val dataOrb = mInventory[1]
    var dataTitle: String = BehaviourDataOrb.getDataTitle(dataOrb)
    if (dataTitle.isEmpty()) {
      dataTitle = DATA_ORB_TITLE
      BehaviourDataOrb.setDataTitle(dataOrb, dataTitle)
      BehaviourDataOrb.setNBTInventory(dataOrb, emptyArray())
    }
    if (dataTitle != DATA_ORB_TITLE) {
      cachedDataOrb = null
      cachedAeJobs = null
      return
    }
    cachedDataOrb = dataOrb
    cachedAeJobs =
        BehaviourDataOrb.getNBTInventory(dataOrb).filterNotNull().toCollection(LinkedList())
    action(cachedDataOrb!!, cachedAeJobs!!)
  }

  private fun addCraftingFX(itemStack: ItemStack) {
    if (hiddenCraftingFX) return
    craftingDisplayPoint?.let { p ->
      network.sendToAllAround(
          MessageCraftingFX(
              p.x,
              p.y,
              p.z,
              mMaxProgresstime,
              AEApi.instance().storage().createItemStack(itemStack)),
          NetworkRegistry.TargetPoint(
              p.w.provider.dimensionId, p.x.toDouble(), p.y.toDouble(), p.z.toDouble(), 64.0))
    }
  }

  private fun getRequest(): BaseActionSource? {
    if (requestSource == null) requestSource = MachineSource(baseMetaTileEntity as IActionHost)
    return requestSource
  }

  private fun flushCachedOutputsIfNeeded(tick: Long) {
    tickCounter = tick
    if (tickCounter <= lastOutputTick + 40) return

    lastOutputFailed = true
    proxy?.let {
      try {
        val storage = it.storage.itemInventory
        for (s in cachedOutputs) {
          if (s.stackSize == 0L) continue
          val rest = Platform.poweredInsert(it.energy, storage, s, getRequest())
          if (rest != null && rest.stackSize > 0) {
            lastOutputFailed = true
            s.stackSize = rest.stackSize
            break
          }
          s.stackSize = 0
        }
      } catch (ignored: GridAccessException) {
        lastOutputFailed = true
      }
    }
    lastOutputTick = tickCounter
  }

  private fun saveAeJobsIfNeeded() {
    if (!aeJobsDirty) return
    withAeJobs { dataOrb, aeJobs ->
      BehaviourDataOrb.setNBTInventory(dataOrb, aeJobs.toTypedArray())
      markDirty()
      aeJobsDirty = false
    }
  }

  private fun issuePatternChangeIfNeeded(tick: Long) {
    if (tick % 20 != 0L) return
    compactedInputs.let { inputs ->
      if (storedInputs.equals(cachedRawPatterns)) return
      cachedRawPatterns = ArrayList(storedInputs)
      val patterns = inputs.filter { it.getPattern(baseMetaTileEntity.world)?.isCraftable == true }
      if (patterns == cachedPatterns) return
      cachedPatterns = patterns
      cachedPatternDetails = patterns.map { it.getPattern(baseMetaTileEntity.world)!! }
      proxy?.let {
        try {
          it.grid.postEvent(MENetworkCraftingPatternChange(this, it.node))
        } catch (ignored: GridAccessException) {
          // Do nothing
        }
      }
    }
  }

  private fun syncAEProxyActive(baseMetaTileEntity: IGregTechTileEntity) {
    if (gridProxy == null) {
      gridProxy =
          AENetworkProxy(this, "proxy", this.getStackForm(1), true).apply {
            setFlags(GridFlags.REQUIRE_CHANNEL)
          }
    }

    proxy?.run {
      if (baseMetaTileEntity.isActive) {
        if (!isReady) onReady()
      } else {
        if (isReady) invalidate()
      }
    }
  }

  private fun addToLargeMolecularAssemblerList(
      tileEntity: IGregTechTileEntity?,
      baseCasingIndex: Short
  ): Boolean {
    val casingIndex = baseCasingIndex.toInt()
    return when {
      addMaintenanceToMachineList(tileEntity, casingIndex) -> true
      addInputToMachineList(tileEntity, casingIndex) -> true
      addEnergyInputToMachineList(tileEntity, casingIndex) -> true
      else -> false
    }
  }

  // region IConfigurableMetaTileEntity
  override fun readConfigFromBytes(buf: ByteBuf) {
    hiddenCraftingFX = buf.readBoolean()
  }

  override fun writeConfigToBytes(buf: ByteBuf) {
    buf.writeBoolean(hiddenCraftingFX)
  }

  override fun getServerGuiElement(ID: Int, player: EntityPlayer?): Any {
    return ContainerConfigLargeMolecularAssembler(this)
  }

  override fun getClientGuiElement(ID: Int, player: EntityPlayer?): Any {
    return GuiConfigLargeMolecularAssembler(ContainerConfigLargeMolecularAssembler(this))
  }
  // endregion

  // region ICraftingProvider
  override fun pushPattern(
      patternDetails: ICraftingPatternDetails,
      table: InventoryCrafting
  ): Boolean {
    withAeJobs { _, aeJobs ->
      if (aeJobs.size < 256) {
        aeJobs.add(patternDetails.getOutput(table, baseMetaTileEntity.world))
        aeJobsDirty = true
        return true
      }
    }
    return false
  }

  override fun isBusy(): Boolean {
    withAeJobs { _, aeJobs -> if (aeJobs.size < 256) return false }
    return true
  }

  override fun provideCrafting(craftingTracker: ICraftingProviderHelper) {
    if (proxy?.isReady == true) {
      cachedPatternDetails.forEach { craftingTracker.addCraftingOption(this, it) }
    }
  }
  // endregion

  // region IActionHost, IGridProxyable
  private var gridProxy: AENetworkProxy? = null

  override fun getProxy(): AENetworkProxy? {
    return gridProxy
  }

  override fun getGridNode(dir: ForgeDirection?): IGridNode? {
    return this.proxy?.node
  }

  override fun securityBreak() {
    baseMetaTileEntity.disableWorking()
  }

  override fun getLocation(): DimensionalCoord {
    return DimensionalCoord(
        baseMetaTileEntity.world,
        baseMetaTileEntity.xCoord,
        baseMetaTileEntity.yCoord.toInt(),
        baseMetaTileEntity.zCoord)
  }

  override fun getActionableNode(): IGridNode? {
    return this.proxy?.node
  }
  // endregion

  companion object {
    private const val MACHINE_TYPE = "Molecular Assembler"
    private const val EU_PER_TICK_BASIC = 16
    private const val EU_PER_TICK_CRAFTING = 64
    private const val CASING_INDEX = 48
    private const val MIN_CASING_COUNT = 24
    private const val DATA_ORB_TITLE = "AE-JOBS"
    private const val NBT_KEY_CACHED_OUTPUTS = "cachedOutputs"
    private const val NBT_KEY_CONFIG_HIDDEN_CRAFTING_FX = "config:hiddenCraftingFX"
    private const val STRUCTURE_HORIZONTAL_OFFSET = 2
    private const val STRUCTURE_VERTICAL_OFFSET = 4
    private const val STRUCTURE_DEPTH_OFFSET = 0
    private const val STRUCTURE_PIECE_MAIN = "main"

    // region STRUCTURE_DEFINITION
    private val STRUCTURE_DEFINITION =
        StructureDefinition.builder<LargeMolecularAssembler>()
            .addShape(
                STRUCTURE_PIECE_MAIN,
                transpose(
                    arrayOf(
                        arrayOf("CCCCC", "CGGGC", "CGGGC", "CGGGC", "CCCCC"),
                        arrayOf("CGGGC", "G---G", "G---G", "G---G", "CGGGC"),
                        arrayOf("CGGGC", "G---G", "G-X-G", "G---G", "CGGGC"),
                        arrayOf("CGGGC", "G---G", "G---G", "G---G", "CGGGC"),
                        arrayOf("CC~CC", "CGGGC", "CGGGC", "CGGGC", "CCCCC"),
                    )))
            .addElement(
                'C',
                ofChain(
                    ofHatchAdder(
                        LargeMolecularAssembler::addToLargeMolecularAssemblerList, CASING_INDEX, 1),
                    onElementPass({ it.casing++ }, ofBlock(GregTechAPI.sBlockCasings4, 0)),
                ))
            .addElement(
                'G',
                ofBlockAnyMeta(
                    AEApi.instance()
                        .definitions()
                        .blocks()
                        .quartzVibrantGlass()
                        .maybeBlock()
                        .get()))
            .addElement(
                'X',
                IStructureElementCheckOnly { it, w, x, y, z ->
                  when {
                    w.isAirBlock(x, y, z) -> {
                      it.craftingDisplayPoint = CraftingDisplayPoint(w, x, y, z)
                      true
                    }
                    else -> false
                  }
                })
            .build()
    // endregion

    private data class CraftingDisplayPoint(val w: World, val x: Int, val y: Int, val z: Int)

    // region IItemList<IAEItemStack> NBT
    private fun IItemList<IAEItemStack>.saveNBTData(nbt: NBTTagCompound, key: String) {
      val isList = NBTTagList()
      this.forEach { aeIS ->
        if (aeIS.stackSize <= 0) return@forEach
        val tag = NBTTagCompound()
        val isTag = NBTTagCompound()
        aeIS.itemStack.writeToNBT(isTag)
        tag.setTag("itemStack", isTag)
        tag.setLong("size", aeIS.stackSize)
        isList.appendTag(tag)
      }
      nbt.setTag(key, isList)
    }

    private fun IItemList<IAEItemStack>.loadNBTData(nbt: NBTTagCompound, key: String) {
      val isList = nbt.getTag(key)
      if (isList !is NBTTagList) return
      repeat(isList.tagCount()) {
        val tag = isList.getCompoundTagAt(it)
        val isTag = tag.getCompoundTag("itemStack")
        val size = tag.getLong("size")
        val itemStack = GTUtility.loadItem(isTag)
        val aeIS = AEApi.instance().storage().createItemStack(itemStack)
        aeIS.stackSize = size
        this.add(aeIS)
      }
    }
    // endregion

    private fun ItemStack.getPattern(w: World): ICraftingPatternDetails? {
      val item = this.item
      if (item !is ItemEncodedPattern) return null
      return item.getPatternForItem(this, w)
    }
  }
}
