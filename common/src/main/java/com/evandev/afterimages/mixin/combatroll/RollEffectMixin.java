package com.evandev.afterimages.mixin.combatroll;

import com.evandev.afterimages.compat.CombatRollCompat;
import net.combatroll.client.RollEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RollEffect.class)
public class RollEffectMixin {

    @Inject(method = "playVisuals", at = @At("HEAD"), remap = false)
    private static void afterimages$onPlayVisuals(RollEffect.Visuals visuals, Player player, Vec3 direction, CallbackInfo ci) {
        CombatRollCompat.onRoll(player);
    }
}