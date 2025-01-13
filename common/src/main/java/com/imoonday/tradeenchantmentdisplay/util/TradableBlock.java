package com.imoonday.tradeenchantmentdisplay.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public interface TradableBlock {

    TradableBlock TRUE = (state, stack, level, player, hand, blockHitResult) -> true;
    TradableBlock FALSE = (state, stack, level, player, hand, blockHitResult) -> false;

    boolean checkTrading(BlockState state, @Nullable ItemStack stack, Level level, Player player, @Nullable InteractionHand hand, BlockHitResult blockHitResult);
}
