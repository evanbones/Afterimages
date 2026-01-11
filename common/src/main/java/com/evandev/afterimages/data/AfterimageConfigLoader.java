package com.evandev.afterimages.data;

import com.evandev.afterimages.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AfterimageConfigLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    public static final Map<EntityType<?>, AfterimageConfig> CONFIGS = new HashMap<>();

    public AfterimageConfigLoader() {
        super(GSON, "afterimages/entities");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        CONFIGS.clear();

        resources.forEach((location, json) -> {
            try {
                JsonObject obj = json.getAsJsonObject();

                ResourceLocation entityId = new ResourceLocation(location.getNamespace(), location.getPath());

                if (obj.has("entity")) {
                    entityId = new ResourceLocation(obj.get("entity").getAsString());
                }

                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);

                double speedThreshold = obj.has("speed_threshold") ? obj.get("speed_threshold").getAsDouble() : 0.5;
                int duration = obj.has("duration") ? obj.get("duration").getAsInt() : 10;
                int color = obj.has("color") ? Integer.decode(obj.get("color").getAsString()) : 0xFFFFFF;
                boolean overlay = obj.has("overlay_only") && obj.get("overlay_only").getAsBoolean();

                CONFIGS.put(type, new AfterimageConfig(speedThreshold, duration, color, overlay));

            } catch (Exception e) {
                Constants.LOG.error("Failed to load afterimage config for {}", location, e);
            }
        });

        Constants.LOG.info("Loaded {} afterimage configurations.", CONFIGS.size());
    }

    public record AfterimageConfig(double speedThreshold, int duration, int color, boolean overlayOnly) {}
}