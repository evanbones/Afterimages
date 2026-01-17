package com.evandev.afterimages.data;

import com.evandev.afterimages.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AfterimageConfigLoader extends SimpleJsonResourceReloadListener {
    public static final Map<EntityType<?>, AfterimageConfig> CONFIGS = new HashMap<>();
    private static final Gson GSON = new Gson();

    public AfterimageConfigLoader() {
        super(GSON, "afterimages/entities");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        CONFIGS.clear();

        resources.forEach((location, json) -> {
            try {
                JsonObject obj = json.getAsJsonObject();
                List<EntityType<?>> entities = new ArrayList<>();

                if (obj.has("entity")) {
                    String entityStr = obj.get("entity").getAsString();
                    if (entityStr.startsWith("#")) {
                        ResourceLocation tagLoc = ResourceLocation.parse(entityStr.substring(1));
                        TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, tagLoc);
                        BuiltInRegistries.ENTITY_TYPE.getTag(tagKey).ifPresent(tag -> {
                            for (Holder<EntityType<?>> holder : tag) entities.add(holder.value());
                        });
                    } else {
                        entities.add(BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityStr)));
                    }
                } else {
                    ResourceLocation entityId = ResourceLocation.fromNamespaceAndPath(location.getNamespace(), location.getPath());
                    entities.add(BuiltInRegistries.ENTITY_TYPE.get(entityId));
                }

                if (!entities.isEmpty()) {
                    double speedThreshold = obj.has("speed_threshold") ? obj.get("speed_threshold").getAsDouble() : 0.1;
                    int duration = obj.has("duration") ? obj.get("duration").getAsInt() : 15;
                    int color = obj.has("color") ? Integer.decode(obj.get("color").getAsString()) : 0xFFFFFF;
                    boolean overlay = obj.has("overlay_only") && obj.get("overlay_only").getAsBoolean();
                    boolean combatRollOnly = obj.has("combat_roll_only") && obj.get("combat_roll_only").getAsBoolean();
                    boolean elenaiDodgeOnly = obj.has("elenai_dodge_only") && obj.get("elenai_dodge_only").getAsBoolean();

                    double startAlpha = obj.has("start_alpha") ? obj.get("start_alpha").getAsDouble() : 0.5;

                    AfterimageConfig config = new AfterimageConfig(speedThreshold, duration, color, overlay, startAlpha, combatRollOnly, elenaiDodgeOnly);

                    for (EntityType<?> type : entities) {
                        CONFIGS.put(type, config);
                    }
                }

            } catch (Exception e) {
                Constants.LOG.error("Failed to load afterimage config for {}", location, e);
            }
        });

        Constants.LOG.info("Loaded {} afterimage configurations.", CONFIGS.size());
    }

    public record AfterimageConfig(double speedThreshold, int duration, int color, boolean overlayOnly,
                                   double startAlpha, boolean combatRollOnly, boolean elenaiDodgeOnly) {
    }
}