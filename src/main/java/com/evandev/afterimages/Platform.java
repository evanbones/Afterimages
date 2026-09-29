package com.evandev.afterimages;

//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
//?} else if forge {
/*import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
*///?} else {
/*import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
*///?}

import java.nio.file.Path;

public final class Platform {
    private Platform() {
    }

    public static boolean isModLoaded(String modId) {
        //? if fabric {
        return FabricLoader.getInstance().isModLoaded(modId);
        //?} else
        //return ModList.get().isLoaded(modId);
    }

    public static Path getConfigDirectory() {
        //? if fabric {
        return FabricLoader.getInstance().getConfigDir();
        //?} else
        //return FMLPaths.CONFIGDIR.get();
    }
}
