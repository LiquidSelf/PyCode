package net.mechanicalcat.pycode.script;

import net.mechanicalcat.pycode.PythonEngine;
import net.mechanicalcat.pycode.init.ModItems;
import net.mechanicalcat.pycode.items.PythonBookItem;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLLog;
import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;

import javax.script.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PythonCode {
    private String code = "";
    private boolean codeChanged = false;
    private SimpleScriptContext context;
    private Bindings bindings;
    private World world = null;
    public static String CODE_NBT_TAG = "code";

    public PythonCode() {
        this.context = new SimpleScriptContext();
        this.bindings = new SimpleBindings();
        this.context.setBindings(this.bindings, ScriptContext.ENGINE_SCOPE);
    }

    public String getCode() {return code;}

    public boolean hasCode() {return !code.isEmpty();}

    public void check(String code) throws ScriptException {
        PythonEngine.compile(code);
    }

    public void setCodeString(String code) {
        this.code = code;
        this.codeChanged = true;
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setString(CODE_NBT_TAG, this.code);
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.setCodeString(compound.getString(CODE_NBT_TAG));
    }

    // CODE BINDINGS
    public void put(String key,Object val) {
        this.bindings.put(key, val);
    }

    public boolean hasKey(String key) {
        return this.bindings.containsKey(key);
    }

    private void failz0r(WorldServer world, BlockPos pos, String fmt, Object... args) {
        world.spawnParticle(EnumParticleTypes.SPELL, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5,  20, 0, 0, 0, .5, new int[0]);
        FMLLog.severe(fmt, args);
    }

    public void invoke(WorldServer world, BlockPos pos, String method, MyEntity entity) {
        this.ensureCompiled(world, pos);
        // wrap entity in MyEntity!
        PyObject obj = (PyObject) this.bindings.get(method);
        if (obj == null) {
            this.failz0r(world, pos, "Unknown function '%s'", method);
            return;
        }
        PyFunction func = (PyFunction)obj;

        // handle instances of optional player argument
        PyObject co_varnames = func.__code__.__getattr__("co_varnames");
        if (entity instanceof MyEntityPlayer && !co_varnames.__contains__(Py.java2py("player"))) {
            // don't pass the player in if it's not expected
            try {
                func.__call__();
            } catch (NullPointerException e) {
                this.failz0r(world, pos, "Error running code: ", e.getMessage());
            }
            return;
        }

        // carry on!
        try {
            func.__call__(Py.java2py(entity));
        } catch (NullPointerException e) {
            this.failz0r(world, pos, "Error running code: %s", e.getMessage());
        }
    }

    public void invoke(WorldServer world, BlockPos pos, String method) {
        this.ensureCompiled(world, pos);
        PyObject obj = (PyObject) this.bindings.get(method);
        if (obj == null) {
            this.failz0r(world, pos, "Unknown function '%s'", method);
            return;
        }
        PyFunction func = (PyFunction)obj;
        try {
            func.__call__();
        } catch (NullPointerException e) {
            this.failz0r(world, pos, "Error running code: ", e.getMessage());
        }
    }

    public boolean setCodeFromBook(WorldServer world, BlockPos pos, ItemStack heldItem) {
        NBTTagCompound bookData = heldItem.getTagCompound();
        NBTTagList pages;
        try {
            // pages are all of type TAG_String == 8
            pages = bookData.getTagList("pages", 8);
        } catch (NullPointerException e) {
            // this should not happen!
            this.failz0r(world, pos, "Could not get pages from the book!?");
            return true;
        }
        // collapse the pages into one string
        StringBuilder sbStr = new StringBuilder();
        for(int i = 0;i<pages.tagCount();i++) {
            String s = pages.getStringTagAt(i);
            if (i > 0) sbStr.append("\n");
            sbStr.append(s);
        }
        this.setCodeString(sbStr.toString());
        this.ensureCompiled(world, pos);
        return true;
    }

    public void ensureCompiled(WorldServer world, BlockPos pos) {
        if (this.codeChanged) {
            this.eval(world, pos);
            this.codeChanged = false;
        }

        // ensure this is up to date
        this.bindings.put("pos", new MyBlockPos(pos));
    }

    public boolean eval(WorldServer world, BlockPos pos) {
        FMLLog.info("Eval my code: %s", this.code);
        this.world = world;
//        this.bindings.put("world", world);
        this.bindings.put("pos", new MyBlockPos(pos));

        // I am reasonably certain that I can't just shove the methods below directly
        // into the script engine namespace because I can't pass a Runnable as a
        // value to be stored in the engine namespace.
        this.bindings.put("__utils__", this);

        // So.. now I copy all those methods to set up the "utils"
        try {
            String s = "";
            for (String n : utils) {
                s += String.format("%s = __utils__.%s\n", n, n);
            }
            PythonEngine.eval(s, this.context);
        } catch (ScriptException e) {
            this.failz0r(world, pos, "Error setting up utils: %s", e.getMessage());
            return false;
        }

        // now execute the code
        try {
            PythonEngine.eval(this.code, this.context);
            world.spawnParticle(EnumParticleTypes.CRIT, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5,  20, 0, 0, 0, .5, new int[0]);
            return true;
        } catch (ScriptException e) {
            this.failz0r(world, pos, "Error running code: %s", e.getMessage());
            return false;
        }
    }

    private String[] utils = {"water", "lava", "clear", "colors"};

    // MyBlockPos for python code, other one for "internal" use without shenanigans
    public void water(MyBlockPos pos) {
        this.water(pos.blockPos);
    }
    public void water(BlockPos pos) {
        if (this.world == null || this.world.isRemote) return;

        Block b = this.world.getBlockState(pos).getBlock();

        if (this.world.isAirBlock(pos)) {
            this.world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState());
        }
    }

    public void lava(MyBlockPos pos) {
        this.lava(pos.blockPos);
    }
    public void lava(BlockPos pos) {
        if (this.world == null || this.world.isRemote) return;

        Block b = this.world.getBlockState(pos).getBlock();

        if (this.world.isAirBlock(pos)) {
            this.world.setBlockState(pos, Blocks.FLOWING_LAVA.getDefaultState());
        }
    }

    public void clear(MyBlockPos pos) {
        this.clear(pos.blockPos);
    }
    public void clear(BlockPos pos) {
        if (this.world == null || this.world.isRemote) return;

        Block b = this.world.getBlockState(pos).getBlock();

        if (!this.world.isAirBlock(pos)) {
            this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    public static HashMap<String, EnumDyeColor> COLORMAP = new HashMap<String, EnumDyeColor>();
    public static HashMap<String, EnumFacing> FACINGMAP = new HashMap<String, EnumFacing>();
    public static List<String> colors = new LinkedList<>();
    public static void init() {
        COLORMAP.put("white", EnumDyeColor.WHITE);
        COLORMAP.put("orange", EnumDyeColor.ORANGE);
        COLORMAP.put("magenta", EnumDyeColor.MAGENTA);
        COLORMAP.put("lightBlue", EnumDyeColor.LIGHT_BLUE);
        COLORMAP.put("yellow", EnumDyeColor.YELLOW);
        COLORMAP.put("lime", EnumDyeColor.LIME);
        COLORMAP.put("pink", EnumDyeColor.PINK);
        COLORMAP.put("gray", EnumDyeColor.GRAY);
        COLORMAP.put("silver", EnumDyeColor.SILVER);
        COLORMAP.put("cyan", EnumDyeColor.CYAN);
        COLORMAP.put("purple", EnumDyeColor.PURPLE);
        COLORMAP.put("blue", EnumDyeColor.BLUE);
        COLORMAP.put("brown", EnumDyeColor.BROWN);
        COLORMAP.put("green", EnumDyeColor.GREEN);
        COLORMAP.put("red", EnumDyeColor.RED );
        COLORMAP.put("black", EnumDyeColor.BLACK);

        FACINGMAP.put("down", EnumFacing.DOWN);
        FACINGMAP.put("up", EnumFacing.UP);
        FACINGMAP.put("north", EnumFacing.NORTH);
        FACINGMAP.put("south", EnumFacing.SOUTH);
        FACINGMAP.put("west", EnumFacing.WEST);
        FACINGMAP.put("east", EnumFacing.EAST);

        for (String name : COLORMAP.keySet()) {
            colors.add(name);
        }
    }
}


