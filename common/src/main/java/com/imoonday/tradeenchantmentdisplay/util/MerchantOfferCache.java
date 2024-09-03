package com.imoonday.tradeenchantmentdisplay.util;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.mixin.MinecraftServerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay.LOGGER;

public class MerchantOfferCache {

    private static final MerchantOfferCache INSTANCE = new MerchantOfferCache();
    private static final Set<UUID> REQUESTED_IDS = new HashSet<>();
    private static File cacheFile;
    private final Map<UUID, MerchantOfferInfo> cache = new HashMap<>();

    public static MerchantOfferCache getInstance() {
        return INSTANCE;
    }

    public MerchantOfferInfo get(UUID uuid) {
        return cache.get(uuid);
    }

    public MerchantOfferInfo get(int id) {
        return cache.values().stream().filter(info -> info.hasId(id)).findFirst().orElse(null);
    }

    public boolean set(UUID uuid, MerchantOfferInfo info) {
        boolean contains = cache.containsKey(uuid);
        cache.put(uuid, info);
        save();
        return contains;
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
        save();
        unmarkRequested(uuid);
    }

    public boolean removeIfExist(UUID uuid) {
        if (contains(uuid)) {
            remove(uuid);
            return true;
        }
        return false;
    }

    public void clear() {
        cache.clear();
        save();
        clearRequestedIds();
    }

    public boolean contains(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public void update(UUID uuid, MerchantOfferInfo info) {
        MerchantOfferInfo oldInfo = get(uuid);
        if (oldInfo != null) {
            oldInfo.update(info);
            save();
        } else {
            set(uuid, info);
        }
    }

    public void save() {
        if (!ModConfig.getCache().enabled) return;
        String name = getCurrentWorldName();
        if (name == null) {
            LOGGER.warn("Failed to get current world name");
            return;
        }
        CompoundTag uuids = new CompoundTag();
        cache.forEach((uuid, info) -> {
            ListTag offers = new ListTag();
            for (MerchantOffer offer : info.getOffers()) {
                offers.add(offer.createTag());
            }
            uuids.put(uuid.toString(), offers);
        });
        CompoundTag root = new CompoundTag();
        root.put(name, uuids);
        try {
            File file = getCacheFile();
            if (!file.exists() && !file.createNewFile()) {
                LOGGER.warn("Failed to create cache file");
                return;
            }
            CompoundTag oldCache;
            try {
                oldCache = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
                oldCache.remove(name);
            } catch (IOException e) {
                oldCache = new CompoundTag();
            }
            oldCache.merge(root);
            NbtIo.writeCompressed(oldCache, file.toPath());
        } catch (IOException e) {
            LOGGER.error("Failed to save cache");
            System.out.println(e);
        }
    }

    public void load() {
        if (!ModConfig.getCache().enabled) return;
        LOGGER.info("Loading cache");
        String name = getCurrentWorldName();
        if (name == null) {
            LOGGER.warn("Failed to get current world name");
            return;
        }
        CompoundTag root;
        File file = getCacheFile();
        if (!file.exists()) {
            LOGGER.warn("No cache file exist");
            return;
        }
        try {
            root = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            LOGGER.error("Failed to read cache file");
            System.out.println(e);
            return;
        }
        if (!root.contains(name)) {
            LOGGER.info("No cache for current world");
            return;
        }
        CompoundTag uuids = root.getCompound(name);
        uuids.getAllKeys().forEach(key -> {
            ListTag offers = uuids.getList(key, Tag.TAG_COMPOUND);
            List<MerchantOffer> list = offers.stream().map(tag -> new MerchantOffer((CompoundTag) tag)).toList();
            MerchantOfferInfo info = new MerchantOfferInfo(list);
            cache.put(UUID.fromString(key), info);
        });
        LOGGER.info("Cache loaded");
    }

    @Nullable
    public static String getCurrentWorldName() {
        String name = null;
        Minecraft mc = Minecraft.getInstance();
        try {
            if (mc.hasSingleplayerServer()) {
                name = ((MinecraftServerAccessor) mc.getSingleplayerServer()).getStorageSource().getLevelId();
            } else {
                ServerData data = mc.getCurrentServer();
                if (data != null) {
                    name = ModConfig.getCache().distinguishPortBetweenServers ? data.ip : data.ip.split(":")[0];
                }
            }
        } catch (Exception e) {
            return null;
        }
        return name;
    }

    private static File getCacheFile() {
        if (cacheFile == null) {
            String filePath = ModConfig.getCache().filePath;
            if (filePath.isEmpty()) {
                filePath = "trades.nbt";
            }
            cacheFile = Minecraft.getInstance().gameDirectory.toPath().resolve(filePath).toFile();
        }
        return cacheFile;
    }

    public static void markRequested(Entity entity) {
        REQUESTED_IDS.add(entity.getUUID());
    }

    public static void unmarkRequested(UUID uuid) {
        REQUESTED_IDS.remove(uuid);
    }

    public static boolean isRequested(Entity entity) {
        return REQUESTED_IDS.contains(entity.getUUID());
    }

    public static void clearRequestedIds() {
        REQUESTED_IDS.clear();
    }
}
