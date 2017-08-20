/*
 * Copyright (c) 2017 Richard Jones <richard@mechanicalcat.net>
 * All Rights Reserved
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.mechanicalcat.pycode.proxy;

import net.mechanicalcat.pycode.init.ModBlocks;
import net.mechanicalcat.pycode.init.ModEntities;
import net.mechanicalcat.pycode.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ClientProxy extends ServerProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent e)
    {
        super.preInit(e);
        ModEntities.registerRenders();
    }

    @Override
    public void init(FMLInitializationEvent e)
    {
        super.init(e);
        ModItems.registerRenders();
        ModBlocks.registerRenders();
    }

    @Override
    public void postInit(FMLPostInitializationEvent e)
    {
        super.postInit(e);
    }

    @Override
    public EntityPlayer getEntityPlayer(MessageContext ctx)
    {
        return ctx.side == Side.CLIENT ? Minecraft.getMinecraft().player : super.getEntityPlayer(ctx);
    }
}