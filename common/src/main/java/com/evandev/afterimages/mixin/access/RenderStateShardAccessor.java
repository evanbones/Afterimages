package com.evandev.afterimages.mixin.access;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {
    @Accessor("NO_TEXTURE")
    static RenderStateShard.EmptyTextureStateShard afterimages$getNoTexture() {
        throw new AssertionError();
    }
}