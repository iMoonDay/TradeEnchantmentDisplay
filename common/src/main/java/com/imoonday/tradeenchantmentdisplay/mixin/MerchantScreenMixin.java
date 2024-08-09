package com.imoonday.tradeenchantmentdisplay.mixin;

import com.imoonday.tradeenchantmentdisplay.EnchantmentNamesRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {

    @Unique
    private int drawTick;

    private MerchantScreenMixin(MerchantMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", shift = At.Shift.AFTER, ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci, MerchantOffers merchantOffers, int i, int j, int k, int l, int m, Iterator var11, MerchantOffer merchantOffer, ItemStack itemStack, ItemStack itemStack2, ItemStack itemStack3, ItemStack itemStack4, int n) {
        EnchantmentNamesRenderer.render(poseStack, font, itemStack4, i, n, drawTick);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        drawTick++;
    }
}
