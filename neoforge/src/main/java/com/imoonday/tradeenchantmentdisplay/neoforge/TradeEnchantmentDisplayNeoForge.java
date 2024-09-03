package com.imoonday.tradeenchantmentdisplay.neoforge;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(TradeEnchantmentDisplay.MOD_ID)
public final class TradeEnchantmentDisplayNeoForge {

    public TradeEnchantmentDisplayNeoForge() {
        if (FMLEnvironment.dist.isClient()) {
            TradeEnchantmentDisplay.init();
            EventHandler.register();
        }
    }
}
