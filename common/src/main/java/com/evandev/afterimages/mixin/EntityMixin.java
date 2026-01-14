package com.evandev.afterimages.mixin;

import com.evandev.afterimages.access.AfterimageAccessor;
import com.evandev.afterimages.data.AfterimageConfigLoader;
import com.evandev.afterimages.compat.CombatRollCompat;
import com.evandev.afterimages.platform.Services;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(Entity.class)
public class EntityMixin implements AfterimageAccessor {
    @Unique
    private final Deque<Snapshot> afterimages$afterimageHistory = new ArrayDeque<>();

    @Override
    public Deque<Snapshot> afterimages$getHistory() {
        return afterimages$afterimageHistory;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickAfterimages(CallbackInfo ci) {
        afterimages$tickHistory();
    }

    @Override
    public void afterimages$tickHistory() {
        Entity self = (Entity) (Object) this;
        long gameTime = self.level().getGameTime();

        var config = AfterimageConfigLoader.CONFIGS.get(self.getType());

        if (config == null) {
            if (!afterimages$afterimageHistory.isEmpty()) afterimages$afterimageHistory.clear();
            return;
        }

        while (!afterimages$afterimageHistory.isEmpty()) {
            Snapshot oldest = afterimages$afterimageHistory.peekLast();
            if (gameTime - oldest.timestamp() > config.duration()) {
                afterimages$afterimageHistory.removeLast();
            } else {
                break;
            }
        }

        boolean shouldRecord = false;

        if (config.combatRollOnly()) {
            if (Services.PLATFORM.isModLoaded("combatroll")) {
                if (CombatRollCompat.isRolling(self)) {
                    shouldRecord = true;
                }
            }
        } else {
            double speed = self.getDeltaMovement().lengthSqr();
            if (speed >= config.speedThreshold() * config.speedThreshold()) {
                shouldRecord = true;
            }
        }

        if (!shouldRecord) return;

        float bodyRot = 0;
        float headRot = self.getYRot();
        if (self instanceof LivingEntity living) {
            bodyRot = living.yBodyRot;
            headRot = living.yHeadRot;
        }

        afterimages$afterimageHistory.addFirst(new Snapshot(
                new Vec3(self.getX(), self.getY(), self.getZ()),
                bodyRot,
                headRot,
                self.getYRot(),
                self.getXRot(),
                1.0f,
                gameTime
        ));
    }
}