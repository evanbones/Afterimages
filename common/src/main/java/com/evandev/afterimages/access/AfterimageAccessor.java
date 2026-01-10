package com.evandev.afterimages.access;

import net.minecraft.world.phys.Vec3;
import java.util.Deque;

public interface AfterimageAccessor {
    Deque<Snapshot> afterimages$getHistory();
    void afterimages$tickHistory();

    record Snapshot(Vec3 position, float yBodyRot, float yHeadRot, float xRot, float scale) {}
}