package com.evandev.afterimages.mixin.elenaidodge;

import com.elenai.elenaidodge2.client.animation.DodgeAnimator;
import com.elenai.elenaidodge2.util.DodgeHandler;
import com.evandev.afterimages.compat.ElenaiDodgeCompat;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DodgeHandler.class)
public class DodgeHandlerMixin {

    @Inject(method = "handleDodge", at = @At(value = "INVOKE", target = "Lcom/elenai/elenaidodge2/networking/ED2Messages;sendToServer(Ljava/lang/Object;)V"), remap = false)
    private static void afterimages$onHandleDodge(DodgeAnimator.DodgeDirection direction, CallbackInfo ci) {
        ElenaiDodgeCompat.onDodge(Minecraft.getInstance().player);
    }
}