package com.evandev.afterimages.mixin.access;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.LightmapStateShard;
import net.minecraft.client.renderer.RenderStateShard.OverlayStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {
    @Accessor("NO_TEXTURE")
    static RenderStateShard.EmptyTextureStateShard afterimages$getNoTexture() {
        throw new AssertionError();
    }

    @Accessor("LIGHTMAP")
    static LightmapStateShard afterimages$getLightmap() {
        throw new AssertionError();
    }

    @Accessor("OVERLAY")
    static OverlayStateShard afterimages$getOverlay() {
        throw new AssertionError();
    }
}