package com.evandev.afterimages.mixin.access;

//? if <1.21 {
/*import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PotionBrewing.class)
public interface PotionBrewingInvoker {
    @Invoker("addMix")
    static void afterimages$addMix(Potion input, Item ingredient, Potion result) {
        throw new AssertionError();
    }
}
*///?}
