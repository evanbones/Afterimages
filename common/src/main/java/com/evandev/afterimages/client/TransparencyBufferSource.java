package com.evandev.afterimages.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class TransparencyBufferSource implements MultiBufferSource {
    private final MultiBufferSource delegate;
    private final ResourceLocation texture;
    private float alpha = 1.0f;
    private int rgb = 0xFFFFFF;
    private boolean overlayOnly = false;

    public TransparencyBufferSource(MultiBufferSource delegate, ResourceLocation texture) {
        this.delegate = delegate;
        this.texture = texture;
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
        if (this.overlayOnly) {
            if (type.toString().contains("eyes")) {
                return new AlphaVertexConsumer(delegate.getBuffer(type), alpha, rgb, true);
            } else {
                return new NoOpVertexConsumer();
            }
        }

        RenderType remappedType = GhostRenderType.get(this.texture);
        return new AlphaVertexConsumer(delegate.getBuffer(remappedType), alpha, rgb, false);
    }

    private static class GhostRenderType extends RenderType {
        private static final Map<ResourceLocation, RenderType> CACHE = new HashMap<>();

        private GhostRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        }

        public static RenderType get(ResourceLocation texture) {
            return CACHE.computeIfAbsent(texture, GhostRenderType::createGhostType);
        }

        private static RenderType createGhostType(ResourceLocation texture) {
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false);

            return RenderType.create(
                    "afterimage_ghost",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    true,
                    true,
                    state
            );
        }
    }

    private record AlphaVertexConsumer(VertexConsumer delegate, float alpha, int rgb, boolean premultiplyAlpha) implements VertexConsumer {
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
        @Override public @NotNull VertexConsumer addVertex(float x, float y, float z) { return this; }
        @Override public @NotNull VertexConsumer setColor(int red, int green, int blue, int alpha) { return this; }
        @Override public @NotNull VertexConsumer setUv(float u, float v) { return this; }
        @Override public @NotNull VertexConsumer setUv1(int u, int v) { return this; }
        @Override public @NotNull VertexConsumer setUv2(int u, int v) { return this; }
        @Override public @NotNull VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}