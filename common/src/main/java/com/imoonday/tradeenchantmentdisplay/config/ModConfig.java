package com.imoonday.tradeenchantmentdisplay.config;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
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

    @Config(name = "screen")
    public static class Screen implements ConfigData {
        public boolean enabled = true;
        public boolean onlyEnchantedBooks = false;
        public int offsetX = 82;
        public int offsetY = 10;
        public boolean xAxisCentered = true;
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
        public String pluralFormat = "$full_name ($index/$size)";
    }

    @Config(name = "hud")
    public static class Hud implements ConfigData {
        public boolean enabled = true;
        public boolean onlyEnchantedBooks = false;
        @ConfigEntry.Gui.PrefixText
        public MerchantOfferAcquisitionMethod acquisitionMethod = MerchantOfferAcquisitionMethod.SIMULATED_AND_CACHE;
        public int offsetX = 10;
        public int offsetY = 10;
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
}
