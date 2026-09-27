package com.cheatclient.mixin;

import com.cheatclient.CheatClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    /**
     * Начало отрисовки HUD. Рисуем ДО ванильных элементов, чтобы хотбар, чат
     * и меню оказались поверх рамок ESP. F1 (hideGui) скрывает и наш HUD.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void cheatclient$onHud(GuiGraphics g, DeltaTracker delta, CallbackInfo ci) {
        if (Minecraft.getInstance().options.hideGui) return;
        CheatClient.MODULES.onHud(g, delta);
    }
}
