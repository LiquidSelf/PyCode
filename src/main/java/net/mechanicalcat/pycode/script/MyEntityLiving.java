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

package net.mechanicalcat.pycode.script;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;

public class MyEntityLiving extends MyEntity
{
    public MyEntityLiving(EntityLivingBase entity)
    {
        super(entity);
    }

    public boolean isMob()
    {
        return true;
    }

    public void potion(String effect)
    {
        if (this.entity.world.isRemote) return;

        Potion potion = Potion.REGISTRY.getObject(new ResourceLocation(effect));

        if (potion == null)
        {
            FMLLog.log.error("Unknown potion name '%s'", effect);
            return;
        }

        PotionEffect potioneffect = new PotionEffect(potion, 50, 1);
        EntityLivingBase entitylivingbase = (EntityLivingBase)this.entity;

        if (potion.isInstant())
        {
            potion.affectEntity(null, null, entitylivingbase, potioneffect.getAmplifier(), 0.5D);
        }
        else
        {
            entitylivingbase.addPotionEffect(potioneffect);
        }
    }
}