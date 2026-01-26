package com.evandev.afterimages.client;

import com.evandev.afterimages.mixin.access.*;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TransparencyBufferSource implements MultiBufferSource {
    public static TransparencyBufferSource CURRENT_INSTANCE = null;
    private final MultiBufferSource delegate;
    private float alpha = 1.0f;
    private int rgb = 0xFFFFFF;
    private boolean overlayOnly = false;

    public TransparencyBufferSource(MultiBufferSource delegate) {
        this.delegate = delegate;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setColor(int rgb) {
        this.rgb = rgb;
    }

    public void setOverlayOnly(boolean overlayOnly) {
        this.overlayOnly = overlayOnly;
    }

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType type) {
        if (type.toString().contains("shadow") ||
                !type.format().getElements().contains(VertexFormatElement.NORMAL)) {
            return new NoOpVertexConsumer();
        }

        if (this.overlayOnly) {
            if (type.toString().contains("eyes")) {
                return new AlphaVertexConsumer(delegate.getBuffer(type), alpha, rgb, true);
            } else {
                return new NoOpVertexConsumer();
            }
        }

        RenderType remappedType = GhostRenderType.get(type);
        return new AlphaVertexConsumer(delegate.getBuffer(remappedType), alpha, rgb, false);
    }

    private static class GhostRenderType {
        private static final Map<RenderType, RenderType> CACHE = new HashMap<>();
        private static RenderPipeline TRANSLUCENT_PIPELINE;

        public static RenderType get(RenderType original) {
            return CACHE.computeIfAbsent(original, GhostRenderType::createGhostType);
        }

        private static RenderPipeline getTranslucentPipeline() {
            if (TRANSLUCENT_PIPELINE == null) {
                RenderType dummy = RenderType.entityTranslucent(ResourceLocation.parse("minecraft:dummy"));
                if (dummy instanceof CompositeRenderTypeAccessor accessor) {
                    TRANSLUCENT_PIPELINE = accessor.afterimages$getPipeline();
                } else {
                    throw new RuntimeException("Could not obtain Translucent RenderPipeline via Accessor");
                }
            }
            return TRANSLUCENT_PIPELINE;
        }

        private static RenderType createGhostType(RenderType original) {
            RenderStateShard.EmptyTextureStateShard textureState = RenderStateShardAccessor.afterimages$getNoTexture();

            if (original instanceof CompositeRenderTypeAccessor accessor) {
                RenderType.CompositeState state = accessor.afterimages$getState();
                if (state != null) {
                    CompositeStateAccessor stateAccess = (CompositeStateAccessor) (Object) state;
                    textureState = stateAccess.afterimages$getTextureState();
                }
            }

            var builder = RenderType.CompositeState.builder();
            CompositeStateBuilderAccessor builderAccess = (CompositeStateBuilderAccessor) builder;

            builderAccess.afterimages$setTextureState(textureState);

            RenderStateShardHelper.setLightmapState(builder, RenderStateShardHelper.LIGHTMAP);
            RenderStateShardHelper.setOverlayState(builder, RenderStateShardHelper.OVERLAY);

            RenderType.CompositeState state = builderAccess.afterimages$createCompositeState(false);

            return RenderTypeAccessor.afterimages$create(
                    "afterimage_ghost_" + original.toString(),
                    1536,
                    false,
                    true,
                    getTranslucentPipeline(),
                    state
            );
        }
    }

    private record AlphaVertexConsumer(VertexConsumer delegate, float alpha, int rgb,
                                       boolean premultiplyAlpha) implements VertexConsumer {
        @Override
        public @NotNull VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setColor(int red, int green, int blue, int alpha) {
            float rScale = ((rgb >> 16) & 0xFF) / 255.0f;
            float gScale = ((rgb >> 8) & 0xFF) / 255.0f;
            float bScale = (rgb & 0xFF) / 255.0f;

            float alphaFactor = this.alpha;

            if (premultiplyAlpha) {
                rScale *= alphaFactor;
                gScale *= alphaFactor;
                bScale *= alphaFactor;
            }

            delegate.setColor(
                    (int) (red * rScale),
                    (int) (green * gScale),
                    (int) (blue * bScale),
                    (int) (alpha * alphaFactor)
            );
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }
    }

    private static class NoOpVertexConsumer implements VertexConsumer {
        @Override
        public @NotNull VertexConsumer addVertex(float x, float y, float z) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }
    }
}