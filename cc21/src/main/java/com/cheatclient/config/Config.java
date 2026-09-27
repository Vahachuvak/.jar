package com.cheatclient.config;

import com.cheatclient.CheatClient;
import com.cheatclient.module.Module;
import com.cheatclient.setting.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Сохранение в .minecraft/config/cheatclient.json:
 * включённые модули, их бинды и значения всех настроек.
 * Пишется при закрытии меню и при переключении модуля клавишей.
 */
public final class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Config() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("cheatclient.json");
    }

    public static void save() {
        JsonObject root = new JsonObject();
        root.addProperty("guiKey", CheatClient.MODULES.guiKey);

        JsonObject modules = new JsonObject();
        for (Module m : CheatClient.MODULES.getModules()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("enabled", m.isEnabled());
            obj.addProperty("key", m.getKey());
            JsonObject settings = new JsonObject();
            for (Setting<?> s : m.getSettings()) settings.add(s.getName(), s.toJson());
            obj.add("settings", settings);
            modules.add(m.getName(), obj);
        }
        root.add("modules", modules);

        try {
            Files.writeString(file(), GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception e) {
            CheatClient.LOGGER.warn("Не удалось сохранить конфиг", e);
        }
    }

    public static void load() {
        Path path = file();
        if (!Files.exists(path)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.has("guiKey")) CheatClient.MODULES.guiKey = root.get("guiKey").getAsInt();

            JsonObject modules = root.getAsJsonObject("modules");
            if (modules == null) return;
            for (Module m : CheatClient.MODULES.getModules()) {
                JsonObject obj = modules.getAsJsonObject(m.getName());
                if (obj == null) continue;
                if (obj.has("key")) m.setKey(obj.get("key").getAsInt());
                if (obj.has("enabled")) m.setEnabled(obj.get("enabled").getAsBoolean());
                JsonObject settings = obj.getAsJsonObject("settings");
                if (settings == null) continue;
                for (Setting<?> s : m.getSettings()) {
                    JsonElement el = settings.get(s.getName());
                    if (el != null) s.fromJson(el);
                }
            }
        } catch (Exception e) {
            CheatClient.LOGGER.warn("Конфиг повреждён, используются значения по умолчанию", e);
        }
    }
}
