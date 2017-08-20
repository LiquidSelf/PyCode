package net.mechanicalcat.pycode.render;

import net.mechanicalcat.pycode.Reference;
import net.mechanicalcat.pycode.entities.RobotEntity;
import net.mechanicalcat.pycode.model.ModelRobot;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderRobot extends RenderBiped<RobotEntity>
{
    public RenderRobot(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelRobot(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(RobotEntity entity)
    {
        return new ResourceLocation(Reference.MODID, "textures/entities/robot.png");
    }
}
