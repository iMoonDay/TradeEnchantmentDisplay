package com.imoonday.tradeenchantmentdisplay;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;

public class EnchantmentNamesRenderer {

    public static final String FULL_NAME = "$full_name";
    public static final String NAME = "$name";
    public static final String LEVEL = "$level";
    public static final String INDEX = "$index";
    public static final String SIZE = "$size";
    private static ModConfig config;

    public static void render(GuiGraphics guiGraphics, Font font, ItemStack stack, int leftX, int topY, int drawTick) {
        if (config == null) {
            config = AutoConfig.getConfigHolder(ModConfig.class).get();
        }
        if (config == null || !config.enabled) return;
        if (stack.is(Items.ENCHANTED_BOOK) || !config.onlyEnchantedBooks && stack.isEnchanted()) {
            List<Object2IntMap.Entry<Holder<Enchantment>>> entries = EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().toList();
            if (!entries.isEmpty()) {
                int size = entries.size();
                if (config.duration < 0) {
                    config.duration = 0;
                }
                String text = generateText(drawTick, size, entries);
                if (config.displayOnTop) {
                    guiGraphics.pose().translate(0.0f, 0.0f, 200.0f);
                }
                if (config.xAxisCentered) {
                    if (config.bgColor != 0) {
                        guiGraphics.fill(leftX + config.offsetX - font.width(text) / 2 - 2, topY + config.offsetY - 2, leftX + config.offsetX + font.width(text) / 2 + 2, topY + config.offsetY + font.lineHeight + 2, config.bgColor);
                    }
                    guiGraphics.drawCenteredString(font, text, leftX + config.offsetX, topY + config.offsetY, config.fontColor);
                } else {
                    if (config.bgColor != 0) {
                        guiGraphics.fill(leftX + config.offsetX - 2, topY + config.offsetY - 2, leftX + config.offsetX + font.width(text) + 2, topY + config.offsetY + font.lineHeight + 2, config.bgColor);
                    }
                    guiGraphics.drawString(font, text, leftX + config.offsetX, topY + config.offsetY, config.fontColor);
                }
            }
        }
    }

    private static String generateText(int drawTick, int size, List<Object2IntMap.Entry<Holder<Enchantment>>> entries) {
        int index = config.duration == 0 ? 0 : drawTick / config.duration % size;
        Enchantment enchantment = entries.get(index).getKey().value();
        int level = entries.get(index).getIntValue();
        String fullName = enchantment.getFullname(level).getString();
        String name = Component.translatable(enchantment.getDescriptionId()).getString();
        String levelText = "";
        if (level != 1 || enchantment.getMaxLevel() != 1) {
            levelText = Component.translatable("enchantment.level." + level).getString();
        }
        String text;
        if (size > 1) {
            text = config.pluralFormat.replace(FULL_NAME, fullName).replace(NAME, name).replace(LEVEL, levelText).replace(INDEX, String.valueOf(index + 1)).replace(SIZE, String.valueOf(size));
        } else {
            text = config.singularFormat.replace(FULL_NAME, fullName).replace(NAME, name).replace(LEVEL, levelText);
        }
        return text;
    }
}
