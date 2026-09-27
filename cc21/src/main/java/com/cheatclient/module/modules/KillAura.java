package com.cheatclient.module.modules;

import com.cheatclient.module.Category;
import com.cheatclient.module.Module;
import com.cheatclient.setting.BoolSetting;
import com.cheatclient.setting.ModeSetting;
import com.cheatclient.setting.NumberSetting;
import com.cheatclient.util.RotationUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * KillAura / Aimbot.
 *
 *  1. Цель: ближайший живой монстр (интерфейс Enemy) или игрок, которого видно
 *     (hasLineOfSight) и до ближайшей точки хитбокса которого ≤ Range.
 *  2. Наведение (см. RotationUtil): Instant — сразу; Smooth — не больше Turn градусов за тик.
 *  3. Атака (если Attack вкл.): когда прицел отклонён < FOV градусов и шкала
 *     заряда удара заполнена (getAttackStrengthScale = 1 → полный урон).
 *     Удар — gameMode.attack(), тот же вызов, что при клике по мобу.
 *  С выключенным Attack модуль — чистый аимбот.
 */
public final class KillAura extends Module {

    private final ModeSetting aim = add(new ModeSetting("Aim", "Smooth", "Smooth", "Instant"));
    private final BoolSetting attack = add(new BoolSetting("Attack", true));
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting monsters = add(new BoolSetting("Monsters", true));
    private final NumberSetting range = add(new NumberSetting("Range", 3.5, 2.0, 6.0, 0.1));
    private final NumberSetting turn = add(new NumberSetting("Turn", 25, 5, 180, 5));
    private final NumberSetting fov = add(new NumberSetting("FOV", 15, 5, 90, 5));

    public KillAura() {
        super("KillAura", "Наводится на ближайшую цель и бьёт", Category.COMBAT, InputConstants.KEY_R);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (mc.screen != null || mc.gameMode == null || player.isSpectator()) return;

        Vec3 eyes = player.getEyePosition();
        LivingEntity target = findTarget(mc, player, eyes);
        if (target == null) return;

        Vec3 aimPoint = RotationUtil.closestPoint(eyes, target.getBoundingBox());
        float[] wanted = RotationUtil.rotationsTo(eyes, aimPoint);

        float step = aim.is("Instant") ? 180f : turn.getFloat();
        // Прибавляем разницу, а не ставим абсолют: yaw игрока не ограничен [−180; 180].
        player.setYRot(RotationUtil.stepTowards(player.getYRot(), wanted[0], step));
        player.setXRot(RotationUtil.stepTowards(player.getXRot(), wanted[1], step));

        if (!attack.isOn()) return;
        float error = RotationUtil.angleDifference(player.getYRot(), player.getXRot(), wanted[0], wanted[1]);
        if (error > fov.getFloat()) return;
        if (player.getAttackStrengthScale(0f) < 1f) return;

        mc.gameMode.attack(player, target);
        player.swing(InteractionHand.MAIN_HAND);
    }

    private LivingEntity findTarget(Minecraft mc, LocalPlayer player, Vec3 eyes) {
        double rangeSq = range.getValue() * range.getValue();
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity living) || e == player || !e.isAlive() || e.isInvisible()) continue;
            if (!isValidType(e)) continue;
            if (eyes.distanceToSqr(RotationUtil.closestPoint(eyes, e.getBoundingBox())) > rangeSq) continue;
            if (!player.hasLineOfSight(e)) continue;

            double d = player.distanceToSqr(e);
            if (d < bestDist) {
                bestDist = d;
                best = living;
            }
        }
        return best;
    }

    private boolean isValidType(Entity e) {
        if (e instanceof Player p) return players.isOn() && !p.isSpectator() && !p.isCreative();
        return monsters.isOn() && e instanceof Enemy;
    }
}
