package com.evandev.afterimages;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
	public static final String MOD_ID = "afterimages";
	public static final String MOD_NAME = "Afterimages";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static ResourceLocation id(String path) {
		return parseId(MOD_ID + ":" + path);
	}

	public static ResourceLocation parseId(String id) {
		//? if >=1.21 {
		return ResourceLocation.parse(id);
		//?} else
		//return new ResourceLocation(id);
	}
}
