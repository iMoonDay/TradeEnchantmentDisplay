package com.imoonday.tradeenchantmentdisplay.util;

import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class MerchantOfferInfo {

    private static final MerchantOfferInfo INSTANCE = new MerchantOfferInfo();
    @Nullable
    private Integer id;
    private List<MerchantOffer> offers = new ArrayList<>();

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
        this.offers = new ArrayList<>(offers);
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
}
