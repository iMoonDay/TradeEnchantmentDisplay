package com.imoonday.tradeenchantmentdisplay;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;

import java.util.logging.Logger;

public final class TradeEnchantmentDisplay {

    public static final String MOD_ID = "tradeenchantmentdisplay";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    private static boolean trading = false;

    public static void init() {
        AutoConfig.register(ModConfig.class, PartitioningSerializer.wrap(GsonConfigSerializer::new));
    }

    public static boolean isTrading() {
        return trading;
    }

    public static void setTrading(boolean trading) {
        TradeEnchantmentDisplay.trading = trading;
    }
}
