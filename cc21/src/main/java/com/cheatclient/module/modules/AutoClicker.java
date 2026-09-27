package com.cheatclient.module.modules;

import com.cheatclient.mixin.MinecraftAccessor;
import com.cheatclient.module.Category;
import com.cheatclient.module.Module;
import com.cheatclient.setting.ModeSetting;
import com.cheatclient.setting.NumberSetting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

/**
 * AutoClicker — автоклики ЛКМ.
 *   Hold   — пока зажата левая кнопка мыши;
 *   Always — постоянно, пока модуль включён.
 *
 * Логика идёт 20 раз/с, поэтому CPS считается «аккумулятором»: каждый тик
 * прибавляем cps/20, при накоплении ≥ 1 — клик. При 13 CPS клики идут через 1–2 тика,
 * в среднем 13/с. Случайный разброс делает ритм неровным. Максимум — 20 CPS.
 */
public final class AutoClicker extends Module {

    private final ModeSetting mode = add(new ModeSetting("Mode", "Hold", "Hold", "Always"));
    private final NumberSetting cps = add(new NumberSetting("CPS", 12, 1, 20, 1));
    private final NumberSetting jitter = add(new NumberSetting("Jitter", 2, 0, 5, 1));

    private final Random random = new Random();
    private double accumulator;

    public AutoClicker() {
        super("AutoClicker", "Автоматические клики ЛКМ", Category.COMBAT, InputConstants.KEY_J);
    }

    @Override
    protected void onEnable() {
        accumulator = 0;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.screen != null || mc.gameMode == null || mc.player.isUsingItem()) {
            accumulator = 0;
            return;
        }
        if (mode.is("Hold") && GLFW.glfwGetMouseButton(mc.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS) {
            accumulator = 0;
            return;
        }

        double current = cps.getValue() + (random.nextDouble() * 2 - 1) * jitter.getValue();
        accumulator += Math.max(1, Math.min(20, current)) / 20.0;

        if (accumulator >= 1) {
            accumulator -= 1;
            ((MinecraftAccessor) mc).cheatclient$startAttack();
        }
    }
}
