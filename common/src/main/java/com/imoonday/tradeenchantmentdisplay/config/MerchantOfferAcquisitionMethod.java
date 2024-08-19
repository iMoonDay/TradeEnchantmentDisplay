package com.imoonday.tradeenchantmentdisplay.config;

import net.minecraft.network.chat.Component;

public enum MerchantOfferAcquisitionMethod {
    SIMULATED_TRADING(true, false),
    TRADING_CACHE(false, true),
    SIMULATED_AND_CACHE(true, true);

    private final Component name = Component.translatable("tradeenchantmentdisplay.merchant_offer_acquisition_method." + name().toLowerCase());
    private final boolean simulate;
    private final boolean cache;

    MerchantOfferAcquisitionMethod(boolean simulate, boolean cache) {
        this.simulate = simulate;
        this.cache = cache;
    }

    public Component getName() {
        return name;
    }

    public boolean shouldSimulate() {
        return simulate;
    }

    public boolean shouldUseCache() {
        return cache;
    }
}
