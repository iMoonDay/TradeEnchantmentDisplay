package com.imoonday.tradeenchantmentdisplay;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = TradeEnchantmentDisplay.MOD_ID)
public class ModConfig implements ConfigData {

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
}
