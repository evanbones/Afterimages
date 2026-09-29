package com.evandev.afterimages.client;

//? if fabric {
import com.evandev.afterimages.Constants;
import com.evandev.afterimages.data.AfterimageConfigLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public class AfterimagesFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new FabricAfterimageConfigLoader());
    }

    private static class FabricAfterimageConfigLoader extends AfterimageConfigLoader implements IdentifiableResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return Constants.id("entities");
        }
    }
}
//?}
