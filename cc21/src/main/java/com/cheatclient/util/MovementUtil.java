package com.cheatclient.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * WASD → направление в мире.
 *
 * player.input.getMoveVector() — вектор ввода «относительно взгляда»:
 *   x = вбок (A = +1, D = -1), y = вперёд (W = +1, S = -1).
 * Поворачиваем его на yaw вокруг оси Y. В Minecraft yaw = 0 смотрит на +Z (юг)
 * и растёт по часовой (90 = -X). Формулы те же, что в Entity#getInputVector:
 *   x = side·cos(yaw) − forward·sin(yaw)
 *   z = forward·cos(yaw) + side·sin(yaw)
 */
public final class MovementUtil {

    private MovementUtil() {}

    public static Vec3 inputDirection(LocalPlayer player) {
        Vec2 move = player.input.getMoveVector();
        float side = move.x;
        float forward = move.y;
        if (Math.abs(side) < 1.0E-4F && Math.abs(forward) < 1.0E-4F) return Vec3.ZERO;

        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        float sin = Mth.sin(yaw);
        float cos = Mth.cos(yaw);

        double x = side * cos - forward * sin;
        double z = forward * cos + side * sin;
        // Нормализация: по диагонали скорость не больше, чем прямо.
        return new Vec3(x, 0, z).normalize();
    }
}
