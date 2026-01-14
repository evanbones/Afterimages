package com.evandev.afterimages.mixin;

import com.evandev.afterimages.access.AfterimageAccessor;
import com.evandev.afterimages.client.TransparencyBufferSource;
import com.evandev.afterimages.config.ModConfig;
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
import net.minecraft.world.phys.Vec3;
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
            transparencyBuffer.setOverlayOnly(config.overlayOnly());

            List<AfterimageAccessor.Snapshot> snapshots = new ArrayList<>();
            double renderTime = entity.level().getGameTime() + partialTicks;

            boolean connectedToBody = false;
            double timeGap = 0;
            if (history.peekFirst() != null) {
                timeGap = renderTime - history.peekFirst().timestamp();
            }

            if (timeGap < 2.0) {
                connectedToBody = true;
                double curX = Mth.lerp(partialTicks, entity.xo, entity.getX());
                double curY = Mth.lerp(partialTicks, entity.yo, entity.getY());
                double curZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());
                float curYRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
                float curXRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
                float curYBody = 0;
                float curYHead = 0;

                if (entity instanceof LivingEntity l) {
                    curYBody = Mth.lerp(partialTicks, l.yBodyRotO, l.yBodyRot);
                    curYHead = Mth.lerp(partialTicks, l.yHeadRotO, l.yHeadRot);
                }

                snapshots.add(new AfterimageAccessor.Snapshot(
                        new Vec3(curX, curY, curZ),
                        curYBody, curYHead, curYRot, curXRot, 1.0f, (long) renderTime
                ));
            }

            snapshots.addAll(history);

            double stepSize = Math.max(0.05, ModConfig.get().step_size);
            double maxAge = config.duration();

            for (double age = stepSize; age < maxAge; age += stepSize) {
                double targetTime = renderTime - age;

                AfterimageAccessor.Snapshot before = null;
                AfterimageAccessor.Snapshot after = null;

                for (int i = 0; i < snapshots.size() - 1; i++) {
                    AfterimageAccessor.Snapshot s1 = snapshots.get(i);
                    AfterimageAccessor.Snapshot s2 = snapshots.get(i+1);

                    double t1 = (i == 0 && connectedToBody) ? renderTime : s1.timestamp();
                    double t2 = s2.timestamp();

                    if (targetTime <= t1 && targetTime >= t2) {
                        before = s1;
                        after = s2;
                        break;
                    }
                }

                if (before == null) continue;

                double t1 = (snapshots.indexOf(before) == 0 && connectedToBody) ? renderTime : before.timestamp();
                double t2 = after.timestamp();
                double delta = t1 - t2;

                if (delta <= 0.0001) continue;

                float progress = (float) ((t1 - targetTime) / delta);

                float ageProgress = (float) (age / maxAge);
                float alpha = (float) (config.startAlpha() * (1.0f - ageProgress));

                alpha *= (float) (stepSize / 0.25);

                if (alpha <= 0.01f) continue;

                transparencyBuffer.setAlpha(alpha);

                double interpX = Mth.lerp(progress, before.position().x, after.position().x);
                double interpY = Mth.lerp(progress, before.position().y, after.position().y);
                double interpZ = Mth.lerp(progress, before.position().z, after.position().z);

                float interpYRot = Mth.rotLerp(progress, before.yRot(), after.yRot());
                float interpXRot = Mth.rotLerp(progress, before.xRot(), after.xRot());
                float interpYBody = Mth.rotLerp(progress, before.yBodyRot(), after.yBodyRot());
                float interpYHead = Mth.rotLerp(progress, before.yHeadRot(), after.yHeadRot());

                poseStack.pushPose();

                double curX = Mth.lerp(partialTicks, entity.xo, entity.getX());
                double curY = Mth.lerp(partialTicks, entity.yo, entity.getY());
                double curZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());

                double offsetX = interpX - curX;
                double offsetY = interpY - curY;
                double offsetZ = interpZ - curZ;

                poseStack.translate(x + offsetX, y + offsetY, z + offsetZ);

                try {
                    float oldYRot = entity.getYRot();
                    float oldXRot = entity.getXRot();
                    float oldYBody = 0;
                    float oldYHead = 0;

                    if (entity instanceof LivingEntity l) {
                        oldYBody = l.yBodyRot;
                        oldYHead = l.yHeadRot;
                        l.yBodyRot = interpYBody;
                        l.yHeadRot = interpYHead;
                    }
                    entity.setYRot(interpYRot);
                    entity.setXRot(interpXRot);

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