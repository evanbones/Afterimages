package com.evandev.afterimages.mixin.access;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.class)
public interface RenderTypeAccessor {
    @Invoker("create")
    static RenderType.CompositeRenderType afterimages$create(String name, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderPipeline renderPipeline, RenderType.CompositeState state) {
        throw new AssertionError();
    }
}