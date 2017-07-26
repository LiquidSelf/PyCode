package net.mechanicalcat.pycode.network;

import io.netty.buffer.ByteBuf;
import net.mechanicalcat.pycode.init.ModConfiguration;
import net.mechanicalcat.pycode.items.PythonWandItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLLog;

public final class SPacketInvokeWand extends AbstractPacket<SPacketInvokeWand>
{
    private static int entityId;
    private static Vec3d hitVec;
    private static RayTraceResult.Type typeOfHit;
    private static BlockPos blockPos;
    private static EnumFacing sideHit;
    private static RayTraceResult traceResult;

    public SPacketInvokeWand(){}
    public SPacketInvokeWand(RayTraceResult traceResult)
    {
        this.traceResult = traceResult;
    }

    @Override
    public void handleClientSide(EntityPlayer player){}

    @Override
    public void handleServerSide(EntityPlayerMP playerMP)
    {
        EntityPlayer player = playerMP;

        switch (this.typeOfHit)
        {
            case BLOCK:
                if (ModConfiguration.isDebug())
                {
                    String info = String.format("Got a InvokeWandMessage block=%s", this.blockPos);
                    FMLLog.log.info(info);
                }

                PythonWandItem.invokeOnBlock(player, this.blockPos);
            case ENTITY:
                Entity entity = player.world.getEntityByID(this.entityId);

                if (ModConfiguration.isDebug())
                {
                    String info = String.format("Got a InvokeWandMessage entity=%s", entity);
                    FMLLog.log.info(info);
                }

                if (entity == null) return;

                PythonWandItem.invokeOnEntity(player, entity);
                break;
            case MISS:
                PythonWandItem.invokeInDirection(player, this.hitVec);
        }
    }

    @Override
    public void fromBytes(ByteBuf byteBuf)
    {
        hitVec = new Vec3d(byteBuf.readDouble(), byteBuf.readDouble(), byteBuf.readDouble());
        typeOfHit = RayTraceResult.Type.MISS;

        switch (byteBuf.readShort())
        {
            case 1:
                typeOfHit = RayTraceResult.Type.BLOCK;
                blockPos = new BlockPos(byteBuf.readInt(), byteBuf.readInt(), byteBuf.readInt());
                sideHit = EnumFacing.values()[byteBuf.readShort()];
                break;
            case 2:
                typeOfHit = RayTraceResult.Type.ENTITY;
                entityId = byteBuf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf byteBuf)
    {
        byteBuf.writeDouble(this.traceResult.hitVec.x);
        byteBuf.writeDouble(this.traceResult.hitVec.y);
        byteBuf.writeDouble(this.traceResult.hitVec.z);
        byteBuf.writeShort(this.traceResult.typeOfHit.ordinal());

        switch (this.traceResult.typeOfHit)
        {
            case ENTITY:
                byteBuf.writeInt(this.traceResult.entityHit.getEntityId());
                break;
            case BLOCK:
                BlockPos blockPos = this.traceResult.getBlockPos();
                byteBuf.writeInt(blockPos.getX());
                byteBuf.writeInt(blockPos.getY());
                byteBuf.writeInt(blockPos.getZ());
                byteBuf.writeShort(this.traceResult.sideHit.ordinal());
        }
    }
}