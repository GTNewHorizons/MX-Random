package com.muxiu1997.mxrandom.api.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public interface IMessageServerSideHandler<REQ extends IMessage, REPLY extends IMessage>
        extends IMessageHandler<REQ, REPLY> {

    @Override
    default REPLY onMessage(REQ message, MessageContext ctx) {
        if (ctx.side == Side.CLIENT) {
            throw new IllegalAccessError("Cannot handle client-side messages");
        }
        return handleServerSideMessage(message, ctx);
    }

    REPLY handleServerSideMessage(REQ message, MessageContext ctx);
}
