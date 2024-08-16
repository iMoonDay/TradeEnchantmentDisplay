package com.imoonday.tradeenchantmentdisplay.config;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;

@Config(name = TradeEnchantmentDisplay.MOD_ID)
public class ModConfig extends PartitioningSerializer.GlobalData {

    @ConfigEntry.Category("screen")
    @ConfigEntry.Gui.TransitiveObject
    public Screen screen = new Screen();

    @ConfigEntry.Category("hud")
    @ConfigEntry.Gui.TransitiveObject
    public Hud hud = new Hud();

    @ConfigEntry.Category("merchant")
    @ConfigEntry.Gui.TransitiveObject
    public Merchant merchant = new Merchant();

    @ConfigEntry.Category("generic")
    @ConfigEntry.Gui.TransitiveObject
    public Generic generic = new Generic();

    @ConfigEntry.Category("cache")
    @ConfigEntry.Gui.TransitiveObject
    public Cache cache = new Cache();

    @Config(name = "screen")
    public static class Screen implements ConfigData {
        public boolean enabled = true;
        public boolean onlyEnchantedBooks = false;
        public int offsetX = 82;
        public int offsetY = 12;
        public boolean xAxisCentered = true;
        @ConfigEntry.BoundedDiscrete(min = 20, max = 200)
        public int fontScale = 85;
        @ConfigEntry.ColorPicker
        public int fontColor = 0xFFFFFF;
        @ConfigEntry.ColorPicker(allowAlpha = true)
        public int bgColor = 0x00000000;
        @ConfigEntry.BoundedDiscrete(max = 20 * 5)
        @ConfigEntry.Gui.PrefixText
        public int duration = 20;
        public boolean displayOnTop = true;
        @ConfigEntry.Gui.PrefixText
        public String singularFormat = "$full_name";
        @ConfigEntry.Gui.PrefixText
        public String pluralFormat = "$full_name ($index/$total)";

        @Override
        public void validatePostLoad() throws ValidationException {
            if (duration < 0) {
                throw new ValidationException("Duration must be greater than or equal to 0");
            }
        }
    }

    @Config(name = "hud")
    public static class Hud implements ConfigData {
        public boolean enabled = true;
        public boolean onlyEnchantedBooks = false;
        public boolean tipForNoEnchantment = true;
        public int offsetX = 10;
        public int offsetY = 10;
        @ConfigEntry.BoundedDiscrete(min = 20, max = 200)
        public int scale = 100;
        @ConfigEntry.ColorPicker
        public int fontColor = 0xFFFFFF;
        @ConfigEntry.ColorPicker(allowAlpha = true)
        public int dividerColor = 0x50FFFFFF;
        @ConfigEntry.ColorPicker(allowAlpha = true)
        public int bgColor = 0x50000000;
        @ConfigEntry.BoundedDiscrete(max = 20)
        public int paddingX = 5;
        @ConfigEntry.BoundedDiscrete(max = 20)
        public int paddingY = 5;
    }

    @Config(name = "merchant")
    public static class Merchant implements ConfigData {
        public boolean enabled = true;
        public double renderDistance = 64.0;
        @ConfigEntry.BoundedDiscrete(max = 20 * 5)
        public int duration = 40;
        public float offsetY = -0.5f;
        @ConfigEntry.ColorPicker
        public int nameColor = 0xFFAA00;
        @ConfigEntry.ColorPicker
        public int priceColor = 0x55FF55;

        @Override
        public void validatePostLoad() throws ValidationException {
            if (duration < 0) {
                throw new ValidationException("Duration must be greater than or equal to 0");
            }
        }
    }

    @Config(name = "generic")
    public static class Generic implements ConfigData {
        public boolean alwaysAttemptToGetNearbyOffers = false;
    }

    @Config(name = "cache")
    public static class Cache implements ConfigData {
        public boolean enabled = true;
        public String filePath = "trades.nbt";
        public boolean distinguishPortBetweenServers = false;
    }

    public static ModConfig get() {
        return AutoConfig.getConfigHolder(ModConfig.class).get();
    }

    public static Screen getScreen() {
        return get().screen;
    }

    public static Hud getHud() {
        return get().hud;
    }

    public static Merchant getMerchant() {
        return get().merchant;
    }

    public static Generic getGeneric() {
        return get().generic;
    }

    public static Cache getCache() {
        return get().cache;
    }

    public static void save() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }
}
