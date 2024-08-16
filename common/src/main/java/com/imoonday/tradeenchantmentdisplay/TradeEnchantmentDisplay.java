package com.imoonday.tradeenchantmentdisplay;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.mojang.logging.LogUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import org.slf4j.Logger;

public final class TradeEnchantmentDisplay {

    public static final String MOD_ID = "tradeenchantmentdisplay";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static boolean trading = false;
    private static String currentWorldName = null;

    public static void init() {
        AutoConfig.register(ModConfig.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));
    }

    public static boolean isTrading() {
        return trading;
    }

    public static void setTrading(boolean trading) {
        TradeEnchantmentDisplay.trading = trading;
    }

    public static String getCurrentWorldName() {
        return currentWorldName;
    }

    public static void setCurrentWorldName(String currentWorldName) {
        TradeEnchantmentDisplay.currentWorldName = currentWorldName;
    }
}
