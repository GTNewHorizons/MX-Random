package com.muxiu1997.mxrandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.muxiu1997.mxrandom.Tags;
import com.muxiu1997.mxrandom.loader.GTMetaTileEntityLoader;
import com.muxiu1997.mxrandom.loader.NetworkLoader;
import com.muxiu1997.mxrandom.loader.RecipeLoader;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(
        modid = MXRandom.MODID,
        name = MXRandom.MODNAME,
        version = Tags.VERSION,
        dependencies = "required-after:appliedenergistics2;" + "required-after:gregtech")
public class MXRandom {

    public static final String MODID = "mxrandom";
    public static final String MODNAME = "MX-Random";

    public static Logger logger = LogManager.getLogger(MODID);
    public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
        GTMetaTileEntityLoader.load();
        NetworkLoader.load();
    }

    @EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        RecipeLoader.load();
    }
}
