package com.imoonday.tradeenchantmentdisplay.forge;

import com.imoonday.tradeenchantmentdisplay.ModKeys;
import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.renderer.EnchantmentRenderer;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferCache;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferInfo;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;

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
            registerKeyBindings();
            registerConnectEvent();
            registerDisconnectEvent();
        }

        private static void registerConfigScreen() {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> {
                return new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
                    return AutoConfig.getConfigScreen(ModConfig.class, parent).get();
                });
            });
        }

        private static void registerHudEvent() {
            FMLJavaModLoadingContext.get().getModEventBus().<RegisterGuiOverlaysEvent>addListener(e -> {
                e.registerAboveAll(TradeEnchantmentDisplay.MOD_ID, (gui, guiGraphics, partialTick, width, height) -> {
                    EnchantmentRenderer.renderInHud(gui.getMinecraft(), guiGraphics);
                });
            });
        }

        private static void registerClientTickEvent() {
            MinecraftForge.EVENT_BUS.<TickEvent.LevelTickEvent>addListener(e -> {
                if (e.side.isClient() && e.phase == TickEvent.Phase.END) {
                    MerchantOfferInfo.update();
                }
            });
            MinecraftForge.EVENT_BUS.<TickEvent.ClientTickEvent>addListener(e -> {
                if (e.phase == TickEvent.Phase.END) {
                    MerchantOfferCache.update(Minecraft.getInstance());
                }
            });
        }

        private static void registerKeyBindings() {
            List<ModKeys.KeyBinding> keys = ModKeys.getKeys();
            FMLJavaModLoadingContext.get().getModEventBus().<RegisterKeyMappingsEvent>addListener(e -> {
                for (ModKeys.KeyBinding key : keys) {
                    e.register(key.keyMapping());
                }
            });
            MinecraftForge.EVENT_BUS.<InputEvent.Key>addListener(e -> {
                if (e.getAction() != InputConstants.PRESS) return;
                Screen screen = Minecraft.getInstance().screen;
                for (ModKeys.KeyBinding keyBinding : keys) {
                    if (keyBinding.keyMapping().matches(e.getKey(), e.getScanCode())) {
                        if (screen != null) {
                            keyBinding.runOnScreen(screen);
                        } else {
                            keyBinding.run(Minecraft.getInstance());
                        }
                    }
                }
            });
        }

        private static void registerConnectEvent() {
            MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingIn>addListener(e -> {
                MerchantOfferCache.getInstance().load();
            });
        }

        private static void registerDisconnectEvent() {
            MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingOut>addListener(e -> {
                MerchantOfferCache.getInstance().save();
                MerchantOfferCache.getInstance().clear();
            });
        }
    }
}
