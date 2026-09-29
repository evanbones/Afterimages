package com.evandev.afterimages;

//? if forge {
/*import com.evandev.afterimages.config.ClothConfigIntegration;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class AfterimagesForge {

    public AfterimagesForge() {
        CommonClass.init();
        MinecraftForge.EVENT_BUS.register(this);

        if (Platform.isModLoaded("cloth_config")) {
            FMLJavaModLoadingContext.get().getModEventBus().register(new Object() {
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

}
*///?}
