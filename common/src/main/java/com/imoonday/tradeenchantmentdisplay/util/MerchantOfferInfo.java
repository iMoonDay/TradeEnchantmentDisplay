package com.imoonday.tradeenchantmentdisplay.util;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
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
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MerchantOfferInfo {

    public static final int MAX_WAITING_TIME = 10;
    private static final MerchantOfferInfo INSTANCE = new MerchantOfferInfo();
    private static int waitingTime = 0;
    @Nullable
    private Integer id;
    private List<MerchantOffer> offers = new ArrayList<>();


    public MerchantOfferInfo() {

    }

    public MerchantOfferInfo(List<MerchantOffer> offers) {
        this.setOffers(offers);
    }

    public static MerchantOfferInfo getInstance() {
        return INSTANCE;
    }

    public List<MerchantOffer> getOffers() {
        return offers;
    }

    public List<MerchantOffer> getOffers(Predicate<MerchantOffer> predicate) {
        return offers.stream().filter(predicate).toList();
    }

    public void setOffers(List<MerchantOffer> offers) {
        this.offers = offers.stream().map(MerchantOfferInfo::copyOffer).collect(Collectors.toCollection(ArrayList::new));
    }

    public void clearOffers() {
        offers = new ArrayList<>();
    }

    public boolean hasOffers() {
        return getId().isPresent() && !getOffers().isEmpty();
    }

    public Optional<Integer> getId() {
        return Optional.ofNullable(id);
    }

    public void setId(@Nullable Integer id) {
        this.id = id;
    }

    public boolean hasId(int id) {
        return getId().map(lastId -> lastId == id).orElse(false);
    }

    public void clearId() {
        setId(null);
    }

    public void copy(MerchantOfferInfo info) {
        setId(info.getId().orElse(null));
        setOffers(info.getOffers());
    }

    public MerchantOfferInfo copy() {
        MerchantOfferInfo info = new MerchantOfferInfo();
        info.copy(this);
        return info;
    }

    public void update(MerchantOfferInfo info) {
        setId(info.getId().orElse(null));
        updateOffers(info.getOffers());
    }

    public void updateOffers(List<MerchantOffer> offers) {
        List<MerchantOffer> oldOffers = getOffers();
        int oldSize = oldOffers.size();
        for (int i = 0; i < offers.size(); i++) {
            MerchantOffer newOffer = offers.get(i);
            if (oldSize > i) {
                if (!isSameMerchantOffer(oldOffers.get(i), newOffer)) {
                    oldOffers.set(i, newOffer);
                }
            } else {
                oldOffers.add(newOffer);
            }
        }
    }

    public static boolean isSameMerchantOffer(MerchantOffer oldOffer, MerchantOffer newOffer) {
        return ItemStack.isSameItemSameTags(oldOffer.getBaseCostA(), newOffer.getBaseCostA()) && ItemStack.isSameItemSameTags(oldOffer.getCostB(), newOffer.getCostB()) && ItemStack.isSameItemSameTags(oldOffer.getResult(), newOffer.getResult());
    }

    public static MerchantOffer copyOffer(MerchantOffer offer) {
        return new MerchantOffer(offer.createTag());
    }

    public static void update() {
        updateWaitingTime();
        ModConfig.Hud settings = ModConfig.getHud();
        if (!settings.enabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        AbstractVillager merchant = getValidMerchant(mc);
        MerchantOfferInfo info = getInstance();
        if (merchant != null && !isWaiting()) {
            if (info.hasId(merchant.getId())) return;
            info.clearOffers();
            info.setId(merchant.getId());
            ClientPacketListener connection = mc.getConnection();
            if (connection != null) {
                connection.send(ServerboundInteractPacket.createInteractionPacket(merchant, mc.player.isShiftKeyDown(), InteractionHand.MAIN_HAND));
            }
        } else if (!TradeEnchantmentDisplay.isTrading()) {
            info.clearId();
        }
    }

    public static boolean isWaiting() {
        return waitingTime > 0;
    }

    public static void startWaiting() {
        waitingTime = MAX_WAITING_TIME;
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
                startWaiting();
                return null;
            }
        }
        return merchant;
    }
}
