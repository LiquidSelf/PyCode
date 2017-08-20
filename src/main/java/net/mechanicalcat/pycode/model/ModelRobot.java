package net.mechanicalcat.pycode.model;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelRobot extends ModelPlayer
{
    private ModelRenderer robot_antenna;

    public ModelRobot()
    {
        super(0, true);
        textureWidth = 64;
        textureHeight = 64;

        robot_antenna = new ModelRenderer(this, 32, 48);
        robot_antenna.addBox(-3.0F, -6.0F, -1.0F, 6, 6, 1, 0);
    }

    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        robot_antenna.render(scale);
    }
}