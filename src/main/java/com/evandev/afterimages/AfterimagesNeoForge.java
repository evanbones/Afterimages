package com.evandev.afterimages;

//? if neoforge {
/*import com.evandev.afterimages.config.ClothConfigIntegration;
import com.evandev.afterimages.effect.AfterimageEffects;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

@Mod(Constants.MOD_ID)
public class AfterimagesNeoForge {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Constants.MOD_ID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, Constants.MOD_ID);

    public AfterimagesNeoForge(IEventBus eventBus) {
        CommonClass.init();
        registerEffects(eventBus);

        if (Platform.isModLoaded("cloth_config")) {
            eventBus.register(new Object() {
                @SubscribeEvent
                public void onConstructMod(FMLConstructModEvent event) {
                    ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> new IConfigScreenFactory() {
                        @Override
                        public @NotNull Screen createScreen(@NotNull ModContainer modContainer, @NotNull Screen parent) {
                            return ClothConfigIntegration.createScreen(parent);
                        }
                    });
                }
            });
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
