package com.cheatclient.mixin;

import com.cheatclient.module.modules.ESP;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    /**
     * Цвет контура свечения берётся из Entity#getTeamColor() (RGB).
     * Для целей ESP подменяем его на цвет по типу: игроки / монстры / животные.
     */
    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void cheatclient$espColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;
        if (ESP.shouldGlow(self)) cir.setReturnValue(ESP.colorFor(self) & 0xFFFFFF);
    }
}
