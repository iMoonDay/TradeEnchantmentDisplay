package com.imoonday.tradeenchantmentdisplay.fabric;

import com.imoonday.tradeenchantmentdisplay.ModKeys;
import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.renderer.EnchantmentRenderer;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferCache;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferInfo;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

public final class TradeEnchantmentDisplayFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TradeEnchantmentDisplay.init();
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            EnchantmentRenderer.renderInHud(Minecraft.getInstance(), guiGraphics);
        });
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            MerchantOfferInfo.update();
        });
        ClientTickEvents.END_CLIENT_TICK.register(MerchantOfferCache::update);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            MerchantOfferCache.getInstance().load();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            MerchantOfferCache cache = MerchantOfferCache.getInstance();
            cache.save();
            cache.clear();
        });
        registerKeys();
    }

    private static void registerKeys() {
        List<ModKeys.KeyBinding> keys = ModKeys.getKeys();
        for (ModKeys.KeyBinding key : keys) {
            KeyBindingHelper.registerKeyBinding(key.keyMapping());
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (ModKeys.KeyBinding key : keys) {
                while (key.keyMapping().consumeClick()) {
                    key.run(client);
                }
            }
        });
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                for (ModKeys.KeyBinding keyBinding : keys) {
                    if (keyBinding.keyMapping().matches(key, scancode)) {
                        keyBinding.runOnScreen(screen1);
                    }
                }
            });
        });
    }
}
