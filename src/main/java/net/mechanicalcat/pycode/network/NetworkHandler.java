package net.mechanicalcat.pycode.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author Ivasik
 */
public final class NetworkHandler
{
    public static final NetworkHandler INSTANCE = new NetworkHandler();
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("PyCodeChannel");
    private int dec;

    public NetworkHandler(){}

    public void register()
    {
        registerMessage(SPacketUpdateNBT.class, Side.SERVER);
        registerMessage(SPacketInvokeWand.class, Side.SERVER);
    }

    public void sendToServer(final IMessage message)
    {
        NETWORK.sendToServer(message);
    }

    private void registerMessage(final Class packet, final Side side)
    {
        try
        {
            packet.getDeclaredConstructor();
            NETWORK.registerMessage(packet, packet, dec++, side);
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
    }
}