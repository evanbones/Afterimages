package com.evandev.afterimages.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClothConfigIntegration {

    public static Screen createScreen(Screen parent) {
        ModConfig config = ModConfig.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.afterimages.config"));

        builder.setSavingRunnable(ModConfig::save);

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.afterimages.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startDoubleField(Component.translatable("option.afterimages.step_size"), config.step_size)
                .setDefaultValue(0.5)
                .setMin(0.05)
                .setMax(2.0)
                .setTooltip(Component.translatable("tooltip.afterimages.step_size"))
                .setSaveConsumer(newValue -> config.step_size = newValue)
                .build());

        return builder.build();
    }
}