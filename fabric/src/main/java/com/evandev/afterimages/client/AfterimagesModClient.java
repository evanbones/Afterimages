package com.evandev.afterimages.client;

import com.evandev.afterimages.Constants;
import com.evandev.afterimages.data.AfterimageConfigLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public class AfterimagesModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new FabricAfterimageConfigLoader());
    }

    private static class FabricAfterimageConfigLoader extends AfterimageConfigLoader implements IdentifiableResourceReloadListener {
        @Override
        public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "entities");
        }
    }
}