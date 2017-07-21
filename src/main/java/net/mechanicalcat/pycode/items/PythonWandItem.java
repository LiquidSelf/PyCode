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
import net.mechanicalcat.pycode.net.InvokeWandMessage;
import net.mechanicalcat.pycode.net.ModNetwork;
import net.mechanicalcat.pycode.script.MyBlock;
import net.mechanicalcat.pycode.script.PyRegistry;
import net.mechanicalcat.pycode.script.PythonCode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PythonWandItem extends Item
{
    public PythonWandItem()
    {
        this.setUnlocalizedName(Reference.PyCodeRegistrations.WAND.getUnlocalizedName());
        this.setRegistryName(Reference.PyCodeRegistrations.WAND.getRegistryName());
        this.setCreativeTab(PyCode.PYTAB);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
    {
        target.setFire(4);
        return true;
    }

    @Nullable
    private static PythonCode getCodeFromBook(EntityPlayer player)
    {
        ItemStack offhand = player.getHeldItemOffhand();

        if (offhand == ItemStack.EMPTY)
        {
            FMLLog.log.info("... nothing in off hand so pass");
            return null;
        }

        Item offitem = offhand.getItem();
        if (offitem instanceof PythonBookItem || offitem instanceof ItemWritableBook)
        {
            String content = PythonCode.bookAsString(offhand);

            if (content == null)
            {
                PythonCode.failz0r(player.world, player.getPosition(), "Could not get pages from the book!?");
                return null;
            }

            PythonCode code = new PythonCode();
            code.setCodeString(content);

            code.setContext(player.world, player, player.getPosition());
            return code;
        }
        return null;
    }

    public static void invokeOnBlock(EntityPlayer player, BlockPos pos)
    {
        PythonCode code = getCodeFromBook(player);
        if (code == null) return;
        IBlockState state = player.world.getBlockState(pos);
        if (code.hasKey("invoke")) code.invoke("invoke", new MyBlock(state, pos));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (world == null || world.isRemote) return EnumActionResult.SUCCESS;
        FMLLog.log.info("Wand onItemUse stack=%s, hand=%s", player.getHeldItem(hand), hand);
        invokeOnBlock(player, pos);
        return EnumActionResult.SUCCESS;
    }

    public static void invokeOnEntity(EntityPlayer player, Entity entity)
    {
        FMLLog.log.info("Wand invokeOnEntity%s, entity=%s", player, entity);
        PythonCode code = getCodeFromBook(player);
        if (code == null) return;
        if (code.hasKey("invoke")) code.invoke("invoke", PyRegistry.myWrapper(player.world, entity));
    }

    public static void invokeInDirection(EntityPlayer player, Vec3d vec)
    {
        PythonCode code = getCodeFromBook(player);
        if (code == null) return;
        code.invoke("invoke", null);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        if (playerIn.world.isRemote)
        {
            FMLLog.log.info("Wand onItemRightClick stack=%s, hand=%s", playerIn.getHeldItem(handIn), handIn);
            RayTraceResult target = Minecraft.getMinecraft().objectMouseOver;
            ModNetwork.network.sendToServer(new InvokeWandMessage(target));
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }
}