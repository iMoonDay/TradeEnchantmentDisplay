package com.imoonday.tradeenchantmentdisplay.command;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public interface Command extends Runnable {

    String getName();

    default int execute() {
        try {
            run();
            Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("commands.tradeenchantmentdisplay.run.success").withStyle(ChatFormatting.GREEN), false);
            return 1;
        } catch (Exception e) {
            Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("commands.tradeenchantmentdisplay.run.fail").withStyle(ChatFormatting.RED), false);
            e.printStackTrace();
            return 0;
        }
    }
}
