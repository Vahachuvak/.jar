package com.cheatclient.gui;

import com.cheatclient.CheatClient;
import com.cheatclient.config.Config;
import com.cheatclient.module.Category;
import com.cheatclient.module.Module;
import com.cheatclient.setting.BoolSetting;
import com.cheatclient.setting.ModeSetting;
import com.cheatclient.setting.NumberSetting;
import com.cheatclient.setting.Setting;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClickGUI (открывается правым Shift).
 *
 *  Панели = категории. Шапку панели можно таскать мышью, ПКМ по шапке — свернуть.
 *  Модуль:  ЛКМ — вкл/выкл, ПКМ — раскрыть настройки.
 *  Настройки: галочка — клик; ползунок — зажать и тянуть; режим — ЛКМ/ПКМ листают.
 *  «Bind» — клик, затем нажми клавишу (Esc/Backspace/Delete — снять бинд).
 *  Всё сохраняется в config/cheatclient.json при закрытии меню.
 *
 * Устройство: каждый кадр строится список прямоугольников-элементов (layout()).
 * По нему же рисуем и определяем, куда кликнули — никакого рассинхрона.
 */
public final class ClickGuiScreen extends Screen {

    // Размеры и цвета (ARGB; альфа обязательна — без неё в 1.21.6+ ничего не видно).
    private static final int PANEL_W = 118, HEADER_H = 16, ROW_H = 14;
    private static final int C_PANEL = 0xE0121218, C_HEADER = 0xFF6A3DE8, C_ENABLED = 0xFF4E2DB0,
            C_HOVER = 0x30FFFFFF, C_TEXT = 0xFFFFFFFF, C_DIM = 0xFFA0A0B0, C_SETTING_BG = 0xE01A1A24,
            C_ACCENT = 0xFFB388FF, C_ON = 0xFF7CFFA0, C_OFF = 0xFFFF6B6B;

    private enum Kind { HEADER, MODULE, BOOL, NUMBER, MODE, BIND }

    private record Element(Kind kind, int x, int y, int w, int h, Category category, Module module, Setting<?> setting) {
        boolean hovered(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private static final class Panel {
        int x, y;
        boolean collapsed;
        Panel(int x, int y) { this.x = x; this.y = y; }
    }

    // Состояние сохраняется между открытиями меню (static).
    private static final Map<Category, Panel> PANELS = new EnumMap<>(Category.class);
    private static final Set<Module> EXPANDED = new HashSet<>();

    private Panel dragging;
    private int dragOffX, dragOffY;
    private NumberSetting slider;
    private int sliderX, sliderW;
    private Module listeningBind;
    private boolean listeningGuiBind;

    public ClickGuiScreen() {
        super(Component.literal("CheatClient"));
        int x = 10;
        for (Category c : Category.values()) {
            PANELS.computeIfAbsent(c, k -> new Panel(0, 0));
            if (PANELS.get(c).x == 0 && PANELS.get(c).y == 0) {
                PANELS.get(c).x = x;
                PANELS.get(c).y = 24;
            }
            x += PANEL_W + 10;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------------------------------------------------------ раскладка

    private List<Element> layout() {
        List<Element> out = new ArrayList<>();
        for (Category c : Category.values()) {
            Panel p = PANELS.get(c);
            int y = p.y;
            out.add(new Element(Kind.HEADER, p.x, y, PANEL_W, HEADER_H, c, null, null));
            y += HEADER_H;
            if (p.collapsed) continue;

            for (Module m : CheatClient.MODULES.byCategory(c)) {
                out.add(new Element(Kind.MODULE, p.x, y, PANEL_W, ROW_H, c, m, null));
                y += ROW_H;
                if (!EXPANDED.contains(m)) continue;

                for (Setting<?> s : m.getSettings()) {
                    Kind k = s instanceof BoolSetting ? Kind.BOOL : s instanceof NumberSetting ? Kind.NUMBER : Kind.MODE;
                    out.add(new Element(k, p.x, y, PANEL_W, ROW_H, c, m, s));
                    y += ROW_H;
                }
                out.add(new Element(Kind.BIND, p.x, y, PANEL_W, ROW_H, c, m, null));
                y += ROW_H;
            }
        }
        return out;
    }

    // ------------------------------------------------------------ отрисовка

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        Element hoveredModule = null;
        for (Element e : layout()) {
            boolean hover = e.hovered(mouseX, mouseY);
            switch (e.kind) {
                case HEADER -> {
                    g.fill(e.x, e.y, e.x + e.w, e.y + e.h, C_HEADER);
                    g.drawString(font, e.category.title, e.x + 5, e.y + 4, C_TEXT, true);
                    String arrow = PANELS.get(e.category).collapsed ? "+" : "-";
                    g.drawString(font, arrow, e.x + e.w - 10, e.y + 4, C_TEXT, true);
                }
                case MODULE -> {
                    Module m = e.module;
                    g.fill(e.x, e.y, e.x + e.w, e.y + e.h, m.isEnabled() ? C_ENABLED : C_PANEL);
                    if (hover) { g.fill(e.x, e.y, e.x + e.w, e.y + e.h, C_HOVER); hoveredModule = e; }
                    g.drawString(font, m.getName(), e.x + 5, e.y + 3, m.isEnabled() ? C_TEXT : C_DIM, false);
                    g.drawString(font, EXPANDED.contains(m) ? "v" : ">", e.x + e.w - 10, e.y + 3, C_DIM, false);
                }
                case BOOL -> {
                    BoolSetting s = (BoolSetting) e.setting;
                    drawSettingBg(g, e, hover);
                    g.drawString(font, s.getName(), e.x + 9, e.y + 3, C_TEXT, false);
                    int bx = e.x + e.w - 14;
                    g.fill(bx, e.y + 3, bx + 8, e.y + 11, 0xFF2A2A36);
                    if (s.isOn()) g.fill(bx + 1, e.y + 4, bx + 7, e.y + 10, C_ACCENT);
                }
                case NUMBER -> {
                    NumberSetting s = (NumberSetting) e.setting;
                    drawSettingBg(g, e, hover);
                    int sx = e.x + 6, sw = e.w - 12;
                    g.fill(sx, e.y + e.h - 3, sx + sw, e.y + e.h - 1, 0xFF2A2A36);
                    g.fill(sx, e.y + e.h - 3, sx + (int) (sw * s.fraction()), e.y + e.h - 1, C_ACCENT);
                    g.drawString(font, s.getName(), e.x + 9, e.y + 2, C_TEXT, false);
                    String v = s.format();
                    g.drawString(font, v, e.x + e.w - 6 - font.width(v), e.y + 2, C_ACCENT, false);
                }
                case MODE -> {
                    ModeSetting s = (ModeSetting) e.setting;
                    drawSettingBg(g, e, hover);
                    g.drawString(font, s.getName(), e.x + 9, e.y + 3, C_TEXT, false);
                    String v = s.get();
                    g.drawString(font, v, e.x + e.w - 6 - font.width(v), e.y + 3, C_ACCENT, false);
                }
                case BIND -> {
                    drawSettingBg(g, e, hover);
                    String v = listeningBind == e.module ? "..." : keyName(e.module.getKey());
                    g.drawString(font, "Bind", e.x + 9, e.y + 3, C_DIM, false);
                    g.drawString(font, v, e.x + e.w - 6 - font.width(v), e.y + 3,
                            listeningBind == e.module ? C_ON : C_DIM, false);
                }
            }
        }

        // Подсказка внизу экрана.
        String hint = hoveredModule != null
                ? hoveredModule.module.getDescription()
                : "ЛКМ — вкл/выкл · ПКМ — настройки · шапку можно таскать";
        g.drawCenteredString(font, hint, width / 2, height - 28, C_DIM);

        String guiBind = "Клавиша меню: " + (listeningGuiBind ? "..." : keyName(CheatClient.MODULES.guiKey)) + "  (клик — сменить)";
        g.drawCenteredString(font, guiBind, width / 2, height - 16, listeningGuiBind ? C_ON : C_DIM);
    }

    private void drawSettingBg(GuiGraphics g, Element e, boolean hover) {
        g.fill(e.x, e.y, e.x + e.w, e.y + e.h, C_SETTING_BG);
        g.fill(e.x, e.y, e.x + 2, e.y + e.h, C_ACCENT); // полоска слева = это настройка
        if (hover) g.fill(e.x, e.y, e.x + e.w, e.y + e.h, C_HOVER);
    }

    private static String keyName(int key) {
        if (key <= 0) return "None";
        return InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName().getString();
    }

    private boolean guiBindHovered(double mx, double my) {
        String text = "Клавиша меню: " + keyName(CheatClient.MODULES.guiKey) + "  (клик — сменить)";
        int w = font.width(text);
        return my >= height - 18 && my < height - 6 && mx >= width / 2.0 - w / 2.0 && mx <= width / 2.0 + w / 2.0;
    }

    // ------------------------------------------------------------ мышь

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        int button = event.button();
        listeningBind = null;

        if (button == 0 && guiBindHovered(mx, my)) {
            listeningGuiBind = true;
            return true;
        }
        listeningGuiBind = false;

        // Идём с конца, чтобы верхние (позже нарисованные) панели получали клик первыми.
        List<Element> elements = layout();
        for (int i = elements.size() - 1; i >= 0; i--) {
            Element e = elements.get(i);
            if (!e.hovered(mx, my)) continue;

            switch (e.kind) {
                case HEADER -> {
                    Panel p = PANELS.get(e.category);
                    if (button == 0) {
                        dragging = p;
                        dragOffX = (int) mx - p.x;
                        dragOffY = (int) my - p.y;
                    } else if (button == 1) {
                        p.collapsed = !p.collapsed;
                    }
                }
                case MODULE -> {
                    if (button == 0) e.module.toggle();
                    else if (button == 1 && !EXPANDED.remove(e.module)) EXPANDED.add(e.module);
                }
                case BOOL -> ((BoolSetting) e.setting).toggle();
                case NUMBER -> {
                    slider = (NumberSetting) e.setting;
                    sliderX = e.x + 6;
                    sliderW = e.w - 12;
                    updateSlider(mx);
                }
                case MODE -> ((ModeSetting) e.setting).cycle(button == 1 ? -1 : 1);
                case BIND -> listeningBind = e.module;
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging != null) {
            dragging.x = (int) event.x() - dragOffX;
            dragging.y = (int) event.y() - dragOffY;
            return true;
        }
        if (slider != null) {
            updateSlider(event.x());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        slider = null;
        return super.mouseReleased(event);
    }

    private void updateSlider(double mx) {
        slider.setFraction((mx - sliderX) / sliderW);
    }

    // ------------------------------------------------------------ клавиатура

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        boolean clear = key == InputConstants.KEY_ESCAPE || key == InputConstants.KEY_BACKSPACE || key == InputConstants.KEY_DELETE;

        if (listeningBind != null) {
            listeningBind.setKey(clear ? -1 : key);
            listeningBind = null;
            return true;
        }
        if (listeningGuiBind) {
            if (!clear) CheatClient.MODULES.guiKey = key; // меню без клавиши оставить нельзя
            listeningGuiBind = false;
            return true;
        }
        if (key == CheatClient.MODULES.guiKey) {
            onClose();
            return true;
        }
        return super.keyPressed(event); // Esc закрывает меню
    }

    @Override
    public void onClose() {
        Config.save();
        super.onClose();
    }
}
