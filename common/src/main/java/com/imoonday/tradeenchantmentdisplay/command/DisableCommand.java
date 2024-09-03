package com.imoonday.tradeenchantmentdisplay.command;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;

public class DisableCommand implements Command {

    @Override
    public String getName() {
        return "disable";
    }

    @Override
    public void run() {
        ModConfig config = ModConfig.get();
        config.screen.enabled = false;
        config.hud.enabled = false;
        config.merchant.enabled = false;
        ModConfig.save();
    }
}
