package com.cheatclient.module;

import com.cheatclient.config.Config;
import com.cheatclient.gui.ClickGuiScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Хранит модули, опрашивает клавиши и раздаёт события.
 *
 * Клавиши опрашиваются напрямую через GLFW (InputConstants.isKeyDown) раз в тик,
 * а не через ванильные KeyMapping — так бинды можно менять прямо в ClickGUI
 * и не нужен Fabric API. Срабатывание — по «фронту»: клавиша нажата сейчас,
 * но не была нажата в прошлом тике.
 */
public final class ModuleManager {

    private final List<Module> modules = new ArrayList<>();
    private final Set<Integer> heldKeys = new HashSet<>();

    /** Клавиша открытия меню. По умолчанию правый Shift. Сохраняется в конфиге. */
    public int guiKey = InputConstants.KEY_RSHIFT;

    public void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> byCategory(Category category) {
        return modules.stream().filter(m -> m.getCategory() == category).toList();
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> type) {
        for (Module m : modules) if (type.isInstance(m)) return (T) m;
        throw new IllegalArgumentException("Module not registered: " + type);
    }

    // ------------------------------------------------------------ события

    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            heldKeys.clear();
            return;
        }

        // Бинды работают только когда не открыт никакой экран (чат, инвентарь, меню).
        if (mc.screen == null) {
            if (pressedNow(mc, guiKey)) {
                mc.setScreen(new ClickGuiScreen());
            }
            for (Module m : modules) {
                if (m.getKey() > 0 && pressedNow(mc, m.getKey())) {
                    m.toggle();
                    Config.save();
                }
            }
        } else {
            // Пока открыт экран, только обновляем состояние клавиш, ничего не переключая.
            // Иначе клавиша, которой закрыли меню, сразу открыла бы его снова.
            pressedNow(mc, guiKey);
            for (Module m : modules) if (m.getKey() > 0) pressedNow(mc, m.getKey());
        }

        for (Module m : modules) {
            if (m.isEnabled()) m.onTick(mc);
        }
    }

    public void onHud(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        for (Module m : modules) {
            if (m.isEnabled()) m.onHud(g, delta);
        }
    }

    /** true ровно один раз на каждое нажатие клавиши. */
    private boolean pressedNow(Minecraft mc, int key) {
        boolean down = InputConstants.isKeyDown(mc.getWindow(), key);
        if (down) return heldKeys.add(key);   // add() == true → раньше не была зажата
        heldKeys.remove(key);
        return false;
    }
}
