package com.imoonday.tradeenchantmentdisplay.renderer;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferInfo;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnchantmentRenderer {

    public static final String FULL_NAME = "$full_name";
    public static final String NAME = "$name";
    public static final String LEVEL = "$level";
    public static final String INDEX = "$index";
    public static final String SIZE = "$size";
    public static final String TOTAL = "$total";
    private static ModConfig config;

    public static void renderInScreen(GuiGraphics guiGraphics, Font font, ItemStack stack, int leftX, int topY, int drawTick) {
        if (!initializeConfigAndCheck()) return;
        ModConfig.Screen settings = config.screen;
        if (!settings.enabled) return;
        if (shouldPass(stack, settings.onlyEnchantedBooks)) return;
        List<Map.Entry<Enchantment, Integer>> entries = EnchantmentHelper.getEnchantments(stack).entrySet().stream().toList();
        if (!entries.isEmpty()) {
            if (settings.duration < 0) {
                settings.duration = 0;
            }
            String text = generateText(entries, drawTick);
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            if (settings.displayOnTop) {
                poseStack.translate(0.0f, 0.0f, 200.0f);
            }
            float scale = settings.fontScale / 100.0f;
            if (scale != 1.0f) {
                poseStack.scale(scale, scale, 1.0f);
            }
            int x = (int) ((leftX + settings.offsetX) / scale);
            int y = (int) ((topY + settings.offsetY) / scale);
            drawTextWithBackground(guiGraphics, font, text, x, y, settings);
            poseStack.popPose();
        }
    }

    private static void drawTextWithBackground(GuiGraphics guiGraphics, Font font, String text, int x, int y, ModConfig.Screen settings) {
        if (settings.xAxisCentered) {
            if (settings.bgColor != 0) {
                guiGraphics.fill(x - font.width(text) / 2 - 2, y - 2, x + font.width(text) / 2 + 2, y + font.lineHeight + 1, settings.bgColor);
            }
            guiGraphics.drawCenteredString(font, text, x, y, settings.fontColor);
        } else {
            if (settings.bgColor != 0) {
                guiGraphics.fill(x - 2, y - 2, x + font.width(text) + 2, y + font.lineHeight + 1, settings.bgColor);
            }
            guiGraphics.drawString(font, text, x, y, settings.fontColor);
        }
    }

    private static String generateText(List<Map.Entry<Enchantment, Integer>> entries, int drawTick) {
        int size = entries.size();
        ModConfig.Screen settings = config.screen;
        int index = settings.duration == 0 ? 0 : drawTick / settings.duration % size;
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
            text = settings.pluralFormat.replace(FULL_NAME, fullName).replace(NAME, name).replace(LEVEL, levelText).replace(INDEX, String.valueOf(index + 1)).replace(TOTAL, String.valueOf(size)).replace(SIZE, String.valueOf(size));
        } else {
            text = settings.singularFormat.replace(FULL_NAME, fullName).replace(NAME, name).replace(LEVEL, levelText);
        }
        return text;
    }

    public static void renderInHud(Minecraft mc, GuiGraphics guiGraphics) {
        if (!initializeConfigAndCheck() || mc == null) return;
        ModConfig.Hud settings = config.hud;
        if (!settings.enabled) return;
        List<MerchantOffer> offers = MerchantOfferUtils.getMerchantOffers(mc, settings.onlyEnchantedBooks);
        MerchantOfferRenderer renderer = new MerchantOfferRenderer(mc, guiGraphics, settings);
        boolean onlyEnchantedBooks = settings.onlyEnchantedBooks;
        renderer.render(offers, settings.offsetX, settings.offsetY, offer -> !shouldPass(offer.getResult(), onlyEnchantedBooks));
    }

    public static boolean shouldPass(ItemStack stack, boolean onlyEnchantedBooks) {
        return !stack.is(Items.ENCHANTED_BOOK) && (onlyEnchantedBooks || !stack.isEnchanted());
    }

    public static void renderUnderNameTag(Entity entity, MerchantOfferInfo info, PoseStack poseStack, MultiBufferSource buffer, ItemRenderer itemRenderer, EntityRenderDispatcher dispatcher, Font font, int packedLight) {
        if (!initializeConfigAndCheck()) return;
        ModConfig.Merchant settings = config.merchant;
        if (!settings.enabled) return;
        List<MerchantOffer> offers = info.getOffers(EnchantmentRenderer::isStandardEnchantedBookTrade);
        if (offers.isEmpty()) return;
        boolean discrete = entity.isDiscrete();
        float offsetY = entity.getNameTagOffsetY() + settings.offsetY;
        int y = "deadmau5".equals(entity.getDisplayName().getString()) ? -10 : 0;
        poseStack.pushPose();
        poseStack.translate(0.0f, offsetY, 0.0f);
        poseStack.mulPose(dispatcher.cameraOrientation());
        poseStack.scale(-0.025f, -0.025f, 0.025f);
        Matrix4f matrix4f = poseStack.last().pose();
        float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25f);
        int bgColor = (int) (g * 255.0f) << 24;
        if (settings.duration < 0) {
            settings.duration = 0;
        }
        int duration = settings.duration;
        MerchantOffer offer = offers.get(duration == 0 ? 0 : entity.tickCount / duration % offers.size());
        ItemStack stack = offer.getResult();
        Set<Map.Entry<Enchantment, Integer>> entries = EnchantmentHelper.getEnchantments(stack).entrySet();
        int nameColor = settings.nameColor;
        String price = String.valueOf(offer.getCostA().getCount());
        int priceColor = settings.priceColor;
        for (Map.Entry<Enchantment, Integer> entry : entries) {
            Enchantment enchantment = entry.getKey();
            Integer level = entry.getValue();
            String name = enchantment.getFullname(level).getString();
            float x = -(font.width(name) + 4 + font.width(price)) / 2f;
            font.drawInBatch(name, x, y, nameColor, false, matrix4f, buffer, !discrete ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, bgColor, packedLight);
            if (!discrete) {
                font.drawInBatch(name, x, y, nameColor, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
            }
            x += font.width(name) + 4;
            font.drawInBatch(price, x, y, priceColor, false, matrix4f, buffer, !discrete ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, bgColor, packedLight);
            if (!discrete) {
                font.drawInBatch(price, x, y, priceColor, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
            }
            poseStack.translate(0.0f, font.lineHeight + 2, 0.0f);
        }
        poseStack.popPose();
    }

    public static boolean isStandardEnchantedBookTrade(MerchantOffer offer) {
        return offer.getCostA().is(Items.EMERALD) && offer.getCostB().is(Items.BOOK) && offer.getResult().is(Items.ENCHANTED_BOOK);
    }

    private static boolean initializeConfigAndCheck() {
        if (config == null) {
            config = ModConfig.get();
        }
        return config != null;
    }
}
