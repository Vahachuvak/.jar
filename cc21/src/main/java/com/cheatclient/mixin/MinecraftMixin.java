package com.cheatclient.mixin;

import com.cheatclient.CheatClient;
import com.cheatclient.module.modules.ESP;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    /**
     * Конец клиентского тика. К этому моменту игрок уже обработал ввод и движение,
     * поэтому скорость, выставленная модулями здесь, применится в следующем тике.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void cheatclient$onTick(CallbackInfo ci) {
        CheatClient.MODULES.onTick((Minecraft) (Object) this);
    }

    /**
     * Ванильный метод решает, рисовать ли сущности контур свечения (как от спектральной
     * стрелы). Контур рисуется поверх блоков — это и есть «сквозь стены».
     * Возвращаем true для целей ESP.
     */
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void cheatclient$glow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (ESP.shouldGlow(entity)) cir.setReturnValue(true);
    }
}
