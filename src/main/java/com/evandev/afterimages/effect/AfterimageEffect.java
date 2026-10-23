package com.evandev.afterimages.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class AfterimageEffect extends MobEffect {
    public AfterimageEffect() {
        super(MobEffectCategory.NEUTRAL, AfterimageEffects.COLOR);
    }
}
