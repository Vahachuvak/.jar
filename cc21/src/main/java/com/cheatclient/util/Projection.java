package com.cheatclient.util;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Перевод точки мира в координаты экрана (для 2D-рамок и подписей ESP).
 *
 *  1. rel = точка − позиция камеры.
 *  2. Поворачиваем rel на ОБРАТНЫЙ поворот камеры (conjugate кватерниона) —
 *     получаем координаты в системе камеры, где она смотрит вдоль −Z.
 *     Если z ≥ 0, точка позади камеры → не рисуем (иначе она «отразится» на экран).
 *  3. Ванильный GameRenderer#projectPointToScreen умножает точку на матрицу
 *     проекции (с текущим FOV) и делит на w → NDC: x,y ∈ [−1; 1].
 *  4. NDC → пиксели GUI: x = (ndc.x + 1)/2 · ширина, y = (1 − ndc.y)/2 · высота
 *     (ось Y экрана направлена вниз, в NDC — вверх).
 *
 * Покачивание камеры при ходьбе (View Bobbing) не учитывается — рамки могут
 * слегка «плавать» при беге; при выключенном View Bobbing совпадают точно.
 */
public final class Projection {

    private Projection() {}

    /** {x, y} в GUI-координатах или null, если точка позади камеры. */
    public static float[] toScreen(Vec3 world) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();

        Vector3f rel = world.subtract(camera.position()).toVector3f();
        rel.rotate(camera.rotation().conjugate(new Quaternionf()));
        if (rel.z >= -0.05f) return null;

        Vec3 ndc = mc.gameRenderer.projectPointToScreen(world);
        float w = mc.getWindow().getGuiScaledWidth();
        float h = mc.getWindow().getGuiScaledHeight();
        return new float[]{(float) (ndc.x + 1) * 0.5f * w, (float) (1 - ndc.y) * 0.5f * h};
    }
}
