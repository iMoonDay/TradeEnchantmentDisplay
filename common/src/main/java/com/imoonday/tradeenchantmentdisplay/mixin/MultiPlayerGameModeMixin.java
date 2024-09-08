package com.imoonday.tradeenchantmentdisplay.mixin;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferInfo;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "interact", at = @At("HEAD"))
    private void interact(Player player, Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (target instanceof AbstractVillager) {
            AbstractVillager villager = (AbstractVillager) target;
            MerchantOfferInfo info = MerchantOfferInfo.getInstance();
            info.getId().ifPresent(id -> {
                if (villager.getId() == id && !info.getOffers().isEmpty()) {
                    TradeEnchantmentDisplay.setTrading(true);
                }
            });
        }
    }
}
