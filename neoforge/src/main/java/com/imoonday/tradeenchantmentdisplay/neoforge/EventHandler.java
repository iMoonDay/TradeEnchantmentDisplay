package com.imoonday.tradeenchantmentdisplay.neoforge;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.command.Command;
import com.imoonday.tradeenchantmentdisplay.command.ModCommands;
import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.keybinding.ModKeys;
import com.imoonday.tradeenchantmentdisplay.renderer.EnchantmentRenderer;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferHandler;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.List;

import static net.minecraft.commands.Commands.literal;

public class EventHandler {

    static void register() {
        registerKeyBindings();
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> {
            return (client, screen) -> {
                return AutoConfig.getConfigScreen(ModConfig.class, screen).get();
            };
        });
        ModLoadingContext.get().getActiveContainer().getEventBus().<RegisterGuiLayersEvent>addListener(e -> {
            e.registerAboveAll(new ResourceLocation(TradeEnchantmentDisplay.MOD_ID, "enchantments"), (guiGraphics, partialTick) -> {
                EnchantmentRenderer.renderInHud(Minecraft.getInstance(), guiGraphics);
            });
        });
        NeoForge.EVENT_BUS.<ClientTickEvent.Post>addListener(e -> {
            MerchantOfferHandler.clientWorldTick();
            MerchantOfferHandler.clientTick(Minecraft.getInstance());
        });
        NeoForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingIn>addListener(e -> {
            MerchantOfferHandler.onJoined();
        });
        NeoForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingOut>addListener(e -> {
            MerchantOfferHandler.onDisconnected();
        });
        NeoForge.EVENT_BUS.<EntityLeaveLevelEvent>addListener(e -> {
            if (e.getLevel().isClientSide) {
                MerchantOfferHandler.onEntityRemoved(e.getEntity());
            }
        });
        NeoForge.EVENT_BUS.<RegisterClientCommandsEvent>addListener(e -> {
            LiteralArgumentBuilder<CommandSourceStack> builder = literal(ModCommands.ROOT);
            for (Command command : ModCommands.getCommands()) {
                builder.then(literal(command.getName()).executes(context -> command.execute()));
            }
            e.getDispatcher().register(builder);
        });
    }

    private static void registerKeyBindings() {
        List<ModKeys.KeyBinding> keys = ModKeys.getKeys();
        ModLoadingContext.get().getActiveContainer().getEventBus().<RegisterKeyMappingsEvent>addListener(e -> {
            for (ModKeys.KeyBinding key : keys) {
                e.register(key.keyMapping());
            }
        });
        NeoForge.EVENT_BUS.<InputEvent.Key>addListener(e -> {
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
}
