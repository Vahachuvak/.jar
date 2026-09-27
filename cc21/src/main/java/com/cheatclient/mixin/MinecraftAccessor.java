package com.cheatclient.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Доступ к приватному Minecraft#startAttack() — ровно то, что вызывает игра при нажатии ЛКМ:
 * удар по сущности под прицелом, начало ломания блока или взмах мимо, со всеми
 * ванильными проверками (кулдаун промаха, занятые руки и т.д.).
 */
@Mixin(Minecraft.class)
public interface MinecraftAccessor {

    @Invoker("startAttack")
    boolean cheatclient$startAttack();
}
