package com.cheatclient.setting;

import com.google.gson.JsonElement;

/** Настройка модуля. Отображается в ClickGUI и сохраняется в config/cheatclient.json. */
public abstract class Setting<T> {

    private final String name;
    protected T value;

    protected Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public T get() { return value; }
    public void set(T value) { this.value = value; }

    public abstract JsonElement toJson();
    public abstract void fromJson(JsonElement json);
}
