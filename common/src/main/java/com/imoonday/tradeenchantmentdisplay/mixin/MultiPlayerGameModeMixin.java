package com.imoonday.tradeenchantmentdisplay.mixin;

import com.imoonday.tradeenchantmentdisplay.TradeEnchantmentDisplay;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferInfo;
import com.imoonday.tradeenchantmentdisplay.util.TradableBlock;
import com.imoonday.tradeenchantmentdisplay.util.TradableBlockManager;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "interact", at = @At("HEAD"))
    private void interact(Player player, Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (target instanceof Merchant) {
            MerchantOfferInfo info = MerchantOfferInfo.getInstance();
            info.getId().ifPresent(id -> {
                if (target.getId() == id && !info.getOffers().isEmpty()) {
                    TradeEnchantmentDisplay.setTrading(true);
                }
            });
        }
    }

    @Redirect(method = "performUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;useItemOn(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/ItemInteractionResult;"))
    private ItemInteractionResult useItemOn(BlockState instance, ItemStack stack, Level level, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        ItemInteractionResult result = instance.useItemOn(stack, level, player, hand, blockHitResult);
        if (result.consumesAction()) {
            TradableBlock tradableBlock = TradableBlockManager.getTradableBlock(instance, level, blockHitResult.getBlockPos(), player);
            if (tradableBlock.checkTrading(instance, stack, level, player, hand, blockHitResult)) {
                TradeEnchantmentDisplay.setTrading(true);
            }
        }
        return result;
    }

    @Redirect(method = "performUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;useWithoutItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult useWithoutItem(BlockState instance, Level level, Player player, BlockHitResult blockHitResult) {
        InteractionResult result = instance.useWithoutItem(level, player, blockHitResult);
        if (result.consumesAction()) {
            TradableBlock tradableBlock = TradableBlockManager.getTradableBlock(instance, level, blockHitResult.getBlockPos(), player);
            if (tradableBlock.checkTrading(instance, null, level, player, null, blockHitResult)) {
                TradeEnchantmentDisplay.setTrading(true);
            }
        }
        return result;
    }
}
