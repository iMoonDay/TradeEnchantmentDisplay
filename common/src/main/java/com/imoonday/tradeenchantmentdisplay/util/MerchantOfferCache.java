package com.imoonday.tradeenchantmentdisplay.util;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MerchantOfferCache {

    private static final MerchantOfferCache INSTANCE = new MerchantOfferCache();
    private final Map<UUID, MerchantOfferInfo> cache = new HashMap<>();

    public static MerchantOfferCache getInstance() {
        return INSTANCE;
    }

    @Nullable
    public MerchantOfferInfo get(UUID uuid) {
        return cache.get(uuid);
    }

    @Nullable
    public MerchantOfferInfo get(int id) {
        return cache.values().stream().filter(info -> info.hasId(id)).findFirst().orElse(null);
    }

    public boolean set(UUID uuid, MerchantOfferInfo info) {
        boolean contains = cache.containsKey(uuid);
        cache.put(uuid, info);
        return contains;
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
    }

    public void clear() {
        cache.clear();
    }
}
