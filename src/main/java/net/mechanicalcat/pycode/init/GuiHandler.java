package net.mechanicalcat.pycode.init;

import net.mechanicalcat.pycode.gui.GuiPythonBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public final class GuiHandler implements IGuiHandler
{
    private final int GUI_BOOK = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer entityPlayer, World world, int x, int y, int z)
    {
        switch (ID)
        {
            default: return null;
        }
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer entityPlayer, World world, int x, int y, int z)
    {
        ItemStack book = null;

        if (entityPlayer.inventory.getCurrentItem() != null)
        {
            book = entityPlayer.inventory.getCurrentItem();
        }

        switch (ID)
        {
            case GUI_BOOK: return new GuiPythonBook(entityPlayer, book);
            default: return null;
        }
    }
}