package com.imoonday.tradeenchantmentdisplay.renderer;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import org.joml.Vector2i;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class MerchantOfferRenderer {

    private final Minecraft mc;
    private final GuiGraphics guiGraphics;
    private final Font font;
    public int fontColor;
    public ModConfig.FontColorForMaxLevel fontColorForMaxLevel = new ModConfig.FontColorForMaxLevel();
    public int bgColor;
    public int dividerColor;
    public int paddingX;
    public int paddingY;
    public int scale;
    public boolean tipForNoEnchantment;

    public MerchantOfferRenderer(Minecraft mc, GuiGraphics guiGraphics, Font font) {
        this.mc = mc;
        this.guiGraphics = guiGraphics;
        this.font = font;
    }

    public MerchantOfferRenderer(Minecraft mc, GuiGraphics guiGraphics, ModConfig.Hud settings) {
        this(mc, guiGraphics, mc.font);
        this.fontColor = settings.fontColor;
        this.fontColorForMaxLevel = settings.fontColorForMaxLevel;
        this.bgColor = settings.bgColor;
        this.dividerColor = settings.dividerColor;
        this.paddingX = settings.paddingX;
        this.paddingY = settings.paddingY;
        this.scale = settings.scale;
        this.tipForNoEnchantment = settings.tipForNoEnchantment;
    }

    public void render(MerchantOffer offer, int x, int y) {
        ItemStack costA = offer.getCostA();
        ItemStack costB = offer.getCostB();
        ItemStack result = offer.getResult();
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        renderItem(result, x, y);
        int itemY = y + 21;
        if (dividerColor != 0) {
            guiGraphics.fill(x, itemY - 3, x + 16, itemY - 2, dividerColor);
        }
        renderItem(costA, x, itemY);
        itemY += 18;
        if (shouldRenderCostB(costA, costB, result)) {
            renderItem(costB, x, itemY);
        }
        y += 2;
        int endX = x + 16;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(result);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer level = entry.getValue();
            MutableComponent name = getFormattedName(enchantment, level);
            TextColor textColor = name.getStyle().getColor();
            int color = textColor != null ? textColor.getValue() : fontColor;
            guiGraphics.drawString(font, name, x + 16 + paddingX, y, color);
            y += font.lineHeight + 2;
            int lastX = x + 16 + paddingX + font.width(name);
            if (lastX > endX) {
                endX = lastX;
            }
        }
        poseStack.popPose();
    }

    public void render(List<MerchantOffer> offers, int x, int y, Predicate<MerchantOffer> filter) {
        float scale = this.scale / 100.0f;
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        if (scale != 1.0f) {
            poseStack.scale(scale, scale, 1.0f);
        }
        if (offers.isEmpty()) {
            if (tipForNoEnchantment && MerchantOfferHandler.getValidMerchant(mc) != null) {
                String text = I18n.get("text.tradeenchantmentdisplay.no_enchantment");
                guiGraphics.fill(x - paddingX, y - paddingY, x + font.width(text) + paddingX, y + font.lineHeight + paddingY - 1, bgColor);
                guiGraphics.drawString(font, text, x, y, fontColor);
            }
        } else {
            final int startY = y;
            int maxX = 0;

            int height = (int) ((mc.getWindow().getGuiScaledHeight() - paddingY) / scale);
            int width = (int) ((mc.getWindow().getGuiScaledWidth() - paddingX) / scale);
            for (MerchantOffer offer : offers) {
                if (!filter.test(offer)) continue;
                Vector2i dimensions = calculateDimensions(offer, false);
                if (dimensions.y + paddingY * 4 <= height && y + dimensions.y + paddingY > height) {
                    y = startY;
                    x = maxX + paddingY * 3;
                }
                if (dimensions.x + paddingX * 4 <= width && x + dimensions.x + paddingX > width) break;
                renderBackground(x, y, dimensions.x, dimensions.y);
                render(offer, x, y);
                y += dimensions.y + paddingY * 3;
                maxX = Math.max(maxX, x + dimensions.x);
            }
        }
        poseStack.popPose();
    }

    public void renderBackground(int x, int y, int width, int height) {
        if (bgColor == 0) return;
        guiGraphics.fill(x - paddingX, y - paddingY, x + width + paddingX, y + height + paddingY, bgColor);
    }

    public static boolean shouldRenderCostB(ItemStack costA, ItemStack costB, ItemStack result) {
        return !(costA.is(Items.EMERALD) && costB.is(Items.BOOK) && result.is(Items.ENCHANTED_BOOK) || costB.isEmpty());
    }

    public void renderItem(ItemStack result, int x, int y) {
        guiGraphics.renderFakeItem(result, x, y);
        guiGraphics.renderItemDecorations(font, result, x, y);
    }

    public Vector2i calculateDimensions(MerchantOffer offer, boolean includePadding) {
        ItemStack costA = offer.getCostA();
        ItemStack costB = offer.getCostB();
        ItemStack result = offer.getResult();
        int itemY = 21 + 18;
        if (shouldRenderCostB(costA, costB, result)) {
            itemY += 16;
        }
        int y = 2;
        int width = 16;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(result);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer level = entry.getValue();
            MutableComponent name = getFormattedName(enchantment, level);
            y += font.lineHeight + 2;
            int nameWidth = 16 + paddingX + font.width(name);
            if (nameWidth > width) {
                width = nameWidth;
            }
        }
        y -= 2;
        int height = Math.max(itemY, y);
        if (includePadding) {
            width += paddingX * 2;
            height += paddingY * 2;
        }
        return new Vector2i(width, height);
    }

    public MutableComponent getFormattedName(Enchantment enchantment, int level) {
        MutableComponent name = enchantment.getFullname(level).copy().setStyle(Style.EMPTY.withColor(fontColor));
        if (fontColorForMaxLevel.shouldFormat(level, enchantment.getMaxLevel())) {
            name = fontColorForMaxLevel.format(name);
        }
        return name;
    }

    public void setColors(int fontColor, int bgColor, int dividerColor) {
        this.fontColor = fontColor;
        this.bgColor = bgColor;
        this.dividerColor = dividerColor;
    }

    public void setPadding(int paddingX, int paddingY) {
        this.paddingX = paddingX;
        this.paddingY = paddingY;
    }
}

