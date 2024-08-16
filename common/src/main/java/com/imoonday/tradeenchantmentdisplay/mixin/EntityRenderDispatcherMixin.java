package com.imoonday.tradeenchantmentdisplay.mixin;

import com.imoonday.tradeenchantmentdisplay.config.ModConfig;
import com.imoonday.tradeenchantmentdisplay.renderer.EnchantmentRenderer;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferCache;
import com.imoonday.tradeenchantmentdisplay.util.MerchantOfferInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow
    @Final
    private ItemRenderer itemRenderer;

    @Shadow
    @Final
    private Font font;

    @Shadow
    public abstract double distanceToSqr(Entity entity);

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", shift = At.Shift.AFTER))
    private <E extends Entity> void render(E entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        MerchantOfferCache cache = MerchantOfferCache.getInstance();
        if (cache.contains(entity.getUUID())) {
            MerchantOfferInfo info = cache.get(entity.getUUID());
            double renderDistance = ModConfig.getMerchant().renderDistance;
            if (info != null && distanceToSqr(entity) <= renderDistance * renderDistance) {
                EnchantmentRenderer.renderUnderNameTag(entity, info, poseStack, buffer, itemRenderer, (EntityRenderDispatcher) (Object) this, font, packedLight);
            }
        }
    }
}
