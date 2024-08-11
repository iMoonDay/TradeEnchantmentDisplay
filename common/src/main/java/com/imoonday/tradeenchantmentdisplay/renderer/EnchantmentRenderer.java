package com.imoonday.tradeenchantmentdisplay.renderer;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.config.MerchantOfferAcquisitionMethod;
import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferCache;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferInfo;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnchantmentRenderer {

    public static final String FULL_NAME = "$full_name";
    public static final String NAME = "$name";
    public static final String LEVEL = "$level";
    public static final String INDEX = "$index";
    public static final String SIZE = "$size";
    private static ModConfig config;

    public static void renderInScreen(GuiGraphics guiGraphics, Font font, ItemStack stack, int leftX, int topY, int drawTick) {
        setConfigIfAbsent();
        if (config == null) return;
        ModConfig.Screen settings = config.screen;
        if (!settings.enabled) return;
        if (shouldPass(stack, settings)) return;
        List<Map.Entry<Enchantment, Integer>> entries = EnchantmentHelper.getEnchantments(stack).entrySet().stream().toList();
        if (!entries.isEmpty()) {
            int size = entries.size();
            if (settings.duration < 0) {
                settings.duration = 0;
            }
            String text = generateText(drawTick, size, entries);
            if (settings.displayOnTop) {
                guiGraphics.pose().translate(0.0f, 0.0f, 200.0f);
            }
            if (settings.xAxisCentered) {
                if (settings.bgColor != 0) {
                    guiGraphics.fill(leftX + settings.offsetX - font.width(text) / 2 - 2, topY + settings.offsetY - 2, leftX + settings.offsetX + font.width(text) / 2 + 2, topY + settings.offsetY + font.lineHeight + 2, settings.bgColor);
                }
                guiGraphics.drawCenteredString(font, text, leftX + settings.offsetX, topY + settings.offsetY, settings.fontColor);
            } else {
                if (settings.bgColor != 0) {
                    guiGraphics.fill(leftX + settings.offsetX - 2, topY + settings.offsetY - 2, leftX + settings.offsetX + font.width(text) + 2, topY + settings.offsetY + font.lineHeight + 2, settings.bgColor);
                }
                guiGraphics.drawString(font, text, leftX + settings.offsetX, topY + settings.offsetY, settings.fontColor);
            }
        }
    }

    private static String generateText(int drawTick, int size, List<Map.Entry<Enchantment, Integer>> entries) {
        ModConfig.Screen settings = config.screen;
        int index = settings.duration == 0 ? 0 : drawTick / settings.duration % size;
        Enchantment enchantment = entries.get(index).getKey();
        int level = entries.get(index).getValue();
        String fullName = enchantment.getFullname(level).getString();
        String name = Component.translatable(enchantment.getDescriptionId()).getString();
        String levelText = "";
        if (level != 1 || enchantment.getMaxLevel() != 1) {
            levelText = Component.translatable("enchantment.level." + level).getString();
        }
        String text;
        if (size > 1) {
            text = settings.pluralFormat.replace(FULL_NAME, fullName).replace(NAME, name).replace(LEVEL, levelText).replace(INDEX, String.valueOf(index + 1)).replace(SIZE, String.valueOf(size));
        } else {
            text = settings.singularFormat.replace(FULL_NAME, fullName).replace(NAME, name).replace(LEVEL, levelText);
        }
        return text;
    }

    public static void renderInHud(Minecraft mc, GuiGraphics guiGraphics) {
        setConfigIfAbsent();
        if (config == null || mc == null) return;
        ModConfig.Hud settings = config.hud;
        if (!settings.enabled) return;
        List<MerchantOffer> offers = getMerchantOffers(mc, settings);
        if (offers.isEmpty()) return;
        int x = settings.offsetX;
        int y = settings.offsetY;
        int paddingY = settings.paddingY;
        MerchantOfferRenderer renderer = new MerchantOfferRenderer(mc, guiGraphics, settings, stack -> shouldPass(stack, settings));
        int maxX = 0;
        for (MerchantOffer offer : offers) {
            Vec2 dimensions = renderer.calculateDimensions(offer, false);
            if (y + dimensions.y + paddingY > mc.getWindow().getGuiScaledHeight()) {
                y = settings.offsetY;
                x += maxX + paddingY * 3;
            }
            renderer.render(offer, x, y);
            y += (int) dimensions.y + paddingY * 3;
            maxX = Math.max(maxX, x + (int) dimensions.x);
        }
    }

    private static List<MerchantOffer> getMerchantOffers(Minecraft mc, ModConfig.Hud settings) {
        MerchantOfferAcquisitionMethod acquisitionMethod = settings.acquisitionMethod;
        if (acquisitionMethod.shouldUseCache()) {
            if (mc.hitResult instanceof EntityHitResult hitResult && hitResult.getType() != EntityHitResult.Type.MISS) {
                Entity entity = hitResult.getEntity();
                if (entity instanceof AbstractVillager merchant) {
                    UUID uuid = merchant.getUUID();
                    MerchantOfferCache cache = MerchantOfferCache.getInstance();
                    MerchantOfferInfo info = cache.get(uuid);
                    if (info == null && acquisitionMethod.shouldSimulate()) {
                        info = MerchantOfferInfo.getInstance();
                        if (info.hasOffers()) {
                            cache.set(uuid, info.copy());
                        } else {
                            info = null;
                        }
                    }
                    return info == null ? List.of() : info.getOffers(offer -> !shouldPass(offer.getResult(), settings));
                }
            }
        }
        MerchantOfferInfo info = MerchantOfferInfo.getInstance();
        if (!info.hasOffers()) return List.of();
        return info.getOffers(offer -> !shouldPass(offer.getResult(), settings));
    }

    public static boolean shouldPass(ItemStack stack, ModConfig.Screen settings) {
        return !stack.is(Items.ENCHANTED_BOOK) && (settings.onlyEnchantedBooks || !stack.isEnchanted());
    }

    public static boolean shouldPass(ItemStack stack, ModConfig.Hud settings) {
        return !stack.is(Items.ENCHANTED_BOOK) && (settings.onlyEnchantedBooks || !stack.isEnchanted());
    }

    public static void update() {
        setConfigIfAbsent();
        if (config == null) return;
        ModConfig.Hud settings = config.hud;
        if (!settings.enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        AbstractVillager merchant = getValidMerchant(mc);
        MerchantOfferInfo info = MerchantOfferInfo.getInstance();
        if (merchant != null) {
            if (info.hasId(merchant.getId())) return;
            info.clearOffers();
            info.setId(merchant.getId());
//            if (settings.acquisitionMethod.shouldUseCache() && MerchantOfferCache.getInstance().get(merchant.getUUID()) != null) {
//                return;
//            }
            ClientPacketListener connection = mc.getConnection();
            if (connection != null) {
//                connection.send(new ServerboundPickItemPacket(mc.player.getInventory().selected));
                connection.send(ServerboundInteractPacket.createInteractionPacket(merchant, mc.player.isShiftKeyDown(), InteractionHand.MAIN_HAND));
            }
        } else if (!TradeEnchantmentDisplay.isTrading() || !AutoConfig.getConfigHolder(ModConfig.class).get().hud.acquisitionMethod.shouldUseCache()) {
            info.clearId();
        }
    }

    public static AbstractVillager getValidMerchant(Minecraft mc) {
        if (mc == null) return null;
        LocalPlayer player = mc.player;
        if (player == null) return null;
        if (TradeEnchantmentDisplay.isTrading()) return null;
        HitResult hitResult = mc.hitResult;
        if (!(hitResult instanceof EntityHitResult entityHitResult) || entityHitResult.getType() == EntityHitResult.Type.MISS) {
            return null;
        }
        Entity entity = entityHitResult.getEntity();
        if (!(entity instanceof AbstractVillager merchant)) return null;
        if (merchant instanceof Villager villager) {
            VillagerProfession profession = villager.getVillagerData().getProfession();
            if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
                return null;
            }
            ItemStack stack = player.getMainHandItem();
            if (stack.is(Items.VILLAGER_SPAWN_EGG) || stack.is(Items.NAME_TAG)) {
                return null;
            }
        }
        return merchant;
    }

    private static void setConfigIfAbsent() {
        if (config == null) {
            config = AutoConfig.getConfigHolder(ModConfig.class).get();
        }
    }
}
