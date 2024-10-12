package com.imoonday.tradeenchantmentdisplay.util;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.renderer.EnchantmentRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.EntityHitResult;

import java.util.*;

public class MerchantOfferUtils {

    public static List<MerchantOffer> getMerchantOffers(Minecraft mc, boolean onlyEnchantedBooks) {
        if (!(mc.hitResult instanceof EntityHitResult) || mc.hitResult.getType() == EntityHitResult.Type.MISS) {
            return Collections.emptyList();
        }
        EntityHitResult hitResult = (EntityHitResult) mc.hitResult;

        Entity entity = hitResult.getEntity();
        if (entity instanceof Merchant) {
            UUID uuid = entity.getUUID();
            MerchantOfferCache cache = MerchantOfferCache.getInstance();
            MerchantOfferInfo info = cache.get(uuid);
            if (info != null && entity instanceof Villager) {
                Villager villager = (Villager) entity;
                VillagerProfession profession = villager.getVillagerData().getProfession();
                if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
                    cache.remove(uuid);
                    info = null;
                }
            }
            if (info == null) {
                info = MerchantOfferInfo.getInstance();
                if (info.hasOffers()) {
                    cache.set(uuid, info.copy());
                } else {
                    info = null;
                }
            }
            return info == null ? Collections.emptyList() : info.getOffers(offer -> !EnchantmentRenderer.shouldPass(offer.getResult(), onlyEnchantedBooks));
        }

        MerchantOfferInfo info = MerchantOfferInfo.getInstance();
        if (!info.hasOffers()) return Collections.emptyList();
        return info.getOffers(offer -> !EnchantmentRenderer.shouldPass(offer.getResult(), onlyEnchantedBooks));
    }

    public static boolean tryRequest(Entity entity) {
        if (!isValidMerchant(entity)) return false;
        MerchantOfferInfo.getInstance().setId(entity.getId());
        return MerchantOfferHandler.sendRequest(entity);
    }

    public static boolean isValidMerchant(Entity entity) {
        if (MerchantOfferHandler.isWaiting()) return false;
        Minecraft mc = Minecraft.getInstance();
        MultiPlayerGameMode gameMode = mc.gameMode;
        if (gameMode == null) return false;
        LocalPlayer player = mc.player;
        if (player == null) return false;
        if (!(entity instanceof Merchant)) return false;
        double distanceSqr = entity.getBoundingBox().getCenter().distanceToSqr(player.getEyePosition(1.0f));
        float range = gameMode.getPickRange();
        if (distanceSqr > range * range) return false;
        if (entity instanceof Villager) {
            Villager villager = (Villager) entity;
            VillagerProfession profession = villager.getVillagerData().getProfession();
            if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
                return false;
            }
            ItemStack stack = player.getMainHandItem();
            if (checkInteractableWithVillager(stack)) {
                MerchantOfferHandler.startWaiting();
                return false;
            }
        }
        return true;
    }

    public static boolean checkInteractableWithVillager(ItemStack stack) {
        return ModConfig.getGeneric().nonInteractableItems
                .stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .map(Registry.ITEM::getOptional)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(item -> stack.getItem() == item);
    }

    public static boolean shouldRequestingOffers() {
        return ModConfig.getHud().enabled || ModConfig.getMerchant().enabled;
    }
}
