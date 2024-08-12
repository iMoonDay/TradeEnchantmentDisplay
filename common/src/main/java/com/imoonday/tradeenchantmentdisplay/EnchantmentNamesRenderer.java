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

import java.util.List;
import java.util.Map;

public class EnchantmentNamesRenderer {

    public static final String FULL_NAME = "$full_name";
    public static final String NAME = "$name";
    public static final String LEVEL = "$level";
    public static final String INDEX = "$index";
    public static final String SIZE = "$size";
    public static final String TOTAL = "$total";
    private static ModConfig config;

    public static void render(PoseStack poseStack, Font font, ItemStack stack, int leftX, int topY, int drawTick) {
        if (config == null) {
            config = AutoConfig.getConfigHolder(ModConfig.class).get();
        }
        if (config == null || !config.enabled) return;
        if (stack.is(Items.ENCHANTED_BOOK) || !config.onlyEnchantedBooks && stack.isEnchanted()) {
            List<Map.Entry<Enchantment, Integer>> entries = EnchantmentHelper.getEnchantments(stack).entrySet().stream().toList();
            if (!entries.isEmpty()) {
                int size = entries.size();
                if (config.duration < 0) {
                    config.duration = 0;
                }
                String text = generateText(drawTick, size, entries);
                poseStack.pushPose();
                if (config.displayOnTop) {
                    poseStack.translate(0.0f, 0.0f, 300.0f);
                }
                float scale = config.fontScale / 100.0f;
                if (scale != 1.0f) {
                    poseStack.scale(scale, scale, 1.0f);
                }
                int x = (int) ((leftX + config.offsetX) / scale);
                int y = (int) ((topY + config.offsetY) / scale);
                if (config.xAxisCentered) {
                    if (config.bgColor != 0) {
                        Screen.fill(poseStack, x - font.width(text) / 2 - 2, y - 2, x + font.width(text) / 2 + 2, y + font.lineHeight + 1, config.bgColor);
                    }
                    Screen.drawCenteredString(poseStack, font, text, x, y, config.fontColor);
                } else {
                    if (config.bgColor != 0) {
                        Screen.fill(poseStack, x - 2, y - 2, x + font.width(text) + 2, y + font.lineHeight + 1, config.bgColor);
                    }
                    Screen.drawString(poseStack, font, text, x, y, config.fontColor);
                }
                poseStack.popPose();
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
            text = config.pluralFormat.replace(FULL_NAME, fullName).replace(NAME, name).replace(LEVEL, levelText).replace(INDEX, String.valueOf(index + 1)).replace(TOTAL, String.valueOf(size)).replace(SIZE, String.valueOf(size));
        } else {
            text = config.singularFormat.replace(FULL_NAME, fullName).replace(NAME, name).replace(LEVEL, levelText);
        }
        return text;
    }
}
