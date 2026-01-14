package com.evandev.afterimages.compat;

import net.combatroll.CombatRoll;
import net.combatroll.internals.RollingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatRollCompat {

    private static final Map<UUID, Long> ROLL_START_TIMES = new ConcurrentHashMap<>();

    public static void init() {
    }

    public static void onRoll(Entity entity) {
        if (entity != null) {
            ROLL_START_TIMES.put(entity.getUUID(), entity.level().getGameTime());
        }
    }

    public static boolean isRolling(Entity entity) {
        if (!(entity instanceof Player)) return false;

        if (entity instanceof RollingEntity rollingEntity) {
            if (rollingEntity.getRollManager() != null) {
                return rollingEntity.getRollManager().isRolling();
            }
        }

        if (ROLL_START_TIMES.containsKey(entity.getUUID())) {
            long startTime = ROLL_START_TIMES.get(entity.getUUID());
            long currentTime = entity.level().getGameTime();
            long elapsed = currentTime - startTime;

            int duration = (CombatRoll.config != null) ? CombatRoll.config.roll_duration : 20;

            if (elapsed >= 0 && elapsed < duration) {
                return true;
            } else {
                ROLL_START_TIMES.remove(entity.getUUID());
            }
        }
        return false;
    }
}