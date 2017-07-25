package net.mechanicalcat.pycode.init;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ModConfiguration
{
    private static boolean debug;

    public ModConfiguration(File config)
    {
        Configuration configuration = new Configuration(config);
        this.debug = configuration.getBoolean("debug", "general", false, "For debug PyCode Mode");
    }

    public static boolean isDebug()
    {
        return debug;
    }
}