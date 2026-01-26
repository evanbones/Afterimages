package com.evandev.afterimages.client;

import com.evandev.afterimages.access.AfterimageAccessor;
import com.evandev.afterimages.config.ModConfig;
import com.evandev.afterimages.data.AfterimageConfigLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AfterimageRenderer {
    private static final List<Entity> RENDER_QUEUE = new ArrayList<>();
    private static boolean isRendering = false;

    public static void addToQueue(Entity entity) {
        if (!isRendering) {
            RENDER_QUEUE.add(entity);
        }
    }

    public static void renderAfterimages(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LevelRenderState levelRenderState) {
        isRendering = true;
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

        float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);

        for (Entity entity : RENDER_QUEUE) {
            renderSingleEntity(entity, poseStack, partialTicks, bufferSource, dispatcher, levelRenderState);
        }

        RENDER_QUEUE.clear();
        isRendering = false;
    }

    private static void renderSingleEntity(Entity entity, PoseStack poseStack, float partialTicks, MultiBufferSource buffer, EntityRenderDispatcher dispatcher, LevelRenderState levelRenderState) {
        if (entity == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            return;
        }

        if (!(entity instanceof AfterimageAccessor accessor)) return;
        var history = accessor.afterimages$getHistory();
        if (history.isEmpty()) return;

        var config = AfterimageConfigLoader.CONFIGS.get(entity.getType());
        if (config == null) return;

        TransparencyBufferSource transparencyBuffer = new TransparencyBufferSource(buffer);
        TransparencyBufferSource.CURRENT_INSTANCE = transparencyBuffer;
        transparencyBuffer.setColor(config.color());
        transparencyBuffer.setOverlayOnly(config.overlayOnly());

        SubmitNodeCollector collector = new SubmitNodeCollector() {
            @Override
            public @NonNull OrderedSubmitNodeCollector order(int i) {
                return this;
            }

            @Override
            public void submitCustomGeometry(PoseStack poseStack, @NonNull RenderType renderType, CustomGeometryRenderer renderer) {
                renderer.render(poseStack.last(), transparencyBuffer.getBuffer(renderType));
            }

            @Override
            public <S> void submitModel(Model<? super S> model, S state, @NonNull PoseStack poseStack, @NonNull RenderType
                    renderType, int light, int overlay, int color, @Nullable TextureAtlasSprite sprite, int skip, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
                model.renderToBuffer(poseStack, transparencyBuffer.getBuffer(renderType), light, overlay, color);
            }

            @Override
            public void submitModelPart(ModelPart modelPart, @NonNull PoseStack poseStack, @NonNull RenderType
                    renderType, int light, int overlay, @Nullable TextureAtlasSprite sprite, boolean mirror, boolean flip, int color, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, int skip) {
                modelPart.render(poseStack, transparencyBuffer.getBuffer(renderType), light, overlay, color);
            }

            @Override
            public void submitItem(PoseStack poseStack, @NonNull ItemDisplayContext displayContext, int light, int overlay, int index, int @NonNull [] data, List<BakedQuad> quads, @NonNull RenderType renderType, ItemStackRenderState.@NonNull FoilType foilType) {
                var vertexConsumer = transparencyBuffer.getBuffer(renderType);
                PoseStack.Pose last = poseStack.last();
                for (BakedQuad quad : quads) {
                    vertexConsumer.putBulkData(
                            last,
                            quad,
                            (float) (config.color() >> 16 & 0xFF) / 255f,
                            (float) (config.color() >> 8 & 0xFF) / 255f,
                            (float) (config.color() & 0xFF) / 255f,
                            1.0f,
                            light,
                            overlay
                    );
                }
            }

            @Override
            public void submitParticleGroup(@NonNull ParticleGroupRenderer renderer) {
            }

            @Override
            public void submitShadow(@NonNull PoseStack poseStack, float radius, @NonNull List<EntityRenderState.ShadowPiece> shadows) {
            }

            @Override
            public void submitNameTag(@NonNull PoseStack poseStack, @Nullable Vec3 vec3, int i, @NonNull Component component, boolean b, int i1, double v, @NonNull CameraRenderState cameraRenderState) {
            }

            @Override
            public void submitText(@NonNull PoseStack poseStack, float v, float v1, @NonNull FormattedCharSequence formattedCharSequence, boolean b, Font.@NonNull DisplayMode displayMode, int i, int i1, int i2, int i3) {
            }

            @Override
            public void submitFlame(@NonNull PoseStack poseStack, @NonNull EntityRenderState entityRenderState, @NonNull Quaternionf quaternionf) {
            }

            @Override
            public void submitLeash(@NonNull PoseStack poseStack, EntityRenderState.@NonNull LeashState leashState) {
            }

            @Override
            public void submitBlock(@NonNull PoseStack poseStack, @NonNull BlockState blockState, int i, int i1, int i2) {
            }

            @Override
            public void submitMovingBlock(@NonNull PoseStack poseStack, @NonNull MovingBlockRenderState movingBlockRenderState) {
            }

            @Override
            public void submitBlockModel(@NonNull PoseStack poseStack, @NonNull RenderType renderType, @NonNull BlockStateModel blockStateModel, float v, float v1, float v2, int i, int i1, int i2) {
            }
        };

        double renderTime = entity.level().getGameTime() + partialTicks;

        try {
            List<AfterimageAccessor.Snapshot> snapshots = new ArrayList<>();

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
                alpha = Math.min(1.0f, Math.max(0.0f, alpha));

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

                float oldYRot = entity.getYRot();
                float oldXRot = entity.getXRot();
                Vec3 oldPos = entity.position();
                double oldXo = entity.xo;
                double oldYo = entity.yo;
                double oldZo = entity.zo;
                float oldYRotO = entity.yRotO;
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

                entity.setYRot(interpYRot);
                entity.setXRot(interpXRot);
                entity.setPos(interpX, interpY, interpZ);

                entity.xo = interpX;
                entity.yo = interpY;
                entity.zo = interpZ;
                entity.yRotO = interpYRot;
                entity.xRotO = interpXRot;

                try {
                    EntityRenderState renderState = dispatcher.extractEntity(entity, partialTicks);

                    Vec3 camPos = levelRenderState.cameraRenderState.pos;
                    double relX = interpX - camPos.x;
                    double relY = interpY - camPos.y;
                    double relZ = interpZ - camPos.z;

                    dispatcher.submit(
                            renderState,
                            levelRenderState.cameraRenderState,
                            relX,
                            relY,
                            relZ,
                            poseStack,
                            collector
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }

                entity.setPos(oldPos.x, oldPos.y, oldPos.z);
                entity.setYRot(oldYRot);
                entity.setXRot(oldXRot);
                entity.xo = oldXo;
                entity.yo = oldYo;
                entity.zo = oldZo;
                entity.yRotO = oldYRotO;
                entity.xRotO = oldXRotO;

                if (entity instanceof LivingEntity l) {
                    l.yBodyRot = oldYBody;
                    l.yHeadRot = oldYHead;
                    l.yBodyRotO = oldYBodyO;
                    l.yHeadRotO = oldYHeadO;
                }

                poseStack.popPose();
            }
        } finally {
            TransparencyBufferSource.CURRENT_INSTANCE = null;
        }
    }
}