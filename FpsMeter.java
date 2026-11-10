package com.fpsmeter;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = FpsMeter.MODID, name = "FpsMeter", version = "1.0.0",
        acceptedMinecraftVersions = "[1.12.2]", clientSideOnly = true)
public class FpsMeter {
    public static final String MODID = "fpsmeter";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new FpsTracker());
        ClientCommandHandler.instance.registerCommand(new FpsCommand());
    }
}
