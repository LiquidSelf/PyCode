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
import net.mechanicalcat.pycode.init.ModConfiguration;
import net.mechanicalcat.pycode.init.ModItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public final class PythonBookItem extends Item
{
    public List<String> pages;

    public PythonBookItem()
    {
        this.setRegistryName(Reference.PyCodeRegistrations.BOOK.getRegistryName());
        this.setUnlocalizedName(Reference.PyCodeRegistrations.BOOK.getUnlocalizedName());
        this.setCreativeTab(PyCode.PYTAB);
        this.setMaxStackSize(1);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack stack = playerIn.getHeldItem(hand);

        if (ModConfiguration.isDebug())
        {
            String info = String.format("Book onItemRightClick stack=%s, hand=%s", stack, hand);
            FMLLog.log.info(info);
        }

        if (hand == EnumHand.OFF_HAND) return new ActionResult(EnumActionResult.FAIL, stack);

        if (world.isRemote)
        {
            playerIn.openGui(PyCode.instance, 0, playerIn.world, 0, 0, 0);
        }
        return new ActionResult(EnumActionResult.SUCCESS, stack);
    }

    public boolean itemInteract(ItemStack stack, HandEntity handEntity)
    {
        if (handEntity != null)
        {
            if (stack.getItem() == ModItems.python_book)
            {
                NBTTagCompound tagCompound = stack.getTagCompound();

                if (tagCompound == null) return false;

                if (tagCompound.hasKey("title"))
                {
                    if (tagCompound.getString("title").isEmpty())
                    {
                        handEntity.setCustomNameTag(I18n.format("item.hand.tooltip.info"));
                    }
                    else
                    {
                        handEntity.setCustomNameTag(tagCompound.getString("title"));
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        NBTTagCompound compound = stack.getTagCompound();

        if (compound == null) return;

        if (compound.hasKey("title"))
        {
            String title = compound.getString("title");

            if (!title.isEmpty()) tooltip.add(title);
        }
    }
}