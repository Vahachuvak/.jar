package com.cheatclient;

import com.cheatclient.config.Config;
import com.cheatclient.module.ModuleManager;
import com.cheatclient.module.modules.AutoClicker;
import com.cheatclient.module.modules.ESP;
import com.cheatclient.module.modules.Flight;
import com.cheatclient.module.modules.Hud;
import com.cheatclient.module.modules.KillAura;
import com.cheatclient.module.modules.Speed;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Точка входа (entrypoint "client").
 *
 * Мод не зависит от Fabric API: вместо его событий используются три mixin-хука:
 *   MinecraftMixin → конец Minecraft.tick()     → MODULES.onTick()
 *   GuiMixin       → начало Gui.render()        → MODULES.onHud()
 *   MinecraftMixin / EntityMixin                → свечение сущностей для ESP
 * Поэтому в папку mods кладётся только один jar.
 */
public final class CheatClient implements ClientModInitializer {

    public static final String MOD_ID = "cheatclient";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ModuleManager MODULES = new ModuleManager();

    @Override
    public void onInitializeClient() {
        MODULES.register(new KillAura());
        MODULES.register(new AutoClicker());
        MODULES.register(new Flight());
        MODULES.register(new Speed());
        MODULES.register(new ESP());
        MODULES.register(new Hud());

        Config.load();
        LOGGER.info("CheatClient загружен. Меню: правый Shift.");
    }
}
