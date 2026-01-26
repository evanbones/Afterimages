package com.evandev.afterimages.mixin.access;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.class)
public interface RenderTypeAccessor {
    @Invoker("create")
    static RenderType afterimages$create(String name, RenderSetup setup) {
        throw new AssertionError();
    }

    @Accessor("state")
    RenderSetup afterimages$getSetup();
}