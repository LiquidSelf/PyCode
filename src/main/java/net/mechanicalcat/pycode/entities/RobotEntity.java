package net.mechanicalcat.pycode.entities;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.World;

public class RobotEntity extends EntityMob
{
    public RobotEntity(World worldIn)
    {
        super(worldIn);
        this.setSize(0.8F, 2F);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
    }

    @Override
    public boolean getAlwaysRenderNameTagForRender()
    {
        return true;
    }
}