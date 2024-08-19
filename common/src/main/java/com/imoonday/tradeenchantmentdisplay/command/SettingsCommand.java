package com.imoonday.tradeenchantmentdisplay.command;

import com.imoonday.tradeenchantmentdisplay.keybinding.ModKeys;
import net.minecraft.client.Minecraft;

public class SettingsCommand implements Command {

    @Override
    public String getName() {
        return "settings";
    }

    @Override
    public void run() {
        ModKeys.openConfigScreen(Minecraft.getInstance());
    }
}
