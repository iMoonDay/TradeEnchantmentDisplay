package com.imoonday.tradeenchantmentdisplay.forge;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(TradeEnchantmentDisplay.MOD_ID)
public final class TradeEnchantmentDisplayForge {

    public TradeEnchantmentDisplayForge() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> TradeEnchantmentDisplay::init);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> EventHandler::register);
    }
}
