package com.cheatclient.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** Галочка вкл/выкл. В GUI переключается кликом. */
public final class BoolSetting extends Setting<Boolean> {

    public BoolSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public boolean isOn() { return value; }
    public void toggle() { value = !value; }

    @Override public JsonElement toJson() { return new JsonPrimitive(value); }

    @Override public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) value = json.getAsBoolean();
    }
}
