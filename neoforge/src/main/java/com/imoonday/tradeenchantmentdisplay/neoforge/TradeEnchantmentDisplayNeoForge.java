package com.imoonday.tradeenchantmentdisplay.neoforge;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(TradeEnchantmentDisplay.MOD_ID)
public final class TradeEnchantmentDisplayNeoForge {

    public TradeEnchantmentDisplayNeoForge() {
        if (FMLEnvironment.dist.isClient()) {
            TradeEnchantmentDisplay.init();
            EventHandler.register();
        }
    }
}
