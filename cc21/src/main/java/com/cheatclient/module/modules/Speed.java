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
 * Speed — быстрее по земле.
 *
 * Не умножаем текущую скорость каждый тик: трение на земле ≈ 0.546/тик, и при
 * множителе ≳ 1.83 скорость росла бы экспоненциально. Вместо этого задаём её явно:
 *     скорость.xz = направление_ввода × скорость_спринта × множитель
 * В воздухе и в воде модуль ничего не делает.
 */
public final class Speed extends Module {

    private static final double SPRINT_SPEED = 0.2806; // ванильный спринт по земле, блоков/тик

    private final NumberSetting multiplier = add(new NumberSetting("Multiplier", 1.6, 1.0, 3.0, 0.1));

    public Speed() {
        super("Speed", "Быстрый бег по земле", Category.MOVEMENT, InputConstants.KEY_H);
    }

    @Override
    public void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (!player.onGround() || player.isInWater() || player.isInLava()
                || player.isShiftKeyDown() || player.getAbilities().flying) return;

        Vec3 dir = MovementUtil.inputDirection(player);
        if (dir == Vec3.ZERO) return;

        double s = SPRINT_SPEED * multiplier.getValue();
        player.setDeltaMovement(dir.x * s, player.getDeltaMovement().y, dir.z * s);
    }
}
