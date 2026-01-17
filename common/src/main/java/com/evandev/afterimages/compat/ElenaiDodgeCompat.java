package com.evandev.afterimages.compat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ElenaiDodgeCompat {
    private static final int DODGE_DURATION = 10;
    private static final Map<UUID, Long> DODGE_START_TIMES = new ConcurrentHashMap<>();

    public static void init() {
    }

    public static void onDodge(Entity entity) {
        if (entity != null) {
            DODGE_START_TIMES.put(entity.getUUID(), entity.level().getGameTime());
        }
    }

    public static boolean isDodging(Entity entity) {
        if (!(entity instanceof Player)) return false;

        if (DODGE_START_TIMES.containsKey(entity.getUUID())) {
            long startTime = DODGE_START_TIMES.get(entity.getUUID());
            long currentTime = entity.level().getGameTime();
            long elapsed = currentTime - startTime;

            if (elapsed >= 0 && elapsed < DODGE_DURATION) {
                return true;
            } else {
                DODGE_START_TIMES.remove(entity.getUUID());
            }
        }
        return false;
    }
}