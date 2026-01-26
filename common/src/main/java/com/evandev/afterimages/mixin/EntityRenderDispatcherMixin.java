package com.evandev.afterimages.mixin;

import com.evandev.afterimages.access.AfterimageAccessor;
import com.evandev.afterimages.client.AfterimageRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("TAIL"))
    public <E extends Entity> void renderAfterimages(E entity, double xOffset, double yOffset, double zOffset, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        if (entity instanceof AfterimageAccessor accessor && !accessor.afterimages$getHistory().isEmpty()) {
            AfterimageRenderer.addToQueue(entity);
        }
    }
}