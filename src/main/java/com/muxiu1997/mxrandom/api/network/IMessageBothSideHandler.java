package com.muxiu1997.mxrandom.api.network;

import org.jetbrains.annotations.NotNull;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public interface IMessageBothSideHandler<REQ extends IMessage, REPLY extends IMessage>
        extends IMessageServerSideHandler<REQ, REPLY>, IMessageClientSideHandler<REQ, REPLY> {

    @Override
    default REPLY onMessage(@NotNull REQ message, @NotNull MessageContext ctx) {
        if (ctx.side == Side.SERVER) {
            return handleServerSideMessage(message, ctx);
        } else {
            return handleClientSideMessage(message, ctx);
        }
    }
}
