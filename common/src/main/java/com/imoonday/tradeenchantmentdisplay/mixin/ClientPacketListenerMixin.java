package com.imoonday.tradeenchantmentdisplay.mixin;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferCache;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferInfo;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleMerchantOffers", at = @At("HEAD"), cancellable = true)
    private void handleMerchantOffers(ClientboundMerchantOffersPacket packet, CallbackInfo ci) {
        MerchantOfferInfo.getInstance().setOffers(packet.getOffers());
        if (!TradeEnchantmentDisplay.isTrading()) {
            ci.cancel();
        } else if (AutoConfig.getConfigHolder(ModConfig.class).get().hud.acquisitionMethod.shouldUseCache()) {
            Minecraft mc = Minecraft.getInstance();
            MerchantOfferInfo.getInstance().getId().ifPresent(id -> {
                if (mc.level != null) {
                    Entity entity = mc.level.getEntity(id);
                    if (entity != null) {
                        MerchantOfferCache.getInstance().set(entity.getUUID(), MerchantOfferInfo.getInstance().copy());
                    }
                }
            });
        }
    }

    @Inject(at = @At("HEAD"), method = "handleOpenScreen", cancellable = true)
    public void onOpenScreen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        var type = packet.getType();
        if (!TradeEnchantmentDisplay.isTrading() && type == MenuType.MERCHANT) {
            ci.cancel();
            Minecraft.getInstance().getConnection().send(new ServerboundContainerClosePacket(packet.getContainerId()));
        }
    }
}
