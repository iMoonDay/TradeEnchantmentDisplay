package com.imoonday.tradeenchantmentdisplay.util;

import com.imoonday.tradeenchantmentdisplay.renderer.EnchantmentRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;
import java.util.UUID;

public class MerchantOfferUtils {

    public static List<MerchantOffer> getMerchantOffers(Minecraft mc, boolean onlyEnchantedBooks) {
        if (!(mc.hitResult instanceof EntityHitResult hitResult) || hitResult.getType() == EntityHitResult.Type.MISS) {
            return List.of();
        }

        Entity entity = hitResult.getEntity();
        if (entity instanceof AbstractVillager merchant) {
            UUID uuid = merchant.getUUID();
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

    public static void tryInteract(Entity entity) {
        if (!isValidMerchant(entity)) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        MerchantOfferInfo.getInstance().setId(entity.getId());
        ClientPacketListener connection = mc.getConnection();
        if (connection != null) {
            connection.send(ServerboundInteractPacket.createInteractionPacket(entity, player.isShiftKeyDown(), InteractionHand.MAIN_HAND));
        }
    }

    public static boolean isValidMerchant(Entity entity) {
        if (MerchantOfferInfo.isWaiting()) return false;
        Minecraft mc = Minecraft.getInstance();
        MultiPlayerGameMode gameMode = mc.gameMode;
        if (gameMode == null) return false;
        LocalPlayer player = mc.player;
        if (player == null) return false;
        if (!(entity instanceof AbstractVillager merchant)) return false;
        double distanceSqr = entity.getBoundingBox().distanceToSqr(player.getEyePosition());
        float range = gameMode.getPickRange();
        if (distanceSqr > range * range) return false;
        if (merchant instanceof Villager villager) {
            VillagerProfession profession = villager.getVillagerData().getProfession();
            if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
                return false;
            }
            ItemStack stack = player.getMainHandItem();
            if (stack.is(Items.VILLAGER_SPAWN_EGG) || stack.is(Items.NAME_TAG)) {
                MerchantOfferInfo.startWaiting();
                return false;
            }
        }
        return true;
    }
}
