package com.imoonday.tradeenchantmentdisplay.keybinding;

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

    private static final String CATEGORY = "key.categories.tradeenchantmentdisplay";
    private static final String NAME_PREFIX = "key.tradeenchantmentdisplay.";
    public static final KeyBinding TOGGLE_ENABLED = new KeyBinding(new KeyMapping(NAME_PREFIX + "toggle_enabled", GLFW.GLFW_KEY_H, CATEGORY), KeyAction.of(ModKeys::toggleEnabled, ModKeys::toggleEnabledOnScreen), true);
    public static final KeyBinding OPEN_CONFIG_SCREEN = new KeyBinding(new KeyMapping(NAME_PREFIX + "open_config_screen", GLFW.GLFW_KEY_J, CATEGORY), ModKeys::openConfigScreen, false);
    private static final List<KeyBinding> KEYS = List.of(
            TOGGLE_ENABLED,
            OPEN_CONFIG_SCREEN
    );

    public static List<KeyBinding> getKeys() {
        return KEYS;
    }

    public static void openConfigScreen(Minecraft mc) {
        mc.setScreen(AutoConfig.getConfigScreen(ModConfig.class, mc.screen).get());
    }

    private static void toggleEnabled(Minecraft mc) {
        if (mc.options.keyShift.isDown()) {
            ModConfig.Merchant settings = ModConfig.getMerchant();
            settings.enabled = !settings.enabled;
        } else {
            ModConfig.Hud settings = ModConfig.getHud();
            settings.enabled = !settings.enabled;
        }
    }

    private static void toggleEnabledOnScreen(Screen screen) {
        if (screen instanceof MerchantScreen) {
            ModConfig.Screen settings = ModConfig.getScreen();
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
