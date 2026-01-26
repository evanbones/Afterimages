package com.evandev.afterimages.mixin.access;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.CompositeState.CompositeStateBuilder.class)
public interface CompositeStateBuilderAccessor {
    @Invoker("setTextureState")
    RenderType.CompositeState.CompositeStateBuilder afterimages$setTextureState(RenderStateShard.EmptyTextureStateShard state);

    @Invoker("createCompositeState")
    RenderType.CompositeState afterimages$createCompositeState(boolean outline);
}