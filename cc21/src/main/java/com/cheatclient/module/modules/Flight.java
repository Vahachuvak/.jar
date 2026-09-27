package com.cheatclient.module.modules;

import com.cheatclient.module.Category;
import com.cheatclient.module.Module;
import com.cheatclient.setting.NumberSetting;
import com.cheatclient.util.MovementUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Flight — полёт в выживании.
 *
 * LivingEntity#travel() каждый тик делает: move(скорость) → скорость.y −= гравитация → трение.
 * Мы в конце тика целиком перезаписываем скорость: в следующем тике игрок сдвинется ровно
 * на наш вектор, а гравитация и трение снова будут затёрты. Гравитация фактически = 0.
 *
 * Пробел — вверх, Shift — вниз, WASD — по горизонтали относительно взгляда.
 * На сервере без allow-flight=true сервер кикнет за полёт.
 */
public final class Flight extends Module {

    private final NumberSetting speed = add(new NumberSetting("Speed", 1.0, 0.1, 3.0, 0.1));
    private final NumberSetting vertical = add(new NumberSetting("Vertical", 0.6, 0.1, 2.0, 0.1));

    public Flight() {
        super("Flight", "Полёт в выживании. Пробел/Shift — вверх/вниз", Category.MOVEMENT, InputConstants.KEY_G);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        Vec3 dir = MovementUtil.inputDirection(player).scale(speed.getValue());

        double y = 0;
        if (player.input.keyPresses.jump()) y += vertical.getValue();
        if (player.input.keyPresses.shift()) y -= vertical.getValue();

        player.setDeltaMovement(dir.x, y, dir.z);
        player.fallDistance = 0;
    }

    @Override
    protected void onDisable() {
        LocalPlayer player = mc().player;
        if (player != null) player.setDeltaMovement(Vec3.ZERO);
    }
}
