package com.evandev.afterimages;

import com.evandev.afterimages.compat.CombatRollCompat;
import com.evandev.afterimages.compat.ElenaiDodgeCompat;

public class CommonClass {

    public static void init() {
        if (Platform.isModLoaded(CombatRollCompat.MOD_ID)) {
            Constants.LOG.info("Combat Roll detected, initializing integration.");
            CombatRollCompat.init();
        }
        if (Platform.isModLoaded("elenaidodge2")) {
            Constants.LOG.info("Elenai Dodge 2 detected, initializing integration.");
            ElenaiDodgeCompat.init();
        }
    }
}