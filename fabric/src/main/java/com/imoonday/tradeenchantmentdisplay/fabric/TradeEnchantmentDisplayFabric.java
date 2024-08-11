package com.imoonday.tradeenchantmentdisplay.fabric;

import com.imoonday.tradeenchantmentdisplay.ModKeys;
import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.renderer.EnchantmentRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;

public final class TradeEnchantmentDisplayFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TradeEnchantmentDisplay.init();
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            EnchantmentRenderer.renderInHud(Minecraft.getInstance(), guiGraphics);
        });
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            EnchantmentRenderer.update();
        });
        for (ModKeys.KeyBinding key : ModKeys.KEYS) {
            KeyBindingHelper.registerKeyBinding(key.keyMapping());
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (ModKeys.KeyBinding key : ModKeys.KEYS) {
                while (key.keyMapping().consumeClick()) {
                    key.run(client);
                }
            }
        });
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.afterKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                for (ModKeys.KeyBinding keyBinding : ModKeys.KEYS) {
                    if (keyBinding.keyMapping().matches(key, scancode)) {
                        keyBinding.runOnScreen(screen1);
                    }
                }
            });
        });
    }
}
