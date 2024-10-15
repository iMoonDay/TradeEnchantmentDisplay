package com.imoonday.tradeenchantmentdisplay.command;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public interface Command extends Runnable {

    Logger LOGGER = LogUtils.getLogger();

    String getName();

    default int execute() {
        try {
            run();
            Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("commands.tradeenchantmentdisplay.run.success").withStyle(ChatFormatting.GREEN), false);
            return 1;
        } catch (Exception e) {
            Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("commands.tradeenchantmentdisplay.run.fail").withStyle(ChatFormatting.RED), false);
            LOGGER.error("Error executing command: {}", e.getMessage());
            return 0;
        }
    }
}
