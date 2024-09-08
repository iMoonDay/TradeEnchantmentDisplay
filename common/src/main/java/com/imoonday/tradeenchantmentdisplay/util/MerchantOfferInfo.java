package com.imoonday.tradeenchantmentdisplay.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MerchantOfferInfo {

    private static final MerchantOfferInfo INSTANCE = new MerchantOfferInfo();
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

    public static boolean isSameMerchantOffer(MerchantOffer oldOffer, MerchantOffer newOffer) {
        return ItemStack.isSameItemSameTags(oldOffer.getBaseCostA(), newOffer.getBaseCostA()) && ItemStack.isSameItemSameTags(oldOffer.getCostB(), newOffer.getCostB()) && ItemStack.isSameItemSameTags(oldOffer.getResult(), newOffer.getResult());
    }

    public static MerchantOffer copyOffer(MerchantOffer offer) {
        return new MerchantOffer(offer.createTag());
    }

    public List<MerchantOffer> getOffers() {
        return offers;
    }

    public void setOffers(List<MerchantOffer> offers) {
        this.offers = offers.stream().map(MerchantOfferInfo::copyOffer).collect(Collectors.toCollection(ArrayList::new));
    }

    public List<MerchantOffer> getOffers(Predicate<MerchantOffer> predicate) {
        return offers.stream().filter(predicate).toList();
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
        int newSize = offers.size();
        List<MerchantOffer> oldOffers = getOffers();
        if (newSize == 0) {
            oldOffers.clear();
            return;
        }
        int oldSize = oldOffers.size();
        if (oldSize > newSize) {
            oldOffers.subList(newSize, oldSize).clear();
        }
        for (int i = 0; i < newSize; i++) {
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
}
