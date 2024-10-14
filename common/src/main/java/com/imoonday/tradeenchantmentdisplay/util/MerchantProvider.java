package com.imoonday.tradeenchantmentdisplay.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MerchantProvider {

    private static final Map<ResourceLocation, Provider> PROVIDERS = new ConcurrentHashMap<>();
    private static final Provider EMPTY_PROVIDER = (level, pos) -> null;

    public static void register(ResourceLocation id, Provider provider) {
        PROVIDERS.put(id, provider);
    }

    public static void unregister(ResourceLocation id) {
        PROVIDERS.remove(id);
    }

    public static Map<ResourceLocation, Provider> getProviders() {
        return Map.copyOf(PROVIDERS);
    }

    public static Provider getProvider(ResourceLocation id) {
        return PROVIDERS.getOrDefault(id, EMPTY_PROVIDER);
    }

    public static Pair<Merchant, UUID> getMerchant(Level level, BlockPos pos) {
        return PROVIDERS.values().stream().map(provider -> provider.get(level, pos))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
    }

    public interface Provider {

        @Nullable
        Pair<@NotNull Merchant, @NotNull UUID> get(Level level, BlockPos pos);
    }
}
