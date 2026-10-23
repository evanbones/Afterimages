package com.evandev.afterimages;

//? if fabric {
import com.evandev.afterimages.effect.AfterimageEffects;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
//? if >=1.21 {
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
//?} else {
/*import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
*///?}

public class AfterimagesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CommonClass.init();
        registerEffects();
    }

    private static void registerEffects() {
        //? if >=1.21 {
        AfterimageEffects.AFTERIMAGE = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT,
                Constants.id(AfterimageEffects.EFFECT_ID), AfterimageEffects.createEffect());
        AfterimageEffects.AFTERIMAGE_POTION = Registry.registerForHolder(BuiltInRegistries.POTION,
                Constants.id(AfterimageEffects.POTION_ID), AfterimageEffects.createPotion(AfterimageEffects.DURATION));
        AfterimageEffects.LONG_AFTERIMAGE_POTION = Registry.registerForHolder(BuiltInRegistries.POTION,
                Constants.id(AfterimageEffects.LONG_POTION_ID), AfterimageEffects.createPotion(AfterimageEffects.LONG_DURATION));

        FabricBrewingRecipeRegistryBuilder.BUILD.register(AfterimageEffects::addMixes);
        //?} else {
        /*MobEffect effect = Registry.register(BuiltInRegistries.MOB_EFFECT,
                Constants.id(AfterimageEffects.EFFECT_ID), AfterimageEffects.createEffect());
        AfterimageEffects.AFTERIMAGE = () -> effect;
        Potion potion = Registry.register(BuiltInRegistries.POTION,
                Constants.id(AfterimageEffects.POTION_ID), AfterimageEffects.createPotion(AfterimageEffects.DURATION));
        AfterimageEffects.AFTERIMAGE_POTION = () -> potion;
        Potion longPotion = Registry.register(BuiltInRegistries.POTION,
                Constants.id(AfterimageEffects.LONG_POTION_ID), AfterimageEffects.createPotion(AfterimageEffects.LONG_DURATION));
        AfterimageEffects.LONG_AFTERIMAGE_POTION = () -> longPotion;

        AfterimageEffects.addMixes();
        *///?}
    }
}
//?}
