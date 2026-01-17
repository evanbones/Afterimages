package com.evandev.afterimages.mixin.azurelibarmor;

import com.evandev.afterimages.client.TransparencyBufferSource;
import mod.azure.azurelibarmor.renderer.GeoArmorRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GeoArmorRenderer.class)
public class GeoArmorRendererMixin {

    @ModifyVariable(method = "renderToBuffer", at = @At("LOAD"), name = "bufferSource")
    private MultiBufferSource afterimages$forceTransparencyBuffer(MultiBufferSource original) {
        if (TransparencyBufferSource.CURRENT_INSTANCE != null) {
            return TransparencyBufferSource.CURRENT_INSTANCE;
        }
        return original;
    }
}