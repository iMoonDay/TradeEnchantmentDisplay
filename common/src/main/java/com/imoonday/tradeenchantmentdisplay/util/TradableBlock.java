package com.imoonday.tradeenchantmentdisplay.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public interface TradableBlock {

    TradableBlock TRUE = (state, level, player, hand, blockHitResult) -> true;
    TradableBlock FALSE = (state, level, player, hand, blockHitResult) -> false;

    boolean checkTrading(BlockState state, Level level, Player player, InteractionHand hand, BlockHitResult blockHitResult);
}
