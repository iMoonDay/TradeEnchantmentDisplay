package com.imoonday.tradeenchantmentdisplay.forge;

import com.imoonday.tradeenchantmentdisplay.ModConfig;
import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmlclient.ConfigGuiHandler;

@Mod(TradeEnchantmentDisplay.MOD_ID)
public final class TradeEnchantmentDisplayForge {
    public TradeEnchantmentDisplayForge() {
        TradeEnchantmentDisplay.init();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Registry::registerModsPage);
    }

    private static class Registry {
        private static void registerModsPage() {
            ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> {
                return new ConfigGuiHandler.ConfigGuiFactory((client, parent) -> {
                    return AutoConfig.getConfigScreen(ModConfig.class, parent).get();
                });
            });
        }
    }
}
