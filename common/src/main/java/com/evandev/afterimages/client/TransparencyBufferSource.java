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

    public TransparencyBufferSource(MultiBufferSource delegate, ResourceLocation texture) {
        this.delegate = delegate;
        this.texture = texture;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType type) {
        RenderType remappedType = remapToTranslucent(type);
        return new AlphaVertexConsumer(delegate.getBuffer(remappedType), alpha);
    }

    private RenderType remapToTranslucent(RenderType type) {
        String description = type.toString();
        if (description.contains("glint")) {
            return type;
        }
        return GhostRenderType.get(this.texture);
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
                    "afterimage",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    true,
                    true,
                    state
            );
        }
    }

    private record AlphaVertexConsumer(VertexConsumer delegate, float alpha) implements VertexConsumer {
        @Override
        public @NotNull VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }
        @Override
        public @NotNull VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color(red, green, blue, (int) (alpha * this.alpha));
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
            delegate.defaultColor(r, g, b, (int) (a * this.alpha));
        }
        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }
}