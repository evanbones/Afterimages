package com.evandev.afterimages.mixin;

import com.evandev.afterimages.access.AfterimageAccessor;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimedTnt.class)
public class PrimedTntMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void afterimages$tick(CallbackInfo ci) {
        ((AfterimageAccessor) this).afterimages$tickHistory();
    }
}