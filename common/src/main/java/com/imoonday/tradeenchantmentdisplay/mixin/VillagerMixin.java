package com.imoonday.tradeenchantmentdisplay.mixin;

import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferCache;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferInfo;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {

    @Shadow
    @Final
    private static EntityDataAccessor<VillagerData> DATA_VILLAGER_DATA;

    @Shadow
    public abstract VillagerData getVillagerData();

    private VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (level().isClientSide && DATA_VILLAGER_DATA.equals(key)) {
            UUID uuid = getUUID();
            MerchantOfferCache.unmarkRequested(uuid);
            MerchantOfferInfo info = MerchantOfferInfo.getInstance();
            int id = getId();
            if (info.hasId(id)) {
                info.clearId();
            }
            VillagerProfession profession = getVillagerData().getProfession();
            if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
                MerchantOfferCache cache = MerchantOfferCache.getInstance();
                cache.removeIfExist(uuid);
            } else {
                MerchantOfferUtils.tryRequest(this);
            }
        }
        super.onSyncedDataUpdated(key);
    }
}
