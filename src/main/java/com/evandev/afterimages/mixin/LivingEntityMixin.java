package com.evandev.afterimages.mixin;

import com.evandev.afterimages.effect.AfterimageEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "onEffectAdded", at = @At("TAIL"))
    private void afterimages$syncEffectAdded(MobEffectInstance instance, @Nullable Entity source, CallbackInfo ci) {
        AfterimageEffects.broadcastUpdate((LivingEntity) (Object) this, instance);
    }

    @Inject(method = "onEffectUpdated", at = @At("TAIL"))
    private void afterimages$syncEffectUpdated(MobEffectInstance instance, boolean forced, @Nullable Entity source, CallbackInfo ci) {
        AfterimageEffects.broadcastUpdate((LivingEntity) (Object) this, instance);
    }

    @Inject(method = "onEffectRemoved", at = @At("TAIL"))
    private void afterimages$syncEffectRemoved(MobEffectInstance instance, CallbackInfo ci) {
        AfterimageEffects.broadcastRemoval((LivingEntity) (Object) this, instance);
    }
}
