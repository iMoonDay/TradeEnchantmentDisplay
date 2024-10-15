package com.imoonday.tradeenchantmentdisplay.util;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TradableBlockManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Block, TradableBlock> TRADABLE_BLOCKS = new ConcurrentHashMap<>();

    public static void addTradableBlock(Block block, TradableBlock tradableBlock) {
        TRADABLE_BLOCKS.put(block, tradableBlock);
    }

    public static <T extends Block & TradableBlock> void addTradableBlock(T tradableBlock) {
        TRADABLE_BLOCKS.put(tradableBlock, tradableBlock);
    }

    public static void removeTradableBlock(Block block) {
        TRADABLE_BLOCKS.remove(block);
    }

    @NotNull
    public static TradableBlock getTradableBlock(BlockState state, Level level, BlockPos pos, Player player) {
        try {
            MenuProvider menuProvider = state.getMenuProvider(level, pos);
            if (menuProvider != null) {
                AbstractContainerMenu menu = menuProvider.createMenu(0, player.getInventory(), player);
                if (menu != null && menu.getType() == MenuType.MERCHANT) {
                    return TradableBlock.TRUE;
                }
            }
        } catch (UnsupportedOperationException ignored) {

        }
        Block block = state.getBlock();
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return ModConfig.getGeneric().tradableBlocks.stream().anyMatch(path::equals) ? TradableBlock.TRUE : TRADABLE_BLOCKS.getOrDefault(block, TradableBlock.FALSE);
    }
}
