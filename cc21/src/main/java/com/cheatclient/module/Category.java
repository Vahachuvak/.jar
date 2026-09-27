package com.cheatclient.module;

/** Категории = панели в ClickGUI. */
public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render");

    public final String title;

    Category(String title) {
        this.title = title;
    }
}
