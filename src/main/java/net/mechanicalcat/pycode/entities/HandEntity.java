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

package net.mechanicalcat.pycode.entities;


import net.mechanicalcat.pycode.init.ModItems;
import net.mechanicalcat.pycode.items.PythonBookItem;
import net.mechanicalcat.pycode.items.PythonWandItem;
import net.mechanicalcat.pycode.script.HandMethods;
import net.mechanicalcat.pycode.script.IHasPythonCode;
import net.mechanicalcat.pycode.script.MyEntityPlayer;
import net.mechanicalcat.pycode.script.PythonCode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;

import javax.annotation.Nullable;

public class HandEntity extends Entity implements IHasPythonCode
{
    private static final DataParameter<String> CODE = EntityDataManager.createKey(HandEntity.class, DataSerializers.STRING);
    private static net.minecraftforge.common.IMinecartCollisionHandler collisionHandler = null;
    public PythonCode code;

    public HandEntity(World worldIn)
    {
        super(worldIn);
        this.noClip = true;
    }

    public HandEntity(World worldIn, @Nullable NBTTagCompound compound, double x, double y, double z, float yaw)
    {
        this(worldIn);
        if (compound != null) this.readEntityFromNBT(compound);

        this.setPositionAndRotation(x, y, z, yaw, 0);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
    }

    protected void entityInit()
    {
        this.preventEntitySpawning = true;
        this.isImmuneToFire = true;
        this.setSize(0.98F, 0.7F);
        this.dataManager.register(CODE, "");
        this.initCode();
    }

    public void notifyDataManagerChange(DataParameter<?> key)
    {
        super.notifyDataManagerChange(key);

        if (CODE.equals(key)) this.code.setCodeString(this.dataManager.get(CODE));
    }

    public void initCode()
    {
        this.code = new PythonCode();
        this.code.setCodeString(this.dataManager.get(CODE));
        this.code.setContext(this.world, this, this.getPosition());
    }

    protected void writeEntityToNBT(NBTTagCompound compound)
    {
        this.code.writeToNBT(compound);
    }

    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        this.code.readFromNBT(compound);
        this.dataManager.set(CODE, this.code.getCode());
        this.code.put("hand", new HandMethods(this));
        this.code.setContext(this.world, this, this.getPosition());
    }

    @Override
    public boolean getAlwaysRenderNameTag()
    {
        return true;
    }

    @Override
    public boolean hasCustomName()
    {
        return this.code.hasCode();
    }

    public BlockPos getFacedPos()
    {
        return getPosition().offset(getHorizontalFacing());
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote)
        {
            if (stack.isEmpty())
            {
                if (this.code.hasKey("run"))
                {
                    this.code.put("hand", new HandMethods(this));
                    this.code.setRunner(player);
                    this.code.invoke("run", new MyEntityPlayer(player));
                    return true;
                }
            }
        }

        Item item = stack.getItem();
        if (item instanceof PythonWandItem)
        {
            PythonWandItem.invokeOnEntity(player, this);
        }
        else if (item instanceof PythonBookItem || item instanceof ItemWritableBook)
        {
            BlockPos pos = this.getPosition();
            this.code.put("hand", new HandMethods(this));
            this.code.setCodeFromBook(this.getEntityWorld(), player, this, pos, stack);
            PythonBookItem bookItem = (PythonBookItem) item;
            bookItem.itemInteract(stack, this);
        }
        return true;
    }

    public void moveForward(float distance)
    {
        Vec3d pos = this.getPositionVector();
        float f1 = -MathHelper.sin(this.rotationYaw * 0.017453292F);
        float f2 = MathHelper.cos(this.rotationYaw * 0.017453292F);
        pos = pos.addVector(distance * f1, 0, distance * f2);
        this.setPosition(pos.x, pos.y, pos.z);
    }

    public void setYaw(float angle)
    {
        this.rotationYaw = angle % 360;
    }

    public void moveYaw(float angle)
    {
        this.rotationYaw = (this.rotationYaw + angle) % 360;
    }

    public boolean canBeCollidedWith()
    {
        return true;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (!this.world.isRemote && !this.isDead)
        {
            if (this.isEntityInvulnerable(source))
            {
                return false;
            }
            else
            {
                this.setBeenAttacked();
                this.removePassengers();
                this.setDead();

                if (this.world.getGameRules().getBoolean("doEntityDrops"))
                {
                    ItemStack itemstack = new ItemStack(ModItems.python_hand, 1);
                    itemstack.setStackDisplayName(this.getName());

                    if (!itemstack.hasTagCompound())
                    {
                        itemstack.setTagCompound(new NBTTagCompound());
                    }

                    NBTTagCompound compound = itemstack.getTagCompound();
                    if (compound == null)
                    {
                        FMLLog.log.warn("Python Hand itemstack NBT missing??");
                    }
                    else
                    {
                        this.writeToNBT(compound);
                    }
                    this.entityDropItem(itemstack, 0.0F);
                }

                return true;
            }
        }
        return true;
    }

    public void onUpdate()
    {
        if (this.posY < -64.0D)
        {
            this.onKillCommand();
        }
    }
}