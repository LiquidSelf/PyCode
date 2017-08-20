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

package net.mechanicalcat.pycode.init;


import net.mechanicalcat.pycode.PyCode;
import net.mechanicalcat.pycode.Reference;
import net.mechanicalcat.pycode.entities.HandEntity;
import net.mechanicalcat.pycode.entities.RobotEntity;
import net.mechanicalcat.pycode.render.RenderHand;
import net.mechanicalcat.pycode.render.RenderRobot;
import net.mechanicalcat.pycode.tileentity.PyCodeBlockTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModEntities
{
    private static int entityCount = 0;

    public static void register()
    {
        registerEntity(HandEntity.class, "python_hand");
        registerEntity(RobotEntity.class, "python_robot");

        registerTileEntity(PyCodeBlockTileEntity.class, "PyCodeBlockTileEntity");
    }

    public static void registerRenders()
    {
        RenderingRegistry.registerEntityRenderingHandler(HandEntity.class, RenderHand::new);
        RenderingRegistry.registerEntityRenderingHandler(RobotEntity.class, manager -> new RenderRobot(manager));
    }

    private static void registerEntity(Class entity, String name)
    {
        EntityRegistry.registerModEntity(new ResourceLocation("pycode", name), entity, name, entityCount++, PyCode.instance, 48, 3, true);
    }

    private static void registerTileEntity(Class tileEntity, String name)
    {
        GameRegistry.registerTileEntity(tileEntity, Reference.MODID + name);
    }
}