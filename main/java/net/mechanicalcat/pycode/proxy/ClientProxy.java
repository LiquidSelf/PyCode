package net.mechanicalcat.pycode.proxy;

import net.mechanicalcat.pycode.events.PyCodeEventHandler;
import net.mechanicalcat.pycode.init.ModBlocks;
import net.mechanicalcat.pycode.init.ModEntities;
import net.mechanicalcat.pycode.init.ModItems;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy implements CommonProxy {
    private PyCodeEventHandler handler = new PyCodeEventHandler();

    @Override
    public void preInit() {
//        MinecraftForge.EVENT_BUS.register(handler);

        // unlike the other renders, this has to be registered preInit or it just fails without error
        ModEntities.registerRenders();
    }

    @Override
    public void init() {
        ModItems.registerRenders();
        ModBlocks.registerRenders();
    }

    @Override
    public void postInit() {

    }
}
