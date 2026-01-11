package com.evandev.afterimages.client;

import com.evandev.afterimages.access.AfterimageAccessor;
import com.evandev.afterimages.config.ModConfig;
import com.evandev.afterimages.data.AfterimageConfigLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class AfterimageRenderer {
    private static final List<Entry> ENTRIES = new ArrayList<>();
    public static boolean isRendering = false;

    public static void add(Entity entity, float partialTick) {
        ENTRIES.add(new Entry(entity, partialTick));
    }

    public static void renderAll(PoseStack poseStack, MultiBufferSource buffer, Camera camera) {
        if (ENTRIES.isEmpty()) return;

        isRendering = true;
        Vec3 cameraPos = camera.getPosition();

        for (Entry entry : ENTRIES) {
            renderEntityAfterimages(entry.entity, entry.partialTick, poseStack, buffer, cameraPos);
        }

        ENTRIES.clear();
        isRendering = false;
    }

    private static void renderEntityAfterimages(Entity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, Vec3 cameraPos) {
        if (!(entity instanceof AfterimageAccessor accessor)) return;

        var history = accessor.afterimages$getHistory();
        if (history.isEmpty()) return;

        var config = AfterimageConfigLoader.CONFIGS.get(entity.getType());
        if (config == null) return;

        EntityRenderer<? super Entity> baseRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        ResourceLocation entityTexture = baseRenderer.getTextureLocation(entity);

        TransparencyBufferSource transparencyBuffer = new TransparencyBufferSource(buffer, entityTexture);
        transparencyBuffer.setColor(config.color());

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

        Vec3 renderOffset = baseRenderer.getRenderOffset(entity, partialTicks);

        for (double age = maxAge; age >= stepSize; age -= stepSize) {
            double targetTime = renderTime - age;
            AfterimageAccessor.Snapshot before = null;
            AfterimageAccessor.Snapshot after = null;

            for (int i = 0; i < snapshots.size() - 1; i++) {
                AfterimageAccessor.Snapshot s1 = snapshots.get(i);
                AfterimageAccessor.Snapshot s2 = snapshots.get(i + 1);
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

            poseStack.translate(
                    interpX - cameraPos.x + renderOffset.x,
                    interpY - cameraPos.y + renderOffset.y,
                    interpZ - cameraPos.z + renderOffset.z
            );

            try {
                float oldXRot = entity.getXRot();
                float oldXRotO = entity.xRotO;

                float oldYBody = 0;
                float oldYHead = 0;
                float oldYBodyO = 0;
                float oldYHeadO = 0;

                if (entity instanceof LivingEntity l) {
                    oldYBody = l.yBodyRot;
                    oldYHead = l.yHeadRot;
                    oldYBodyO = l.yBodyRotO;
                    oldYHeadO = l.yHeadRotO;

                    l.yBodyRot = interpYBody;
                    l.yHeadRot = interpYHead;
                    l.yBodyRotO = interpYBody;
                    l.yHeadRotO = interpYHead;
                }

                entity.setXRot(interpXRot);
                entity.xRotO = interpXRot;

                baseRenderer.render(entity, interpYRot, 1.0f, poseStack, transparencyBuffer, 15728880);

                entity.setXRot(oldXRot);
                entity.xRotO = oldXRotO;

                if (entity instanceof LivingEntity l) {
                    l.yBodyRot = oldYBody;
                    l.yHeadRot = oldYHead;
                    l.yBodyRotO = oldYBodyO;
                    l.yHeadRotO = oldYHeadO;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            poseStack.popPose();
        }
    }

    private record Entry(Entity entity, float partialTick) {
    }
}