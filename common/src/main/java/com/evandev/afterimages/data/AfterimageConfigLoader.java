package com.evandev.afterimages.data;

import com.evandev.afterimages.Constants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
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

public class AfterimageConfigLoader extends SimpleJsonResourceReloadListener<AfterimageConfigLoader.AfterimageConfig> {

    public static final Map<EntityType<?>, AfterimageConfig> CONFIGS = new HashMap<>();

    public AfterimageConfigLoader() {
        super(AfterimageConfig.CODEC, FileToIdConverter.json("afterimages/entities"));
    }

    @Override
    protected void apply(Map<ResourceLocation, AfterimageConfig> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        CONFIGS.clear();

        resources.forEach((location, config) -> {
            try {
                List<EntityType<?>> entities = new ArrayList<>();

                String entityStr = config.entityId();
                if (entityStr != null && !entityStr.isEmpty()) {
                    if (entityStr.startsWith("#")) {
                        ResourceLocation tagLoc = ResourceLocation.parse(entityStr.substring(1));
                        TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, tagLoc);

                        BuiltInRegistries.ENTITY_TYPE.get(tagKey).ifPresent(tag -> {
                            for (Holder<EntityType<?>> holder : tag) {
                                entities.add(holder.value());
                            }
                        });
                    } else {
                        BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityStr))
                                .ifPresent(holder -> entities.add(holder.value()));
                    }
                } else {
                    ResourceLocation entityId = ResourceLocation.fromNamespaceAndPath(location.getNamespace(), location.getPath());
                    BuiltInRegistries.ENTITY_TYPE.get(entityId)
                            .ifPresent(holder -> entities.add(holder.value()));
                }

                if (!entities.isEmpty()) {
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

    public record AfterimageConfig(String entityId, double speedThreshold, int duration, int color, boolean overlayOnly,
                                   double startAlpha, boolean combatRollOnly) {

        public static final Codec<AfterimageConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("entity", "").forGetter(AfterimageConfig::entityId),
                Codec.DOUBLE.optionalFieldOf("speed_threshold", 0.1).forGetter(AfterimageConfig::speedThreshold),
                Codec.INT.optionalFieldOf("duration", 15).forGetter(AfterimageConfig::duration),
                Codec.STRING.optionalFieldOf("color", "0xFFFFFF").xmap(
                        Integer::decode,
                        val -> "0x" + Integer.toHexString(val)
                ).forGetter(c -> c.color),
                Codec.BOOL.optionalFieldOf("overlay_only", false).forGetter(AfterimageConfig::overlayOnly),
                Codec.DOUBLE.optionalFieldOf("start_alpha", 0.5).forGetter(AfterimageConfig::startAlpha),
                Codec.BOOL.optionalFieldOf("combat_roll_only", false).forGetter(AfterimageConfig::combatRollOnly)
        ).apply(instance, AfterimageConfig::new));
    }
}