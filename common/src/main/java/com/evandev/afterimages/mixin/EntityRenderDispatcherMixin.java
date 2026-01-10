package com.evandev.afterimages.mixin;

import com.evandev.afterimages.access.AfterimageAccessor;
import com.evandev.afterimages.data.AfterimageConfigLoader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Unique
    private boolean isRenderingAfterimage = false;

    @Inject(method = "render", at = @At("HEAD"))
    public <E extends Entity> void renderAfterimages(E entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (isRenderingAfterimage) return;

        if (entity instanceof AfterimageAccessor accessor) {
            var history = accessor.afterimages$getHistory();
            if (history.isEmpty()) return;

            var config = AfterimageConfigLoader.CONFIGS.get(entity.getType());
            if (config == null) return;

            isRenderingAfterimage = true;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.4f);

            int i = 0;
            for (AfterimageAccessor.Snapshot snapshot : history) {
                if (i++ % 2 == 0) continue;

                poseStack.pushPose();

                double offsetX = snapshot.position().x - net.minecraft.util.Mth.lerp(partialTicks, entity.xo, entity.getX());
                double offsetY = snapshot.position().y - net.minecraft.util.Mth.lerp(partialTicks, entity.yo, entity.getY());
                double offsetZ = snapshot.position().z - net.minecraft.util.Mth.lerp(partialTicks, entity.zo, entity.getZ());

                poseStack.translate(x + offsetX, y + offsetY, z + offsetZ);

                try {
                    float oldYRot = entity.getYRot();
                    float oldXRot = entity.getXRot();
                    float oldYBody = 0;
                    float oldYHead = 0;
                    if (entity instanceof LivingEntity l) {
                        oldYBody = l.yBodyRot;
                        oldYHead = l.yHeadRot;
                        l.yBodyRot = snapshot.yBodyRot();
                        l.yHeadRot = snapshot.yHeadRot();
                    }
                    entity.setYRot(snapshot.yHeadRot());
                    entity.setXRot(snapshot.xRot());

                    ((EntityRenderDispatcher) (Object) this).render(entity, 0, 0, 0, 0, partialTicks, poseStack, buffer, packedLight);

                    entity.setYRot(oldYRot);
                    entity.setXRot(oldXRot);
                    if (entity instanceof LivingEntity l) {
                        l.yBodyRot = oldYBody;
                        l.yHeadRot = oldYHead;
                    }
                } catch (Exception e) {
                }

                poseStack.popPose();
            }

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
            isRenderingAfterimage = false;
        }
    }
}