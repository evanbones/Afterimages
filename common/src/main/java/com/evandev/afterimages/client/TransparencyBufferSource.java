package com.evandev.afterimages.client;

import com.evandev.afterimages.mixin.access.CompositeStateAccessor;
import com.evandev.afterimages.mixin.access.RenderTypeAccessor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
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
                !type.format().getElements().contains(DefaultVertexFormat.ELEMENT_NORMAL)) {
            return new NoOpVertexConsumer();
        }

        if (this.overlayOnly) {
            if (type.toString().contains("eyes")) {
                return new AlphaVertexConsumer(delegate.getBuffer(type), alpha, rgb, true, true);
            } else {
                return new NoOpVertexConsumer();
            }
        }

        RenderType remappedType = GhostRenderType.get(type);

        return new AlphaVertexConsumer(delegate.getBuffer(remappedType), alpha, rgb, false, false);
    }

    private static class GhostRenderType extends RenderType {
        private static final Map<RenderType, RenderType> CACHE = new HashMap<>();

        private GhostRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        }

        public static RenderType get(RenderType original) {
            return CACHE.computeIfAbsent(original, GhostRenderType::createGhostType);
        }

        private static RenderType createGhostType(RenderType original) {
            RenderStateShard.EmptyTextureStateShard textureState = RenderStateShard.NO_TEXTURE;

            if (original instanceof RenderTypeAccessor accessor) {
                RenderType.CompositeState state = accessor.afterimages$getState();
                if (state != null) {
                    CompositeStateAccessor stateAccess = (CompositeStateAccessor) (Object) state;
                    textureState = stateAccess.afterimages$getTextureState();
                }
            }

            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(textureState)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .createCompositeState(false);

            return RenderType.create(
                    "afterimage_ghost_" + original.toString(),
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    true,
                    true,
                    state
            );
        }
    }

    private record AlphaVertexConsumer(VertexConsumer delegate, float alpha, int rgb, boolean premultiplyAlpha,
                                       boolean applyBias) implements VertexConsumer {
        @Override
        public @NotNull VertexConsumer vertex(double x, double y, double z) {
            if (applyBias) {
                double bias = 0.995;
                delegate.vertex(x * bias, y * bias, z * bias);
            } else {
                delegate.vertex(x, y, z);
            }
            return this;
        }

        @Override
        public @NotNull VertexConsumer color(int red, int green, int blue, int alpha) {
            float rScale = ((rgb >> 16) & 0xFF) / 255.0f;
            float gScale = ((rgb >> 8) & 0xFF) / 255.0f;
            float bScale = (rgb & 0xFF) / 255.0f;

            float alphaFactor = this.alpha;

            if (premultiplyAlpha) {
                rScale *= alphaFactor;
                gScale *= alphaFactor;
                bScale *= alphaFactor;
            }

            delegate.color(
                    (int) (red * rScale),
                    (int) (green * gScale),
                    (int) (blue * bScale),
                    (int) (alpha * alphaFactor)
            );
            return this;
        }

        @Override
        public @NotNull VertexConsumer uv(float u, float v) {
            delegate.uv(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            float rScale = ((rgb >> 16) & 0xFF) / 255.0f;
            float gScale = ((rgb >> 8) & 0xFF) / 255.0f;
            float bScale = (rgb & 0xFF) / 255.0f;

            float alphaFactor = this.alpha;
            if (premultiplyAlpha) {
                rScale *= alphaFactor;
                gScale *= alphaFactor;
                bScale *= alphaFactor;
            }

            delegate.defaultColor((int) (r * rScale), (int) (g * gScale), (int) (b * bScale), (int) (a * alphaFactor));
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }

    private static class NoOpVertexConsumer implements VertexConsumer {
        @Override
        public @NotNull VertexConsumer vertex(double x, double y, double z) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer color(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer uv(float u, float v) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer uv2(int u, int v) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer normal(float x, float y, float z) {
            return this;
        }

        @Override
        public void endVertex() {
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
        }

        @Override
        public void unsetDefaultColor() {
        }
    }
}