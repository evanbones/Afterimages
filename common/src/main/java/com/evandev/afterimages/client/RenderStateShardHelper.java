package com.evandev.afterimages.client;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RenderStateShardHelper {
    public static final RenderStateShard LIGHTMAP;
    public static final RenderStateShard OVERLAY;

    private static final MethodHandle SET_LIGHTMAP;
    private static final MethodHandle SET_OVERLAY;

    static {
        try {
            Field lightmapField = RenderStateShard.class.getDeclaredField("LIGHTMAP");
            lightmapField.setAccessible(true);
            LIGHTMAP = (RenderStateShard) lightmapField.get(null);

            Field overlayField = RenderStateShard.class.getDeclaredField("OVERLAY");
            overlayField.setAccessible(true);
            OVERLAY = (RenderStateShard) overlayField.get(null);

            Class<?> builderClass = RenderType.CompositeState.CompositeStateBuilder.class;

            Method setLightmap = null;
            Method setOverlay = null;

            for (Method m : builderClass.getDeclaredMethods()) {
                if (m.getName().equals("setLightmapState") && m.getParameterCount() == 1) {
                    setLightmap = m;
                }
                if (m.getName().equals("setOverlayState") && m.getParameterCount() == 1) {
                    setOverlay = m;
                }
            }

            if (setLightmap == null || setOverlay == null) throw new RuntimeException("Could not find Builder methods");

            setLightmap.setAccessible(true);
            setOverlay.setAccessible(true);

            MethodHandles.Lookup lookup = MethodHandles.lookup();
            SET_LIGHTMAP = lookup.unreflect(setLightmap);
            SET_OVERLAY = lookup.unreflect(setOverlay);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RenderStateShardHelper", e);
        }
    }

    public static void setLightmapState(RenderType.CompositeState.CompositeStateBuilder builder, RenderStateShard shard) {
        try {
            SET_LIGHTMAP.invoke(builder, shard);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void setOverlayState(RenderType.CompositeState.CompositeStateBuilder builder, RenderStateShard shard) {
        try {
            SET_OVERLAY.invoke(builder, shard);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}