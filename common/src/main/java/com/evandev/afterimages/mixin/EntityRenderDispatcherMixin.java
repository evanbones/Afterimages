package com.evandev.afterimages.mixin;

import com.evandev.afterimages.access.AfterimageAccessor;
import com.evandev.afterimages.client.TransparencyBufferSource;
import com.evandev.afterimages.data.AfterimageConfigLoader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow public abstract <E extends Entity> EntityRenderer<? super E> getRenderer(E entity);

    @Unique
    private boolean afterimages$isRenderingAfterimage = false;

    @Inject(method = "render", at = @At("TAIL"))
    public <E extends Entity> void renderAfterimages(E entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (afterimages$isRenderingAfterimage) return;

        if (entity instanceof AfterimageAccessor accessor) {
            var history = accessor.afterimages$getHistory();
            if (history.isEmpty()) return;

            var config = AfterimageConfigLoader.CONFIGS.get(entity.getType());
            if (config == null) return;

            afterimages$isRenderingAfterimage = true;

            RenderSystem.depthMask(false);

            EntityRenderer<? super Entity> baseRenderer = this.getRenderer(entity);
            ResourceLocation entityTexture = baseRenderer.getTextureLocation(entity);

            TransparencyBufferSource transparencyBuffer = new TransparencyBufferSource(buffer, entityTexture);
            transparencyBuffer.setColor(config.color());

            List<AfterimageAccessor.Snapshot> snapshots = new ArrayList<>(history);
            double renderTime = entity.level().getGameTime() + partialTicks;

            for (AfterimageAccessor.Snapshot snapshot : snapshots) {
                double age = renderTime - snapshot.timestamp();

                float ageProgress = (float) (age / config.duration());
                if (ageProgress >= 1.0f) continue;

                float alpha = (float) (config.startAlpha() * (1.0f - ageProgress));

                if (alpha <= 0.05f) continue;

                transparencyBuffer.setAlpha(alpha);

                poseStack.pushPose();

                double entityX = Mth.lerp(partialTicks, entity.xo, entity.getX());
                double entityY = Mth.lerp(partialTicks, entity.yo, entity.getY());
                double entityZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());

                double offsetX = snapshot.position().x - entityX;
                double offsetY = snapshot.position().y - entityY;
                double offsetZ = snapshot.position().z - entityZ;

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
                    entity.setYRot(snapshot.yRot());
                    entity.setXRot(snapshot.xRot());

                    baseRenderer.render(entity, 0, 0, poseStack, transparencyBuffer, packedLight);

                    entity.setYRot(oldYRot);
                    entity.setXRot(oldXRot);
                    if (entity instanceof LivingEntity l) {
                        l.yBodyRot = oldYBody;
                        l.yHeadRot = oldYHead;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                poseStack.popPose();
            }

            RenderSystem.depthMask(true);
            afterimages$isRenderingAfterimage = false;
        }
    }
}