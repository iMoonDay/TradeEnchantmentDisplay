package com.imoonday.tradeenchantmentdisplay.forge;

import com.imoonday.tradeenchantmentdisplay.command.Command;
import com.imoonday.tradeenchantmentdisplay.command.ModCommands;
import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.keybinding.ModKeys;
import com.imoonday.tradeenchantmentdisplay.renderer.EnchantmentRenderer;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferHandler;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;

import static net.minecraft.commands.Commands.literal;

public class EventHandler {

    static void register() {
        registerKeyBindings();
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> {
            return (client, parent) -> {
                return AutoConfig.getConfigScreen(ModConfig.class, parent).get();
            };
        });
        MinecraftForge.EVENT_BUS.<RenderGameOverlayEvent.Post>addListener(e -> {
            EnchantmentRenderer.renderInHud(Minecraft.getInstance(), e.getMatrixStack());
        });
        MinecraftForge.EVENT_BUS.<TickEvent.ClientTickEvent>addListener(e -> {
            if (e.phase == TickEvent.Phase.END) {
                MerchantOfferHandler.clientWorldTick();
                MerchantOfferHandler.clientTick(Minecraft.getInstance());
            }
        });
        MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggedInEvent>addListener(e -> {
            MerchantOfferHandler.onJoined();
        });
        MinecraftForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggedOutEvent>addListener(e -> {
            MerchantOfferHandler.onDisconnected();
        });
        MinecraftForge.EVENT_BUS.<EntityLeaveWorldEvent>addListener(e -> {
            if (e.getWorld().isClientSide) {
                MerchantOfferHandler.onEntityRemoved(e.getEntity());
            }
        });
        MinecraftForge.EVENT_BUS.<RegisterCommandsEvent>addListener(e -> {
            LiteralArgumentBuilder<CommandSourceStack> builder = literal(ModCommands.ROOT);
            for (Command command : ModCommands.getCommands()) {
                builder.then(literal(command.getName()).executes(context -> command.execute()));
            }
            e.getDispatcher().register(builder);
        });
    }

    private static void registerKeyBindings() {
        List<ModKeys.KeyBinding> keys = ModKeys.getKeys();
        MinecraftForge.EVENT_BUS.<FMLClientSetupEvent>addListener(e -> {
            for (ModKeys.KeyBinding key : keys) {
                ClientRegistry.registerKeyBinding(key.keyMapping());
            }
        });
        MinecraftForge.EVENT_BUS.<InputEvent.KeyInputEvent>addListener(e -> {
            if (e.getAction() != 1) return;
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
