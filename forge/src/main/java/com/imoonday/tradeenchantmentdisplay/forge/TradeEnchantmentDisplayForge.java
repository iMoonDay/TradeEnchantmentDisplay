package com.imoonday.tradeenchantmentdisplay.forge;

import com.imoonday.tradeenchantmentdisplay.ModConfig;
import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(TradeEnchantmentDisplay.MOD_ID)
public final class TradeEnchantmentDisplayForge {
    public TradeEnchantmentDisplayForge() {
        TradeEnchantmentDisplay.init();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Registry::registerModsPage);
    }

    private static class Registry {
        private static void registerModsPage() {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> {
                return new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
                    return AutoConfig.getConfigScreen(ModConfig.class, parent).get();
                });
            });
        }
    }
}
