package net.mechanicalcat.pycode.network;

import io.netty.buffer.ByteBuf;
import net.mechanicalcat.pycode.init.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * @author Ivasik
 */
public final class SPacketUpdateNBT extends AbstractPacket<SPacketUpdateNBT>
{
    private static ItemStack stack;

    public SPacketUpdateNBT(){}
    public SPacketUpdateNBT(ItemStack stack)
    {
        this.stack = stack;
    }

    @Override
    public void handleClientSide(EntityPlayer player) {}

    @Override
    public void handleServerSide(EntityPlayerMP player)
    {
        try {
            ItemStack itemstack = this.stack;

            if (itemstack.isEmpty()) return;

            if (!ItemWritableBook.isNBTValid(itemstack.getTagCompound()))
            {
                FMLLog.log.error("Invalid book tag!");
            }

            ItemStack itemstack1 = player.getHeldItemMainhand();

            if (itemstack1.isEmpty()) return;

            if (itemstack.getItem() == ModItems.python_book && itemstack.getItem() == itemstack1.getItem())
            {
                itemstack1.setTagInfo("title", new NBTTagString(itemstack.getTagCompound().getString("title")));
                itemstack1.setTagInfo("pages", itemstack.getTagCompound().getTagList("pages", 8));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void fromBytes(ByteBuf byteBuf)
    {
        this.stack = ByteBufUtils.readItemStack(byteBuf);
    }

    @Override
    public void toBytes(ByteBuf byteBuf)
    {
        ByteBufUtils.writeItemStack(byteBuf, this.stack);
    }
}