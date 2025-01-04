package com.muxiu1997.mxrandom.loader;

import com.muxiu1997.mxrandom.MXRandom;
import com.muxiu1997.mxrandom.network.message.MessageCraftingFX;

import cpw.mods.fml.relauncher.Side;

public class NetworkLoader {

    public static void load() {
        MXRandom.network.registerMessage(new MessageCraftingFX.Handler(), MessageCraftingFX.class, 0, Side.CLIENT);
    }
}
