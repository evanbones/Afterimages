package com.evandev.afterimages.client;

import com.evandev.afterimages.mixin.access.RenderStateShardAccessor;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class RenderStateShardHelper {
    public static final RenderStateShard LIGHTMAP = RenderStateShardAccessor.afterimages$getLightmap();
    public static final RenderStateShard OVERLAY = RenderStateShardAccessor.afterimages$getOverlay();

    public static void setLightmapState(RenderType.CompositeState.CompositeStateBuilder builder, RenderStateShard shard) {
        builder.setLightmapState((RenderStateShard.LightmapStateShard) shard);
    }

    public static void setOverlayState(RenderType.CompositeState.CompositeStateBuilder builder, RenderStateShard shard) {
        builder.setOverlayState((RenderStateShard.OverlayStateShard) shard);
    }
}