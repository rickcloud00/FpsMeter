package com.fpsmeter;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class FpsCommand extends CommandBase {
    @Override
    public String getName() {
        return "fpsm";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/fpsm [reset|show|hide]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        String a = args.length > 0 ? args[0].toLowerCase() : "reset";
        if (a.equals("show")) {
            FpsTracker.visible = true;
            sender.sendMessage(new TextComponentString("FpsMeter: visível"));
        } else if (a.equals("hide")) {
            FpsTracker.visible = false;
            sender.sendMessage(new TextComponentString("FpsMeter: escondido"));
        } else {
            FpsTracker.resetRequested = true;
            sender.sendMessage(new TextComponentString("FpsMeter: média zerada"));
        }
    }
}
