package com.imoonday.tradeenchantmentdisplay.util;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.mixin.MinecraftServerAccessor;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffers;
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
    private static final long DEBOUNCE_DELAY = 1000;
    private static int errorCount = 0;
    private Timer timer;

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
        saveWithDebounce();
        return contains;
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
        saveWithDebounce();
        unmarkRequested(uuid);
    }

    public boolean removeIfExist(UUID uuid) {
        if (contains(uuid)) {
            remove(uuid);
            return true;
        }
        return false;
    }

    public void saveWithDebounce() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                save();
            }
        }, DEBOUNCE_DELAY);
    }

    public boolean contains(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public void save() {
        try {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;
            if (!ModConfig.getCache().enabled) return;
            String name = getCurrentWorldName();
            if (name == null) return;

            CompoundTag uuids = new CompoundTag();
            cache.forEach((uuid, info) -> uuids.put(uuid.toString(), MerchantOffers.CODEC.encodeStart(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), info.createOffers()).getOrThrow()));
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
                errorCount = 0;
            } catch (IOException e) {
                LOGGER.error("Failed to save cache", e);
            }
        } catch (Exception e) {
            LOGGER.error("Error while saving cache, stopping saving. If this error persist, please try to delete cache file {} and report this issue", getCacheFile(), e);
            handleError();
        }
    }

    private static void handleError() {
        if (++errorCount > 10) {
            errorCount = 0;
            LOGGER.error("Too many errors while saving or loading cache, disabling cache");
            ModConfig.getCache().enabled = false;
            ModConfig.save();
        }
    }

    @Nullable
    public static String getCurrentWorldName() {
        String name = null;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.hasSingleplayerServer()) {
                name = ((MinecraftServerAccessor) mc.getSingleplayerServer()).getStorageSource().getLevelId();
            } else {
                ServerData data = mc.getCurrentServer();
                if (data != null) {
                    name = ModConfig.getCache().distinguishPortBetweenServers ? data.ip : data.ip.split(":")[0];
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get current world name", e);
        }
        return name;
    }

    public void clear() {
        cache.clear();
        saveWithDebounce();
        clearRequestedIds();
    }

    public void update(UUID uuid, MerchantOfferInfo info) {
        MerchantOfferInfo oldInfo = get(uuid);
        if (oldInfo != null) {
            oldInfo.update(info);
            saveWithDebounce();
        } else {
            set(uuid, info);
        }
    }

    public void load() {
        try {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;
            if (!ModConfig.getCache().enabled) return;
            LOGGER.info("Loading cache");
            String name = getCurrentWorldName();
            if (name == null) return;

            CompoundTag root;
            File file = getCacheFile();
            if (!file.exists()) {
                LOGGER.warn("No cache file exist");
                return;
            }
            try {
                root = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
            } catch (IOException e) {
                LOGGER.error("Failed to read cache file", e);
                return;
            }
            if (!root.contains(name)) {
                LOGGER.info("No cache for current world");
                return;
            }

            int count = 0;

            CompoundTag uuids = root.getCompound(name);
            for (String key : uuids.getAllKeys()) {
                CompoundTag offers = uuids.getCompound(key);
                Optional<MerchantOffers> optional = MerchantOffers.CODEC.parse(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), offers).resultOrPartial(Util.prefix("Failed to load offers: ", LOGGER::warn));
                if (optional.isPresent()) {
                    MerchantOffers merchantOffers = optional.get();
                    MerchantOfferInfo info = new MerchantOfferInfo(merchantOffers);
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(key);
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Failed to parse UUID", e);
                        continue;
                    }
                    cache.put(uuid, info);
                    count++;
                }
            }

            errorCount = 0;
            LOGGER.info("Loaded {} offers from cache", count);
        } catch (Exception e) {
            LOGGER.error("Error while loading cache, stopping loading. If this error persist, please try to delete cache file {} and report this issue", getCacheFile(), e);
            handleError();
        }
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