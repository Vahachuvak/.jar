package com.cheatclient.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Arrays;
import java.util.List;

/** Выбор одного варианта из списка. В GUI: ЛКМ — следующий, ПКМ — предыдущий. */
public final class ModeSetting extends Setting<String> {

    private final List<String> modes;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name, defaultMode);
        this.modes = Arrays.asList(modes);
    }

    public boolean is(String mode) { return value.equalsIgnoreCase(mode); }

    public void cycle(int direction) {
        int i = modes.indexOf(value);
        i = Math.floorMod(i + direction, modes.size());
        value = modes.get(i);
    }

    @Override public JsonElement toJson() { return new JsonPrimitive(value); }

    @Override public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive() && modes.contains(json.getAsString())) {
            value = json.getAsString();
        }
    }
}
