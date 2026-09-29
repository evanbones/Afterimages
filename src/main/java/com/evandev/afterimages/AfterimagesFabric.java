package com.evandev.afterimages;

//? if fabric {
import net.fabricmc.api.ModInitializer;

public class AfterimagesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CommonClass.init();
    }
}
//?}
