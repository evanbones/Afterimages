package com.evandev.afterimages;

//? if neoforge {
/*import com.evandev.afterimages.client.NeoForgeClientEvents;
import com.evandev.afterimages.effect.AfterimageEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Constants.MOD_ID)
public class AfterimagesNeoForge {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Constants.MOD_ID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, Constants.MOD_ID);

    public AfterimagesNeoForge(IEventBus eventBus) {
        CommonClass.init();
        registerEffects(eventBus);

        if (FMLEnvironment.dist == Dist.CLIENT && Platform.isModLoaded("cloth_config")) {
            NeoForgeClientEvents.registerConfigScreen();
        }
    }

    private static void registerEffects(IEventBus eventBus) {
        AfterimageEffects.AFTERIMAGE = EFFECTS.register(AfterimageEffects.EFFECT_ID, AfterimageEffects::createEffect);
        AfterimageEffects.AFTERIMAGE_POTION = POTIONS.register(AfterimageEffects.POTION_ID,
                () -> AfterimageEffects.createPotion(AfterimageEffects.DURATION));
        AfterimageEffects.LONG_AFTERIMAGE_POTION = POTIONS.register(AfterimageEffects.LONG_POTION_ID,
                () -> AfterimageEffects.createPotion(AfterimageEffects.LONG_DURATION));

        EFFECTS.register(eventBus);
        POTIONS.register(eventBus);
        NeoForge.EVENT_BUS.addListener((RegisterBrewingRecipesEvent event) -> AfterimageEffects.addMixes(event.getBuilder()));
    }

}
*///?}
