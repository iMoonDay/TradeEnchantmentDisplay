package com.imoonday.tradeenchantmentdisplay.forge;

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
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;

import static net.minecraft.commands.Commands.literal;

public class EventHandler {

    static void register() {
        registerKeyBindings();
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> {
            return new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
                return AutoConfig.getConfigScreen(ModConfig.class, parent).get();
            });
        });
        FMLJavaModLoadingContext.get().getModEventBus().<RegisterGuiOverlaysEvent>addListener(e -> {
            e.registerAboveAll(TradeEnchantmentDisplay.MOD_ID, (gui, guiGraphics, partialTick, width, height) -> {
                EnchantmentRenderer.renderInHud(gui.getMinecraft(), guiGraphics);
            });
        });
        MinecraftForge.EVENT_BUS.<TickEvent.LevelTickEvent>addListener(e -> {
            if (e.side.isClient() && e.phase == TickEvent.Phase.END) {
                MerchantOfferHandler.clientWorldTick();
            }
        });
        MinecraftForge.EVENT_BUS.<TickEvent.ClientTickEvent>addListener(e -> {
            if (e.phase == TickEvent.Phase.END) {
                MerchantOfferHandler.clientTick(Minecraft.getInstance());
            }
        });
        MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingIn>addListener(e -> {
            MerchantOfferHandler.onJoined();
        });
        MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingOut>addListener(e -> {
            MerchantOfferHandler.onDisconnected();
        });
        MinecraftForge.EVENT_BUS.<EntityLeaveLevelEvent>addListener(e -> {
            if (e.getLevel().isClientSide) {
                MerchantOfferHandler.onEntityRemoved(e.getEntity());
            }
        });
        MinecraftForge.EVENT_BUS.<RegisterClientCommandsEvent>addListener(e -> {
            LiteralArgumentBuilder<CommandSourceStack> builder = literal(ModCommands.ROOT);
            for (Command command : ModCommands.getCommands()) {
                builder.then(literal(command.getName()).executes(context -> command.execute()));
            }
            e.getDispatcher().register(builder);
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
}
