package com.imoonday.tradeenchantmentdisplay.mixin;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferCache;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferInfo;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleMerchantOffers", at = @At("HEAD"), cancellable = true)
    private void handleMerchantOffers(ClientboundMerchantOffersPacket packet, CallbackInfo ci) {
        MerchantOfferInfo.getInstance().setOffers(packet.getOffers());
        updateOrSetOffer();
        if (MerchantOfferUtils.shouldRequestingOffers() && !TradeEnchantmentDisplay.isTrading()) {
            ci.cancel();
        }
    }

    @Unique
    private void updateOrSetOffer() {
        MerchantOfferInfo.getInstance().getId().ifPresent(id -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                Entity entity = minecraft.level.getEntity(id);
                if (entity != null) {
                    MerchantOfferCache cache = MerchantOfferCache.getInstance();
                    MerchantOfferInfo info = MerchantOfferInfo.getInstance().copy();
                    if (TradeEnchantmentDisplay.isTrading()) {
                        cache.set(entity.getUUID(), info);
                    } else {
                        cache.update(entity.getUUID(), info);
                    }
                }
            }
        });
    }

    @Inject(at = @At("HEAD"), method = "handleOpenScreen", cancellable = true)
    public void onOpenScreen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        if (!MerchantOfferUtils.shouldRequestingOffers()) return;
        MenuType<?> type = packet.getType();
        if (type != MenuType.MERCHANT) return;
        if (!TradeEnchantmentDisplay.isTrading()) {
            ci.cancel();
            Minecraft.getInstance().getConnection().send(new ServerboundContainerClosePacket(packet.getContainerId()));
        }
    }
}
