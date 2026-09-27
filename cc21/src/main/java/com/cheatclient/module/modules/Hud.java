package com.cheatclient.module.modules;

import com.cheatclient.CheatClient;
import com.cheatclient.module.Category;
import com.cheatclient.module.Module;
import com.cheatclient.setting.BoolSetting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Надпись в углу и список включённых модулей. Включён по умолчанию. */
public final class Hud extends Module {

    private final BoolSetting watermark = add(new BoolSetting("Watermark", true));

    public Hud() {
        super("HUD", "Список включённых модулей на экране", Category.RENDER, -1);
        setEnabled(true);
    }

    @Override
    public void onHud(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = mc();
        int y = 4;
        if (watermark.isOn()) {
            g.drawString(mc.font, "CheatClient  [RShift — меню]", 4, y, 0xFFB388FF, true);
            y += 12;
        }
        for (Module m : CheatClient.MODULES.getModules()) {
            if (!m.isEnabled() || m == this) continue;
            g.drawString(mc.font, m.getName(), 4, y, 0xFF7CFFA0, true);
            y += 10;
        }
    }
}
