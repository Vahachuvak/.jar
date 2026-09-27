package com.cheatclient.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** Число в диапазоне [min; max] с шагом step. В GUI — ползунок. */
public final class NumberSetting extends Setting<Double> {

    private final double min, max, step;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getValue() { return value; }
    public float getFloat() { return value.floatValue(); }

    /** Ставит значение, округляя до шага и зажимая в диапазон. */
    @Override
    public void set(Double v) {
        double snapped = Math.round(v / step) * step;
        this.value = Math.max(min, Math.min(max, snapped));
    }

    /** Доля заполнения ползунка 0..1. */
    public double fraction() { return (value - min) / (max - min); }

    public void setFraction(double f) {
        set(min + Math.max(0, Math.min(1, f)) * (max - min));
    }

    public String format() {
        return step >= 1 ? String.valueOf(Math.round(value)) : String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    @Override public JsonElement toJson() { return new JsonPrimitive(value); }

    @Override public void fromJson(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) set(json.getAsDouble());
    }
}
