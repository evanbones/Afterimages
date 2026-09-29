package com.evandev.afterimages.mixin.emf;

import com.evandev.afterimages.client.TransparencyBufferSource;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import traben.entity_model_features.models.parts.EMFModelPart;
import traben.entity_texture_features.utils.URenderTypeToVertexConsumer;

@Mixin(value = EMFModelPart.class, remap = false)
public class EMFModelPartMixin {

    @WrapOperation(
            method = "renderTextureOverrideWithoutReset",
            at = @At(
                    value = "INVOKE",
                    target = "Ltraben/entity_texture_features/utils/URenderTypeToVertexConsumer;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            ),
            require = 0
    )
    private VertexConsumer afterimages$wrapTextureOverrideBuffer(
            URenderTypeToVertexConsumer provider,
            RenderType renderType,
            Operation<VertexConsumer> original
    ) {
        if (TransparencyBufferSource.CURRENT_INSTANCE != null) {
            return TransparencyBufferSource.CURRENT_INSTANCE.getBuffer(renderType);
        }
        return original.call(provider, renderType);
    }
}