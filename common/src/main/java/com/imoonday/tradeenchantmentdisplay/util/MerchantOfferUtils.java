package com.imoonday.tradeenchantmentdisplay.util;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.renderer.EnchantmentRenderer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class MerchantOfferUtils {

    public static List<MerchantOffer> getMerchantOffers(Minecraft mc, boolean onlyEnchantedBooks) {
        HitResult result = mc.hitResult;
        if (result == null || result.getType() == EntityHitResult.Type.MISS) return List.of();
        Merchant merchant = null;
        UUID uuid = null;
        if (result instanceof EntityHitResult hitResult) {
            Entity entity = hitResult.getEntity();
            if (entity instanceof Merchant m) {
                merchant = m;
                uuid = entity.getUUID();
            }
        } else if (result instanceof BlockHitResult hitResult) {
            Pair<Merchant, UUID> pair = MerchantProvider.getMerchant(mc.level, hitResult.getBlockPos());
            merchant = pair.getFirst();
            uuid = pair.getSecond();
        }
        if (merchant != null && uuid != null) {
            MerchantOfferCache cache = MerchantOfferCache.getInstance();
            MerchantOfferInfo info = cache.get(uuid);
            if (info != null && merchant instanceof Villager villager) {
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
            return info == null ? List.of() : info.getOffers(offer -> !EnchantmentRenderer.shouldPass(offer.getResult(), onlyEnchantedBooks));
        }

        MerchantOfferInfo info = MerchantOfferInfo.getInstance();
        if (!info.hasOffers()) return List.of();
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
        double distanceSqr = entity.getBoundingBox().distanceToSqr(player.getEyePosition());
        float range = gameMode.getPickRange();
        if (distanceSqr > range * range) return false;
        if (entity instanceof Villager villager) {
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
                .map(BuiltInRegistries.ITEM::getOptional)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(stack::is);
    }

    public static boolean shouldRequestingOffers() {
        return ModConfig.getHud().enabled || ModConfig.getMerchant().enabled;
    }
}
