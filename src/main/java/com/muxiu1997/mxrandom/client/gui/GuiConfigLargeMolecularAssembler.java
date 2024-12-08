package com.muxiu1997.mxrandom.client.gui;

import java.awt.Color;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import com.muxiu1997.mxrandom.MXRandom;
import com.muxiu1997.mxrandom.metatileentity.MTELargeMolecularAssembler;
import com.muxiu1997.mxrandom.network.container.ContainerConfigLargeMolecularAssembler;

public class GuiConfigLargeMolecularAssembler extends GuiContainer {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(
            MXRandom.MODID,
            "textures/gui/configMetaTileEntity.png");
    private static final String LANG_CRAFTING_FX = "mxrandom.client.gui.GuiConfigLargeMolecularAssembler.craftingFX";
    private static final String LANG_VISIBLE = "mxrandom.client.gui.GuiConfigLargeMolecularAssembler.visible";
    private static final String LANG_HIDDEN = "mxrandom.client.gui.GuiConfigLargeMolecularAssembler.hidden";

    private static final int PADDING = 8;
    private static final int BUTTON_WIDTH = 40;
    private static final int BUTTON_HEIGHT = 18;

    private GuiButton buttonToggleCraftingFX;
    private final MTELargeMolecularAssembler LMA;

    public GuiConfigLargeMolecularAssembler(ContainerConfigLargeMolecularAssembler container) {
        super(container);
        this.LMA = container.LMA;
        this.xSize = 176;
        this.ySize = 107;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonToggleCraftingFX = new GuiButton(
                0,
                guiLeft + xSize - BUTTON_WIDTH - PADDING,
                guiTop + PADDING + 16,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                buttonToggleCraftingFXDisplayString());
        buttonList.add(buttonToggleCraftingFX);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        buttonToggleCraftingFX.displayString = buttonToggleCraftingFXDisplayString();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(LMA.getLocalName(), PADDING, PADDING, Color.BLACK.getRGB());
        fontRendererObj.drawString(
                StatCollector.translateToLocal(LANG_CRAFTING_FX),
                PADDING,
                PADDING + 20,
                Color.BLACK.getRGB());
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == buttonToggleCraftingFX) {
            LMA.hiddenCraftingFX = !LMA.hiddenCraftingFX;
            LMA.applyConfigChanges();
        }
    }

    private String buttonToggleCraftingFXDisplayString() {
        if (LMA.hiddenCraftingFX) {
            return StatCollector.translateToLocal(LANG_HIDDEN);
        }
        return StatCollector.translateToLocal(LANG_VISIBLE);
    }
}
