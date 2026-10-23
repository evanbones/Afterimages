package com.evandev.afterimages;

//? if forge {
/*import com.evandev.afterimages.config.ClothConfigIntegration;
import com.evandev.afterimages.effect.AfterimageEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(Constants.MOD_ID)
public class AfterimagesForge {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Constants.MOD_ID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, Constants.MOD_ID);

    public AfterimagesForge() {
        CommonClass.init();
        MinecraftForge.EVENT_BUS.register(this);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        registerEffects(modEventBus);

        if (Platform.isModLoaded("cloth_config")) {
            modEventBus.register(new Object() {
                @SubscribeEvent
                public void onConstructMod(FMLConstructModEvent event) {
                    ModLoadingContext.get().registerExtensionPoint(
                            ConfigScreenHandler.ConfigScreenFactory.class,
                            () -> new ConfigScreenHandler.ConfigScreenFactory(
                                    (client, parent) -> ClothConfigIntegration.createScreen(parent)
                            )
                    );
                }
            });
        }
    }

    private static void registerEffects(IEventBus modEventBus) {
        AfterimageEffects.AFTERIMAGE = EFFECTS.register(AfterimageEffects.EFFECT_ID, AfterimageEffects::createEffect);
        AfterimageEffects.AFTERIMAGE_POTION = POTIONS.register(AfterimageEffects.POTION_ID,
                () -> AfterimageEffects.createPotion(AfterimageEffects.DURATION));
        AfterimageEffects.LONG_AFTERIMAGE_POTION = POTIONS.register(AfterimageEffects.LONG_POTION_ID,
                () -> AfterimageEffects.createPotion(AfterimageEffects.LONG_DURATION));

        EFFECTS.register(modEventBus);
        POTIONS.register(modEventBus);
        modEventBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(AfterimageEffects::addMixes));
    }

}
*///?}
