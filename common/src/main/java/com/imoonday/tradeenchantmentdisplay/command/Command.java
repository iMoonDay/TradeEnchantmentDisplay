package com.imoonday.tradeenchantmentdisplay.command;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.logging.Level;

import static com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay.LOGGER;

public interface Command extends Runnable {

    String getName();

    default int execute() {
        try {
            run();
            Minecraft.getInstance().gui.setOverlayMessage(new TranslatableComponent("commands.tradeenchantmentdisplay.run.success").withStyle(ChatFormatting.GREEN), false);
            return 1;
        } catch (Exception e) {
            Minecraft.getInstance().gui.setOverlayMessage(new TranslatableComponent("commands.tradeenchantmentdisplay.run.fail").withStyle(ChatFormatting.RED), false);
            LOGGER.log(Level.SEVERE, "Error executing command: {}", e.getMessage());
            return 0;
        }
    }
}
