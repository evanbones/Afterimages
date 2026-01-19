package com.evandev.afterimages.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class AfterimagesMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return switch (mixinClassName) {
            case "com.evandev.afterimages.mixin.combatroll.RollEffectMixin" ->
                    isClassAvailable("net.combatroll.CombatRoll");
            case "com.evandev.afterimages.mixin.elenaidodge.DodgeHandlerMixin" ->
                    isClassAvailable("com.elenai.elenaidodge2.ElenaiDodge2");
            case "com.evandev.afterimages.mixin.azurelibarmor.GeoArmorRendererMixin" ->
                    isClassAvailable("mod.azure.azurelibarmor.renderer.GeoArmorRenderer");
            case "com.evandev.afterimages.mixin.emf.EMFModelPartMixin" ->
                    isClassAvailable("traben.entity_model_features.models.parts.EMFModelPart");
            default -> true;
        };
    }

    private boolean isClassAvailable(String className) {
        String path = className.replace('.', '/') + ".class";
        return this.getClass().getClassLoader().getResource(path) != null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}