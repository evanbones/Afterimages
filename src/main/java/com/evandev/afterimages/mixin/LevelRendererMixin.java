package com.evandev.afterimages.mixin;

import com.evandev.afterimages.client.AfterimageRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
//? if >=1.21
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderDebug(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/Camera;)V"
            )
    )
    //? if >=1.21 {
    private void renderAfterimages(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        PoseStack poseStack = new PoseStack();
    //?} else {
    /*private void renderAfterimages(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
    *///?}
        var mc = Minecraft.getInstance();
        AfterimageRenderer.renderAfterimages(poseStack, partialTick, mc.renderBuffers().bufferSource());
    }
}
