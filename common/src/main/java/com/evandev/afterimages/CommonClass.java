package com.evandev.afterimages;

import com.evandev.afterimages.compat.CombatRollCompat;
import com.evandev.afterimages.platform.Services;

public class CommonClass {

    public static void init() {
        if (Services.PLATFORM.isModLoaded("combat_roll")) {
            Constants.LOG.info("Combat Roll detected, initializing integration.");
            CombatRollCompat.init();
        }
    }
}