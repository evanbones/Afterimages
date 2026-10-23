package com.evandev.afterimages.effect;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;

//? if <1.21 {
/*import com.evandev.afterimages.mixin.access.PotionBrewingInvoker;

import java.util.function.Supplier;
*///?}

public class AfterimageEffects {
    public static final String EFFECT_ID = "afterimage";
    public static final String POTION_ID = "afterimage";
    public static final String LONG_POTION_ID = "long_afterimage";

    public static final int COLOR = 0xB8E2F2;
    public static final int DURATION = 3600;
    public static final int LONG_DURATION = 9600;

    public static final Item INGREDIENT = Items.ENDER_PEARL;

    //? if >=1.21 {
    public static Holder<MobEffect> AFTERIMAGE;
    public static Holder<Potion> AFTERIMAGE_POTION;
    public static Holder<Potion> LONG_AFTERIMAGE_POTION;
    //?} else {
    /*public static Supplier<MobEffect> AFTERIMAGE;
    public static Supplier<Potion> AFTERIMAGE_POTION;
    public static Supplier<Potion> LONG_AFTERIMAGE_POTION;
    *///?}

    public static MobEffect createEffect() {
        return new AfterimageEffect();
    }

    public static Potion createPotion(int duration) {
        //? if >=1.21 {
        return new Potion(POTION_ID, new MobEffectInstance(AFTERIMAGE, duration));
        //?} else
        //return new Potion(POTION_ID, new MobEffectInstance(AFTERIMAGE.get(), duration));
    }

    //? if >=1.21 {
    public static void addMixes(PotionBrewing.Builder builder) {
        builder.addMix(Potions.AWKWARD, INGREDIENT, AFTERIMAGE_POTION);
        builder.addMix(AFTERIMAGE_POTION, Items.REDSTONE, LONG_AFTERIMAGE_POTION);
    }
    //?} else {
    /*public static void addMixes() {
        PotionBrewingInvoker.afterimages$addMix(Potions.AWKWARD, INGREDIENT, AFTERIMAGE_POTION.get());
        PotionBrewingInvoker.afterimages$addMix(AFTERIMAGE_POTION.get(), Items.REDSTONE, LONG_AFTERIMAGE_POTION.get());
    }
    *///?}

    public static boolean isAfterimage(MobEffectInstance instance) {
        if (AFTERIMAGE == null) return false;
        //? if >=1.21 {
        return instance.is(AFTERIMAGE);
        //?} else
        //return instance.getEffect() == AFTERIMAGE.get();
    }

    public static boolean hasAfterimage(Entity entity) {
        if (AFTERIMAGE == null || !(entity instanceof LivingEntity living)) return false;
        //? if >=1.21 {
        return living.hasEffect(AFTERIMAGE);
        //?} else
        //return living.hasEffect(AFTERIMAGE.get());
    }

    public static void broadcastUpdate(LivingEntity entity, MobEffectInstance instance) {
        if (!isAfterimage(instance) || !(entity.level() instanceof ServerLevel level)) return;
        level.getChunkSource().broadcast(entity, updatePacket(entity, instance));
    }

    public static void broadcastRemoval(LivingEntity entity, MobEffectInstance instance) {
        if (!isAfterimage(instance) || !(entity.level() instanceof ServerLevel level)) return;
        level.getChunkSource().broadcast(entity, new ClientboundRemoveMobEffectPacket(entity.getId(), instance.getEffect()));
    }

    public static void sendToPlayer(Entity entity, ServerPlayer player) {
        if (AFTERIMAGE == null || !(entity instanceof LivingEntity living)) return;
        //? if >=1.21 {
        MobEffectInstance instance = living.getEffect(AFTERIMAGE);
        //?} else
        //MobEffectInstance instance = living.getEffect(AFTERIMAGE.get());
        if (instance != null) {
            player.connection.send(updatePacket(living, instance));
        }
    }

    private static Packet<?> updatePacket(LivingEntity entity, MobEffectInstance instance) {
        //? if >=1.21 {
        return new ClientboundUpdateMobEffectPacket(entity.getId(), instance, false);
        //?} else
        //return new ClientboundUpdateMobEffectPacket(entity.getId(), instance);
    }
}
