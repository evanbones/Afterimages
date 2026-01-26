package com.evandev.afterimages.neoforge.client;

import com.evandev.afterimages.Constants;
import com.evandev.afterimages.data.AfterimageConfigLoader;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientEvents {

    @SubscribeEvent
    public static void onRegisterReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "afterimage_config"),
                new AfterimageConfigLoader()
        );
    }
}