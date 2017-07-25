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

package net.mechanicalcat.pycode.items;

import net.mechanicalcat.pycode.PyCode;
import net.mechanicalcat.pycode.Reference;
import net.mechanicalcat.pycode.entities.HandEntity;
import net.mechanicalcat.pycode.script.PythonCode;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public final class HandItem extends Item
{
    public HandItem()
    {
        this.setUnlocalizedName(Reference.PyCodeRegistrations.HAND.getUnlocalizedName());
        this.setRegistryName(Reference.PyCodeRegistrations.HAND.getRegistryName());
        this.setCreativeTab(PyCode.PYTAB);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) return;
        if (compound.hasKey(PythonCode.CODE_NBT_TAG)) tooltip.add(I18n.format("item.hand.tooltip.info"));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote) return EnumActionResult.PASS;

        ItemStack stack = player.getHeldItem(hand);

        if (stack.getMaxStackSize() != 0)
        {
            float yaw = player.getHorizontalFacing().getHorizontalAngle();
            NBTTagCompound compound = stack.getTagCompound();
            HandEntity entity = new HandEntity(world, compound, pos.getX() + .5, pos.getY() + 1.0, pos.getZ() + .5, yaw);
            world.spawnEntity(entity);
            stack.shrink(1);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }
}