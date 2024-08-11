package com.imoonday.tradeenchantmentdisplay.forge;

import com.imoonday.tradeenchantmentdisplay.ModKeys;
import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.renderer.EnchantmentRenderer;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(TradeEnchantmentDisplay.MOD_ID)
public final class TradeEnchantmentDisplayForge {
    public TradeEnchantmentDisplayForge() {
        TradeEnchantmentDisplay.init();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Registry::register);
    }

    private static class Registry {
        private static void register() {
            registerConfigScreen();
            registerHudEvent();
            registerClientTickEvent();
        }

        private static void registerConfigScreen() {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> {
                return new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
                    return AutoConfig.getConfigScreen(ModConfig.class, parent).get();
                });
            });
        }

        private static void registerHudEvent() {
            MinecraftForge.EVENT_BUS.<RegisterGuiOverlaysEvent>addListener(e -> {
                e.registerAboveAll(TradeEnchantmentDisplay.MOD_ID, (gui, guiGraphics, partialTick, width, height) -> {
                    EnchantmentRenderer.renderInHud(gui.getMinecraft(), guiGraphics);
                });
            });
        }

        private static void registerClientTickEvent() {
            MinecraftForge.EVENT_BUS.<TickEvent.LevelTickEvent>addListener(e -> {
                if (e.side.isClient() && e.phase == TickEvent.Phase.END) {
                    EnchantmentRenderer.update();
                }
            });
        }

        private static void registerKeyBindings() {
            MinecraftForge.EVENT_BUS.<RegisterKeyMappingsEvent>addListener(e -> {
                for (ModKeys.KeyBinding key : ModKeys.KEYS) {
                    e.register(key.keyMapping());
                }
            });
            MinecraftForge.EVENT_BUS.<TickEvent.ClientTickEvent>addListener(e -> {
                if (e.phase == TickEvent.Phase.END) {
                    for (ModKeys.KeyBinding key : ModKeys.KEYS) {
                        key.run(Minecraft.getInstance());
                    }
                }
            });
            //TODO run on screen
        }
    }
}
