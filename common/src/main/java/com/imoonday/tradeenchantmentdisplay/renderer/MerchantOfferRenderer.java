package com.imoonday.tradeenchantmentdisplay.renderer;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class MerchantOfferRenderer {

    private final Minecraft mc;
    private final GuiGraphics guiGraphics;
    private final Font font;
    private int fontColor;
    private int bgColor;
    private int dividerColor;
    private int paddingX;
    private int paddingY;
    private int scale;
    private boolean tipForNoEnchantment;

    public MerchantOfferRenderer(Minecraft mc, GuiGraphics guiGraphics, Font font) {
        this.mc = mc;
        this.guiGraphics = guiGraphics;
        this.font = font;
    }

    public MerchantOfferRenderer(Minecraft mc, GuiGraphics guiGraphics, ModConfig.Hud settings) {
        this(mc, guiGraphics, mc.font);
        this.fontColor = settings.fontColor;
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
        RenderSystem.enableDepthTest();
        poseStack.translate(0.0f, 0.0f, 100f);
        int startY = y;
        renderItem(result, x, y);
        int itemY = y + 21;
        if (dividerColor != 0) {
            guiGraphics.fill(x, itemY - 3, x + 16, itemY - 2, dividerColor);
        }
        renderItem(costA, x, itemY);
        itemY += 18;
        if (shouldRenderCostB(costA, costB, result)) {
            renderItem(costB, x, itemY);
            itemY += 16;
        }
        y += 2;
        int endX = x + 16;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(result);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer level = entry.getValue();
            String name = enchantment.getFullname(level).getString();
            guiGraphics.drawString(font, name, x + 16 + paddingX, y, fontColor);
            y += font.lineHeight + 2;
            int lastX = x + 16 + paddingX + font.width(name);
            if (lastX > endX) {
                endX = lastX;
            }
        }
        y -= 2;
        int endY = Math.max(itemY, y);
        if (bgColor != 0) {
            poseStack.translate(0.0f, 0.0f, -100f);
            guiGraphics.fill(x - paddingX, startY - paddingY, endX + paddingX, endY + paddingY, bgColor);
        }
        RenderSystem.disableDepthTest();
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
                Vec2 dimensions = calculateDimensions(offer, false);
                if (dimensions.y + paddingY * 4 <= height && y + dimensions.y + paddingY > height) {
                    y = startY;
                    x = maxX + paddingY * 3;
                }
                if (dimensions.x + paddingX * 4 <= width && x + dimensions.x + paddingX > width) break;
                render(offer, x, y);
                y += (int) dimensions.y + paddingY * 3;
                maxX = Math.max(maxX, x + (int) dimensions.x);
            }
        }
        poseStack.popPose();
    }

    public static boolean shouldRenderCostB(ItemStack costA, ItemStack costB, ItemStack result) {
        return !(costA.is(Items.EMERALD) && costB.is(Items.BOOK) && result.is(Items.ENCHANTED_BOOK) || costB.isEmpty());
    }

    public void renderItem(ItemStack result, int x, int y) {
        guiGraphics.renderFakeItem(result, x, y);
        guiGraphics.renderItemDecorations(font, result, x, y);
    }

    public Vec2 calculateDimensions(MerchantOffer offer, boolean includePadding) {
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
            String name = enchantment.getFullname(level).getString();
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
        return new Vec2(width, height);
    }

    public int getFontColor() {
        return fontColor;
    }

    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public int getDividerColor() {
        return dividerColor;
    }

    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
    }

    public void setColors(int fontColor, int bgColor, int dividerColor) {
        this.fontColor = fontColor;
        this.bgColor = bgColor;
        this.dividerColor = dividerColor;
    }

    public int getPaddingX() {
        return paddingX;
    }

    public void setPaddingX(int paddingX) {
        this.paddingX = paddingX;
    }

    public int getPaddingY() {
        return paddingY;
    }

    public void setPaddingY(int paddingY) {
        this.paddingY = paddingY;
    }

    public void setPadding(int paddingX, int paddingY) {
        this.paddingX = paddingX;
        this.paddingY = paddingY;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public boolean hasTipForNoEnchantment() {
        return tipForNoEnchantment;
    }

    public void setTipVisibility(boolean visible) {
        this.tipForNoEnchantment = visible;
    }
}

