package com.imoonday.tradeenchantmentdisplay.fabric;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import net.fabricmc.api.ClientModInitializer;

public final class TradeEnchantmentDisplayFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TradeEnchantmentDisplay.init();
    }
}
