package com.imoonday.tradeenchantmentdisplay.fabric;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.command.Command;
import com.imoonday.tradeenchantmentdisplay.command.ModCommands;
import com.imoonday.tradeenchantmentdisplay.keybinding.ModKeys;
import com.imoonday.tradeenchantmentdisplay.renderer.EnchantmentRenderer;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferHandler;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;

import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class TradeEnchantmentDisplayFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TradeEnchantmentDisplay.init();
        registerKeys();
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            EnchantmentRenderer.renderInHud(Minecraft.getInstance(), guiGraphics);
        });
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            MerchantOfferHandler.clientWorldTick();
        });
        ClientTickEvents.END_CLIENT_TICK.register(MerchantOfferHandler::clientTick);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            MerchantOfferHandler.onJoined();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            MerchantOfferHandler.onDisconnected();
        });
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, client) -> {
            MerchantOfferHandler.onEntityRemoved(entity);
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> builder = literal(ModCommands.ROOT);
            for (Command command : ModCommands.getCommands()) {
                builder.then(literal(command.getName()).executes(context -> command.execute()));
            }
            dispatcher.register(builder);
        });
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
