package com.imoonday.tradeenchantmentdisplay.command;

import java.util.HashSet;
import java.util.Set;

public class ModCommands {

    public static final String ROOT = "trade-display";
    private static final Set<Command> COMMANDS = new HashSet<>();

    static {
        register(new ResetCommand());
        register(new ReloadCommand());
        register(new SaveCommand());
        register(new SettingsCommand());
        register(new DisableCommand());
    }

    public static Set<Command> getCommands() {
        return COMMANDS;
    }

    public static void register(Command command) {
        COMMANDS.add(command);
    }
}
