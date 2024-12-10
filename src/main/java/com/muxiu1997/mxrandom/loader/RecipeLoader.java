package com.muxiu1997.mxrandom.loader;

import static gregtech.api.enums.Mods.AppliedEnergistics2;
import static gregtech.api.recipe.RecipeMaps.assemblerRecipes;
import static gregtech.api.util.GTModHandler.getModItem;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.TierEU;

public class RecipeLoader {

    public static void load() {
        GTValues.RA.stdBuilder()
                .itemInputs(
                        getModItem(AppliedEnergistics2.ID, "tile.BlockInterface", 8, 0),
                        getModItem(AppliedEnergistics2.ID, "tile.BlockMolecularAssembler", 8, 0),
                        ItemList.Emitter_IV.get(4L),
                        ItemList.Casing_RobustTungstenSteel.get(1L))
                .itemOutputs(GTMetaTileEntityLoader.largeMolecularAssembler.getStackForm(1))
                .fluidInputs(Materials.Plastic.getMolten(1296)).duration(60 * SECONDS).eut(TierEU.RECIPE_IV)
                .addTo(assemblerRecipes);
    }
}
