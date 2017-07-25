package net.mechanicalcat.pycode.network;

import net.mechanicalcat.pycode.PyCode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author Ivasik
 * @param <REQ> - package
 */
public abstract class AbstractPacket<REQ extends IMessage> implements IMessage, IMessageHandler<REQ, REQ>
{
    @Override
    public REQ onMessage(final REQ message, final MessageContext ctx)
    {
        if(ctx.side == Side.SERVER)
        {
            handleServerSide((EntityPlayerMP) PyCode.proxy.getEntityPlayer(ctx));
        }
        else
        {
            handleClientSide(PyCode.proxy.getEntityPlayer(ctx));
        }
        return null;
    }

    public abstract void handleClientSide(final EntityPlayer player);
    public abstract void handleServerSide(final EntityPlayerMP player);
}