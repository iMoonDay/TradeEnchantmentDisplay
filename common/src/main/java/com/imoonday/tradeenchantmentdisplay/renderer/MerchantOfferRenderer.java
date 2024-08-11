package com.imoonday.tradeenchantmentdisplay.renderer;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.Vec2;

import java.util.Map;

public class MerchantOfferRenderer {

    private final GuiGraphics guiGraphics;
    private final Font font;
    private final PassPredicate passPredicate;
    private int fontColor;
    private int bgColor;
    private int dividerColor;
    private int paddingX;
    private int paddingY;

    public MerchantOfferRenderer(Minecraft mc, GuiGraphics guiGraphics, Font font, PassPredicate passPredicate, int fontColor, int bgColor, int dividerColor, int paddingX, int paddingY) {
        this.guiGraphics = guiGraphics;
        this.font = font;
        this.passPredicate = passPredicate;
        this.fontColor = fontColor;
        this.bgColor = bgColor;
        this.dividerColor = dividerColor;
        this.paddingX = paddingX;
        this.paddingY = paddingY;
    }

    public MerchantOfferRenderer(Minecraft mc, GuiGraphics guiGraphics, ModConfig.Hud settings) {
        this(mc, guiGraphics, settings, stack -> false);
    }

    public MerchantOfferRenderer(Minecraft mc, GuiGraphics guiGraphics, ModConfig.Hud settings, PassPredicate passPredicate) {
        this.guiGraphics = guiGraphics;
        this.fontColor = settings.fontColor;
        this.bgColor = settings.bgColor;
        this.dividerColor = settings.dividerColor;
        this.paddingX = settings.paddingX;
        this.paddingY = settings.paddingY;
        this.font = mc.font;
        this.passPredicate = passPredicate;
    }

    public void render(MerchantOffer offer, int x, int y) {
        ItemStack costA = offer.getCostA();
        ItemStack costB = offer.getCostB();
        ItemStack result = offer.getResult();
        if (passPredicate.test(result)) return;
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        RenderSystem.enableDepthTest();
        poseStack.translate(0.0f, 0.0f, 100f);
        int startY = y;
        guiGraphics.renderFakeItem(result, x, y);
        guiGraphics.renderItemDecorations(font, result, x, y);
        int itemY = y + 21;
        if (dividerColor != 0) {
            guiGraphics.fill(x, itemY - 3, x + 16, itemY - 2, dividerColor);
        }
        guiGraphics.renderFakeItem(costA, x, itemY);
        guiGraphics.renderItemDecorations(font, costA, x, itemY);
        itemY += 18;
        if (!(costA.is(Items.EMERALD) && costB.is(Items.BOOK) && result.is(Items.ENCHANTED_BOOK)) && !costB.isEmpty()) {
            guiGraphics.renderFakeItem(costB, x, itemY);
            guiGraphics.renderItemDecorations(font, costB, x, itemY);
            itemY += 18;
        }
        y += 2;
        int endX = x + 18;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(result);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer level = entry.getValue();
            String name = enchantment.getFullname(level).getString();
            guiGraphics.drawString(font, name, x + 20, y, fontColor);
            y += font.lineHeight + 2;
            int lastX = x + 20 + font.width(name);
            if (lastX > endX) {
                endX = lastX;
            }
        }
        int endY = Math.max(itemY, y);
        if (bgColor != 0) {
            poseStack.translate(0.0f, 0.0f, -100f);
            guiGraphics.fill(x - paddingX, startY - paddingY, endX + paddingX, endY + paddingY, bgColor);
            poseStack.translate(0.0f, 0.0f, 100f);
        }
        RenderSystem.disableDepthTest();
        poseStack.popPose();
    }

    public Vec2 calculateDimensions(MerchantOffer offer, boolean includePadding) {
        ItemStack costA = offer.getCostA();
        ItemStack costB = offer.getCostB();
        ItemStack result = offer.getResult();
        int itemY = 21 + 18;
        if (!(costA.is(Items.EMERALD) && costB.is(Items.BOOK) && result.is(Items.ENCHANTED_BOOK)) && !costB.isEmpty()) {
            itemY += 18;
        }
        int y = 2;
        int width = 18;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(result);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer level = entry.getValue();
            String name = enchantment.getFullname(level).getString();
            y += font.lineHeight + 2;
            int nameWidth = 20 + font.width(name);
            if (nameWidth > width) {
                width = nameWidth;
            }
        }
        int height = Math.max(itemY, y);
        if (includePadding) {
            width += paddingX * 2;
            height += paddingY * 2;
        }
        return new Vec2(width, height);
    }


    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
    }

    public void setColors(int fontColor, int bgColor, int dividerColor) {
        this.fontColor = fontColor;
        this.bgColor = bgColor;
        this.dividerColor = dividerColor;
    }

    public void setPaddingX(int paddingX) {
        this.paddingX = paddingX;
    }

    public void setPaddingY(int paddingY) {
        this.paddingY = paddingY;
    }

    public void setPadding(int paddingX, int paddingY) {
        this.paddingX = paddingX;
        this.paddingY = paddingY;
    }

    public interface PassPredicate {
        boolean test(ItemStack stack);
    }
}

