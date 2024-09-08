package com.imoonday.tradeenchantmentdisplay.util;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class MerchantOfferHandler {

    public static final int MAX_WAITING_TIME = 10;
    private static int waitingTime = 0;

    public static void clientWorldTick() {
        updateWaitingTime();
        if (!MerchantOfferUtils.shouldRequestingOffers()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        AbstractVillager merchant = getValidMerchant(mc);
        MerchantOfferInfo info = MerchantOfferInfo.getInstance();
        if (merchant != null && !isWaiting()) {
            if (info.hasId(merchant.getId())) return;
            info.clearOffers();
            info.setId(merchant.getId());
            sendRequest(merchant);
        } else if (!TradeEnchantmentDisplay.isTrading()) {
            info.clearId();
        }
    }

    public static boolean isWaiting() {
        return waitingTime > 0;
    }

    public static void startWaiting() {
        startWaiting(MAX_WAITING_TIME);
    }

    public static void startWaiting(int time) {
        waitingTime = time;
    }

    public static void updateWaitingTime() {
        if (waitingTime > 0) {
            waitingTime--;
        }
    }

    public static AbstractVillager getValidMerchant(Minecraft mc) {
        if (mc == null) return null;
        LocalPlayer player = mc.player;
        if (player == null) return null;
        if (TradeEnchantmentDisplay.isTrading()) return null;
        HitResult hitResult = mc.hitResult;
        if (!(hitResult instanceof EntityHitResult) || hitResult.getType() == EntityHitResult.Type.MISS) {
            return null;
        }
        EntityHitResult entityHitResult = (EntityHitResult) hitResult;
        Entity entity = entityHitResult.getEntity();
        if (!(entity instanceof AbstractVillager)) return null;
        AbstractVillager merchant = (AbstractVillager) entity;
        if (merchant instanceof Villager) {
            Villager villager = (Villager) merchant;
            VillagerProfession profession = villager.getVillagerData().getProfession();
            if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
                return null;
            }
            ItemStack stack = player.getMainHandItem();
            if (MerchantOfferUtils.checkInteractableWithVillager(stack)) {
                startWaiting();
                return null;
            }
        }
        return merchant;
    }

    public static void clientTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        MultiPlayerGameMode gameMode = mc.gameMode;
        if (player != null && mc.level != null && gameMode != null) {
            boolean tryInteract = ModConfig.getGeneric().alwaysAttemptToGetNearbyOffers && MerchantOfferUtils.shouldRequestingOffers();
            if (tryInteract) {
                MerchantOfferCache cache = MerchantOfferCache.getInstance();
                List<Entity> entities = mc.level.getEntities(player, player.getBoundingBox().inflate(gameMode.getPickRange()));
                entities.sort(Comparator.comparingDouble(e -> e.distanceTo(player)));
                int cooldown = ModConfig.getGeneric().requestFrequency;
                for (Entity entity : entities) {
                    if (!cache.contains(entity.getUUID()) && MerchantOfferUtils.isValidMerchant(entity) && !MerchantOfferCache.isRequested(entity) && MerchantOfferUtils.tryRequest(entity)) {
                        MerchantOfferCache.markRequested(entity);
                        startWaiting(cooldown);
                        break;
                    }
                }
            }
        }
    }

    public static void onEntityRemoved(Entity entity) {
        if (!(entity instanceof AbstractVillager)) return;
        if (entity.isAlive()) return;
        MerchantOfferCache cache = MerchantOfferCache.getInstance();
        UUID uuid = entity.getUUID();
        cache.removeIfExist(uuid);
    }

    public static boolean sendRequest(AbstractVillager merchant) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.send(new ServerboundInteractPacket(merchant, InteractionHand.MAIN_HAND, Minecraft.getInstance().player.isShiftKeyDown()));
            return true;
        }
        return false;
    }

    public static void onJoined() {
        MerchantOfferCache.getInstance().load();
    }

    public static void onDisconnected() {
        MerchantOfferCache.getInstance().save();
        MerchantOfferCache.getInstance().clear();
        MerchantOfferCache.clearRequestedIds();
    }
}
