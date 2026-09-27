package com.cheatclient.module;

import com.cheatclient.setting.Setting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Базовый класс модуля.
 *
 *  - name / description — показываются в ClickGUI;
 *  - key — GLFW-код клавиши переключения (-1 = не назначена). Меняется в GUI;
 *  - settings — список настроек, GUI строит для них элементы автоматически.
 *
 * ModuleManager вызывает onTick / onHud только у включённых модулей и только в мире.
 */
public abstract class Module {

    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private int key;
    private boolean enabled;

    protected Module(String name, String description, Category category, int defaultKey) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.key = defaultKey;
    }

    protected static Minecraft mc() {
        return Minecraft.getInstance();
    }

    /** Регистрирует настройку (вызывать в конструкторе модуля). */
    protected <S extends Setting<?>> S add(S setting) {
        settings.add(setting);
        return setting;
    }

    // ------------------------------------------------------------ состояние

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        if (mc().player != null) {
            if (value) onEnable(); else onDisable();
            mc().player.displayClientMessage(Component.literal(name + ": ")
                    .append(Component.literal(value ? "ON" : "OFF")
                            .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED)), true);
        }
    }

    public final boolean isEnabled() { return enabled; }
    public final String getName() { return name; }
    public final String getDescription() { return description; }
    public final Category getCategory() { return category; }
    public final List<Setting<?>> getSettings() { return Collections.unmodifiableList(settings); }
    public final int getKey() { return key; }
    public final void setKey(int key) { this.key = key; }

    // ------------------------------------------------------------ хуки

    protected void onEnable() {}
    protected void onDisable() {}

    /** 20 раз в секунду, в конце Minecraft.tick(). */
    public void onTick(Minecraft mc) {}

    /** Каждый кадр, до отрисовки ванильного HUD (хотбар и чат будут поверх). */
    public void onHud(GuiGraphics g, DeltaTracker delta) {}
}
