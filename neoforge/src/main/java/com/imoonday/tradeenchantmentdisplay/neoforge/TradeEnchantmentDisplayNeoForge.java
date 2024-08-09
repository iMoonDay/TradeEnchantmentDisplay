package com.imoonday.tradeenchantmentdisplay.neoforge;

import com.imoonday.tradeenchantmentdisplay.ModConfig;
import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import me.shedaniel.autoconfig.AutoConfig;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(TradeEnchantmentDisplay.MOD_ID)
public final class TradeEnchantmentDisplayNeoForge {
    public TradeEnchantmentDisplayNeoForge() {
        TradeEnchantmentDisplay.init();
        if (FMLEnvironment.dist.isClient()) {
            registerModsPage();
        }
    }

    private static void registerModsPage() {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> {
            return (client, screen) -> {
                return AutoConfig.getConfigScreen(ModConfig.class, screen).get();
            };
        });
    }
}
