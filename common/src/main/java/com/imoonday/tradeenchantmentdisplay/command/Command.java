package com.imoonday.tradeenchantmentdisplay.command;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Command extends Runnable {

    Logger LOGGER = LoggerFactory.getLogger(Command.class);

    String getName();

    default int execute() {
        try {
            run();
            Minecraft.getInstance().gui.setOverlayMessage(new TranslatableComponent("commands.tradeenchantmentdisplay.run.success").withStyle(ChatFormatting.GREEN), false);
            return 1;
        } catch (Exception e) {
            Minecraft.getInstance().gui.setOverlayMessage(new TranslatableComponent("commands.tradeenchantmentdisplay.run.fail").withStyle(ChatFormatting.RED), false);
            LOGGER.error("Error executing command: {}", e.getMessage());
            return 0;
        }
    }
}
