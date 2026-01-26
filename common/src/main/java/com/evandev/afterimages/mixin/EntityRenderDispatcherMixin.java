package com.evandev.afterimages.mixin;

import com.evandev.afterimages.access.AfterimageAccessor;
import com.evandev.afterimages.client.AfterimageRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "extractEntity", at = @At("HEAD"))
    public <E extends Entity> void renderAfterimages(E entity, float partialTick, CallbackInfoReturnable<EntityRenderState> cir) {
        if (entity instanceof AfterimageAccessor accessor && !accessor.afterimages$getHistory().isEmpty()) {
            AfterimageRenderer.addToQueue(entity);
        }
    }
}