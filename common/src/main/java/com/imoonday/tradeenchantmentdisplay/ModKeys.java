package com.imoonday.tradeenchantmentdisplay;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

public class ModKeys {

    public static final String CATEGORY = "key.categories.tradeenchantmentdisplay";
    public static final List<KeyBinding> KEYS = List.of(
            new KeyBinding(new KeyMapping("key.tradeenchantmentdisplay.toggle_enabled", GLFW.GLFW_KEY_H, CATEGORY), KeyAction.of(ModKeys::handleToggleEnabled, ModKeys::handleToggleEnabledOnScreen), true)
    );

    private static void handleToggleEnabled(Minecraft mc) {
        ModConfig.Hud settings = AutoConfig.getConfigHolder(ModConfig.class).get().hud;
        settings.enabled = !settings.enabled;
    }

    private static void handleToggleEnabledOnScreen(Screen screen) {
        if (screen instanceof MerchantScreen) {
            ModConfig.Screen settings = AutoConfig.getConfigHolder(ModConfig.class).get().screen;
            settings.enabled = !settings.enabled;
        }
    }

    public interface KeyAction {

        void run(Minecraft mc);

        default void runOnScreen(Screen screen) {

        }

        static KeyAction of(Consumer<Minecraft> action, Consumer<Screen> actionOnScreen) {
            return new KeyAction() {
                @Override
                public void run(Minecraft mc) {
                    action.accept(mc);
                }

                @Override
                public void runOnScreen(Screen screen) {
                    actionOnScreen.accept(screen);
                }
            };
        }
    }

    public record KeyBinding(KeyMapping keyMapping, KeyAction action, boolean availableOnScreen) {

        public void run(Minecraft mc) {
            action.run(mc);
        }

        public void runOnScreen(Screen screen) {
            if (availableOnScreen) {
                action.runOnScreen(screen);
            }
        }
    }
}
