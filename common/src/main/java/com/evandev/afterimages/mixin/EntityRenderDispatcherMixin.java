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
            if (history.size() < 2) return;

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

            for (int i = snapshots.size() - 2; i >= 0; i--) {
                AfterimageAccessor.Snapshot newer = snapshots.get(i);
                AfterimageAccessor.Snapshot older = snapshots.get(i + 1);

                double dist = newer.position().distanceTo(older.position());

                int steps = (int) Math.max(1, Math.ceil(dist / 0.05));

                for (int s = 0; s < steps; s++) {
                    float stepT = (float) s / steps;

                    double interpX = Mth.lerp(stepT, newer.position().x, older.position().x);
                    double interpY = Mth.lerp(stepT, newer.position().y, older.position().y);
                    double interpZ = Mth.lerp(stepT, newer.position().z, older.position().z);

                    double interpTime = Mth.lerp(stepT, (double)newer.timestamp(), (double)older.timestamp());

                    double age = renderTime - interpTime;
                    float ageProgress = (float) (age / config.duration());

                    if (ageProgress >= 1.0f) continue;

                    float alpha = 0.08f * (1.0f - ageProgress);

                    if (alpha <= 0.001f) continue;
                    transparencyBuffer.setAlpha(alpha);

                    poseStack.pushPose();

                    double offsetX = interpX - Mth.lerp(partialTicks, entity.xo, entity.getX());
                    double offsetY = interpY - Mth.lerp(partialTicks, entity.yo, entity.getY());
                    double offsetZ = interpZ - Mth.lerp(partialTicks, entity.zo, entity.getZ());

                    poseStack.translate(x + offsetX, y + offsetY, z + offsetZ);

                    try {
                        float oldYRot = entity.getYRot();
                        float oldXRot = entity.getXRot();
                        float oldYBody = 0;
                        float oldYHead = 0;

                        float interpYRot = Mth.rotLerp(stepT, newer.yRot(), older.yRot());
                        float interpXRot = Mth.rotLerp(stepT, newer.xRot(), older.xRot());
                        float interpYBody;
                        float interpYHead;

                        if (entity instanceof LivingEntity l) {
                            oldYBody = l.yBodyRot;
                            oldYHead = l.yHeadRot;
                            interpYBody = Mth.rotLerp(stepT, newer.yBodyRot(), older.yBodyRot());
                            interpYHead = Mth.rotLerp(stepT, newer.yHeadRot(), older.yHeadRot());

                            l.yBodyRot = interpYBody;
                            l.yHeadRot = interpYHead;
                        }

                        entity.setYRot(interpYRot);
                        entity.setXRot(interpXRot);

                        EntityRenderer<? super Entity> renderer = this.getRenderer(entity);
                        renderer.render(entity, 0, 0, poseStack, transparencyBuffer, packedLight);

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
            }

            RenderSystem.depthMask(true);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            afterimages$isRenderingAfterimage = false;
        }
    }
}