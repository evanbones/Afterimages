package com.evandev.afterimages.client;

import com.evandev.afterimages.Constants;
import com.evandev.afterimages.mixin.access.RenderSetupAccessor;
import com.evandev.afterimages.mixin.access.RenderTypeAccessor;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
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
                type.toString().contains("glint") ||
                !hasNormal(type)) {
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

    private boolean hasNormal(RenderType type) {
        return type.format().getElements().stream()
                .anyMatch(e -> e.equals(VertexFormatElement.NORMAL));
    }

    private static class GhostRenderType {
        private static final Map<RenderType, RenderType> CACHE = new HashMap<>();

        public static RenderType get(RenderType original) {
            return CACHE.computeIfAbsent(original, GhostRenderType::createGhostType);
        }

        private static RenderType createGhostType(RenderType original) {
            if (!(original instanceof RenderTypeAccessor accessor)) {
                return original;
            }

            try {
                RenderSetup setup = accessor.afterimages$getSetup();
                if (setup == null) return original;

                Identifier texture = extractTexture(setup);

                if (texture != null) {
                    RenderType template = RenderTypes.entityTranslucent(texture);

                    RenderSetup newSetup = RenderSetup.builder(template.pipeline())
                            .withTexture("Sampler0", texture)
                            .useLightmap()
                            .useOverlay()
                            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                            .createRenderSetup();

                    return RenderTypeAccessor.afterimages$create(
                            "afterimage_ghost_" + texture.getPath(),
                            newSetup
                    );
                }

            } catch (Exception e) {
                Constants.LOG.error("Failed to create Ghost RenderType for {}", original, e);
            }

            return original;
        }

        private static Identifier extractTexture(RenderSetup setup) {
            try {
                Map<String, ?> textures = ((RenderSetupAccessor) (Object) setup).afterimages$getTextures();

                if (textures == null || textures.isEmpty()) return null;

                Object binding = textures.values().iterator().next();

                for (Field f : binding.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    if (f.getType().equals(Identifier.class)) {
                        return (Identifier) f.get(binding);
                    }
                }
            } catch (Exception e) {
                // Ignore reflection errors to avoid spam
            }
            return null;
        }

    }

    private record AlphaVertexConsumer(VertexConsumer delegate, float alphaMultiplier, int rgb,
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

            float alphaFactor = this.alphaMultiplier;

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
        public @NotNull VertexConsumer setColor(int packedColor) {
            int alpha = (packedColor >> 24) & 0xFF;
            int red = (packedColor >> 16) & 0xFF;
            int green = (packedColor >> 8) & 0xFF;
            int blue = packedColor & 0xFF;
            return setColor(red, green, blue, alpha);
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

        @Override
        public @NotNull VertexConsumer setLineWidth(float width) {
            delegate.setLineWidth(width);
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
        public @NotNull VertexConsumer setColor(int packedColor) {
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

        @Override
        public @NotNull VertexConsumer setLineWidth(float width) {
            return this;
        }
    }
}