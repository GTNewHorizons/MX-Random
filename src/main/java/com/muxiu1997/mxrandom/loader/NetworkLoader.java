package com.muxiu1997.mxrandom.loader;

import com.muxiu1997.mxrandom.MXRandom;
import com.muxiu1997.mxrandom.network.GuiHandler;
import com.muxiu1997.mxrandom.network.message.MessageCraftingFX;
import com.muxiu1997.mxrandom.network.message.MessageSyncMetaTileEntityConfig;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

public class NetworkLoader {

    public static void load() {
        MXRandom.network.registerMessage(MessageCraftingFX.Handler.class, MessageCraftingFX.class, 0, Side.CLIENT);
        MXRandom.network.registerMessage(
                MessageSyncMetaTileEntityConfig.Handler.class,
                MessageSyncMetaTileEntityConfig.class,
                1,
                Side.SERVER);
        MXRandom.network.registerMessage(
                MessageSyncMetaTileEntityConfig.Handler.class,
                MessageSyncMetaTileEntityConfig.class,
                1,
                Side.CLIENT);

        NetworkRegistry.INSTANCE.registerGuiHandler(MXRandom.MODID, new GuiHandler());
    }
}
