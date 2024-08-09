package com.imoonday.tradeenchantmentdisplay;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public final class TradeEnchantmentDisplay {

    public static final String MOD_ID = "tradeenchantmentdisplay";

    public static void init() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }
}
