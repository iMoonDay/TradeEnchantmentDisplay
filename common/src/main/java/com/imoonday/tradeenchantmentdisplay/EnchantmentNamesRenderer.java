package com.imoonday.tradeenchantmentdisplay;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchantmentNamesRenderer {

    public static final String FULL_NAME = "$full_name";
    public static final String NAME = "$name";
    public static final String LEVEL = "$level";
    public static final String INDEX = "$index";
    public static final String SIZE = "$size";
    private static ModConfig config;

    public static void render(PoseStack poseStack, Font font, ItemStack stack, int leftX, int topY, int drawTick) {
        if (config == null) {
            config = AutoConfig.getConfigHolder(ModConfig.class).get();
        }
        if (config == null || !config.enabled) return;
        if (stack.getItem() == Items.ENCHANTED_BOOK || !config.onlyEnchantedBooks && stack.isEnchanted()) {
            List<Map.Entry<Enchantment, Integer>> entries = new ArrayList<>(EnchantmentHelper.getEnchantments(stack).entrySet());
            if (!entries.isEmpty()) {
                int size = entries.size();
                if (config.duration < 0) {
                    config.duration = 0;
                }
                String text = generateText(drawTick, size, entries);
                if (config.displayOnTop) {
                    poseStack.translate(0.0f, 0.0f, 300.0f);
                }
                if (config.xAxisCentered) {
                    if (config.bgColor != 0) {
                        Screen.fill(poseStack, leftX + config.offsetX - font.width(text) / 2 - 2, topY + config.offsetY - 2, leftX + config.offsetX + font.width(text) / 2 + 2, topY + config.offsetY + font.lineHeight + 2, config.bgColor);
                    }
                    Screen.drawCenteredString(poseStack, font, text, leftX + config.offsetX, topY + config.offsetY, config.fontColor);
                } else {
                    if (config.bgColor != 0) {
                        Screen.fill(poseStack, leftX + config.offsetX - 2, topY + config.offsetY - 2, leftX + config.offsetX + font.width(text) + 2, topY + config.offsetY + font.lineHeight + 2, config.bgColor);
                    }
                    Screen.drawString(poseStack, font, text, leftX + config.offsetX, topY + config.offsetY, config.fontColor);
                }
                if (config.displayOnTop) {
                    poseStack.translate(0.0f, 0.0f, -300.0f);
                }
            }
        }
    }

    private static String generateText(int drawTick, int size, List<Map.Entry<Enchantment, Integer>> entries) {
        int index = config.duration == 0 ? 0 : drawTick / config.duration % size;
        Enchantment enchantment = entries.get(index).getKey();
        int level = entries.get(index).getValue();
        String fullName = enchantment.getFullname(level).getString();
        String name = I18n.get(enchantment.getDescriptionId());
        String levelText = "";
        if (level != 1 || enchantment.getMaxLevel() != 1) {
            levelText = I18n.get("enchantment.level." + level);
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
