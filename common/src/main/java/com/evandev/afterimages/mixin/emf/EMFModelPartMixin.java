package com.evandev.afterimages.mixin.emf;

import com.evandev.afterimages.client.TransparencyBufferSource;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import traben.entity_model_features.models.parts.EMFModelPart;

@Mixin(EMFModelPart.class)
public class EMFModelPartMixin {

    @ModifyVariable(method = "renderWithTextureOverride", at = @At("STORE"), remap = false, name = "provider")
    private MultiBufferSource afterimages$useTransparencySource(MultiBufferSource original) {
        if (TransparencyBufferSource.CURRENT_INSTANCE != null) {
            return TransparencyBufferSource.CURRENT_INSTANCE;
        }
        return original;
    }
}