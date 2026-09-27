package com.cheatclient.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Математика наведения.
 *
 *  yaw   — поворот вокруг вертикали: 0° = +Z (юг), 90° = −X (запад), −90° = +X (восток).
 *  pitch — наклон головы: −90° вверх, 0° горизонт, +90° вниз.
 *
 *  Для вектора d = цель − глаза:
 *    yaw   = deg(atan2(dz, dx)) − 90   (atan2 отсчитывает от +X, Minecraft — от +Z)
 *    pitch = −deg(atan2(dy, √(dx² + dz²)))   (минус: «вверх» в Minecraft отрицательный)
 *  Проверка: цель точно на юге (dx=0, dz=1) → atan2 = 90° → yaw = 0°. ✔
 */
public final class RotationUtil {

    private RotationUtil() {}

    public static float[] rotationsTo(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        return new float[]{Mth.wrapDegrees(yaw), Mth.clamp(pitch, -90f, 90f)};
    }

    /**
     * Ближайшая к глазам точка хитбокса: покомпонентно «зажимаем» позицию глаз внутрь AABB.
     * По ней и целимся, и меряем дальность — так же, как сервер проверяет досягаемость.
     */
    public static Vec3 closestPoint(Vec3 eyes, AABB box) {
        return new Vec3(
                Mth.clamp(eyes.x, box.minX, box.maxX),
                Mth.clamp(eyes.y, box.minY, box.maxY),
                Mth.clamp(eyes.z, box.minZ, box.maxZ));
    }

    /** Шаг от current к target не больше maxStep градусов, по кратчайшей дуге. */
    public static float stepTowards(float current, float target, float maxStep) {
        float delta = Mth.wrapDegrees(target - current);
        return current + Mth.clamp(delta, -maxStep, maxStep);
    }

    public static float angleDifference(float yaw, float pitch, float targetYaw, float targetPitch) {
        float dYaw = Math.abs(Mth.wrapDegrees(targetYaw - yaw));
        float dPitch = Math.abs(targetPitch - pitch);
        return (float) Math.sqrt(dYaw * dYaw + dPitch * dPitch);
    }
}
