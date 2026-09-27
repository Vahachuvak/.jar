package com.cheatclient.module.modules;

import com.cheatclient.module.Category;
import com.cheatclient.module.Module;
import com.cheatclient.setting.BoolSetting;
import com.cheatclient.setting.ModeSetting;
import com.cheatclient.setting.NumberSetting;
import com.cheatclient.util.Projection;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

/**
 * ESP — подсветка сущностей сквозь стены.
 *
 * Режимы:
 *   Glow — ванильный контур свечения (как от спектральной стрелы), виден сквозь блоки.
 *          Включается mixin'ом в Minecraft#shouldEntityAppearGlowing, цвет — в Entity#getTeamColor.
 *   Box  — 2D-рамка на экране: 8 углов хитбокса проецируются на экран (см. Projection),
 *          берём min/max по X и Y → прямоугольник. Рисуется в HUD, поэтому всегда поверх мира.
 *   Both — оба сразу.
 * Дополнительно: полоска здоровья слева от рамки и подпись «имя · HP · дистанция».
 *
 * Интерполяция: мир тикает 20 раз/с, а кадров больше. Позицию берём
 * getPosition(partialTick) — между прошлым и текущим тиком, иначе рамки дёргаются.
 */
public final class ESP extends Module {

    private final ModeSetting mode = add(new ModeSetting("Mode", "Both", "Box", "Glow", "Both"));
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting monsters = add(new BoolSetting("Monsters", true));
    private final BoolSetting animals = add(new BoolSetting("Animals", false));
    private final BoolSetting labels = add(new BoolSetting("Labels", true));
    private final BoolSetting healthBar = add(new BoolSetting("HealthBar", true));
    private final NumberSetting maxDistance = add(new NumberSetting("Distance", 96, 16, 256, 8));

    private static ESP instance;

    public ESP() {
        super("ESP", "Рамки, здоровье и дистанция сквозь стены", Category.RENDER, InputConstants.KEY_K);
        instance = this;
    }

    // ------------------------------------------------------------ фильтр (используется и mixin'ами)

    private boolean isTarget(Entity e) {
        Minecraft mc = mc();
        if (!(e instanceof LivingEntity) || e == mc.player || !e.isAlive() || e instanceof ArmorStand) return false;
        if (mc.player == null || e.distanceToSqr(mc.player) > maxDistance.getValue() * maxDistance.getValue()) return false;
        if (e instanceof Player) return players.isOn();
        if (e instanceof Enemy) return monsters.isOn();
        return animals.isOn();
    }

    /** Вызывается из mixin'ов при рендере каждой сущности — должна быть дешёвой. */
    public static boolean shouldGlow(Entity e) {
        ESP esp = instance;
        return esp != null && esp.isEnabled() && !esp.mode.is("Box") && esp.isTarget(e);
    }

    /** ARGB-цвет по типу: игроки — красный, монстры — оранжевый, остальные — зелёный. */
    public static int colorFor(Entity e) {
        if (e instanceof Player) return 0xFFFF4040;
        if (e instanceof Enemy) return 0xFFFFA020;
        return 0xFF40FF60;
    }

    // ------------------------------------------------------------ 2D-рамки и подписи

    @Override
    public void onHud(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = mc();
        float partialTick = delta.getGameTimeDeltaPartialTick(true);
        boolean drawBox = !mode.is("Glow");
        Font font = mc.font;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (!isTarget(e)) continue;
            LivingEntity living = (LivingEntity) e;

            // Хитбокс, сдвинутый в интерполированную позицию.
            Vec3 lerped = e.getPosition(partialTick);
            AABB box = e.getBoundingBox().move(lerped.subtract(e.position()));

            float[] rect = projectBox(box);
            if (rect == null) continue;
            int x1 = (int) rect[0], y1 = (int) rect[1], x2 = (int) rect[2], y2 = (int) rect[3];
            if (x2 - x1 < 2 || y2 - y1 < 2) continue;

            int color = colorFor(e);

            if (drawBox) {
                g.renderOutline(x1 - 1, y1 - 1, x2 - x1 + 2, y2 - y1 + 2, 0xAA000000); // тёмная обводка для контраста
                g.renderOutline(x1, y1, x2 - x1, y2 - y1, color);
            }

            float hpFrac = Mth.clamp(living.getHealth() / Math.max(1f, living.getMaxHealth()), 0f, 1f);
            if (healthBar.isOn()) {
                int barX = x1 - 4;
                int filled = (int) ((y2 - y1) * hpFrac);
                g.fill(barX - 1, y1 - 1, barX + 2, y2 + 1, 0xAA000000);
                g.fill(barX, y2 - filled, barX + 1, y2, healthColor(hpFrac));
            }

            if (labels.isOn()) {
                double dist = mc.gameRenderer.getMainCamera().position().distanceTo(lerped);
                String text = String.format(Locale.ROOT, "%s  %.1f HP  %.0fm",
                        e.getName().getString(), living.getHealth(), dist);
                int tw = font.width(text);
                int tx = (x1 + x2) / 2 - tw / 2;
                int ty = y1 - font.lineHeight - 2;
                g.fill(tx - 2, ty - 1, tx + tw + 2, ty + font.lineHeight, 0x90000000);
                g.drawString(font, text, tx, ty, healthColor(hpFrac), false);
            }
        }
    }

    /** Проецирует 8 углов AABB; возвращает {minX, minY, maxX, maxY} или null, если хоть один угол за камерой. */
    private static float[] projectBox(AABB b) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        double[] xs = {b.minX, b.maxX}, ys = {b.minY, b.maxY}, zs = {b.minZ, b.maxZ};
        for (double x : xs) for (double y : ys) for (double z : zs) {
            float[] p = Projection.toScreen(new Vec3(x, y, z));
            if (p == null) return null;
            minX = Math.min(minX, p[0]);
            minY = Math.min(minY, p[1]);
            maxX = Math.max(maxX, p[0]);
            maxY = Math.max(maxY, p[1]);
        }
        return new float[]{minX, minY, maxX, maxY};
    }

    /** Красный (0 HP) → жёлтый → зелёный (полное HP). */
    private static int healthColor(float f) {
        int r = (int) (255 * Math.min(1f, 2f * (1f - f)));
        int gr = (int) (255 * Math.min(1f, 2f * f));
        return 0xFF000000 | (r << 16) | (gr << 8);
    }
}
