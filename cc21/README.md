# CheatClient для Minecraft 1.21.11 (Fabric)

В папку `mods` кладётся ОДИН файл `cheatclient-2.0.0.jar`. Fabric API не нужен.
Нужен только сам Fabric Loader для 1.21.11 (ставится один раз установщиком с fabricmc.net).

## Как получить .jar

### Вариант А — без установки чего-либо (через GitHub)
1. Зарегистрируйся на github.com → New repository → создай пустой репозиторий.
2. «uploading an existing file» → перетащи ВСЁ содержимое этой папки (включая скрытую `.github`).
   Если браузер не видит `.github`, создай файл вручную: Add file → Create new file →
   имя `.github/workflows/build.yml` и вставь содержимое из архива.
3. Вкладка Actions → дождись зелёной галочки (~3–5 минут).
4. Справа в разделе Releases → «CheatClient (latest build)» → скачай `cheatclient-2.0.0.jar`.

### Вариант Б — на своём компьютере
1. Установи JDK 21: https://adoptium.net (Temurin 21).
2. Дважды кликни `build-windows.bat` — он соберёт мод и сам скопирует jar в `%APPDATA%\.minecraft\mods`.

## Управление
- Правый Shift — меню (ClickGUI). Модуль: ЛКМ вкл/выкл, ПКМ — настройки и бинд.
- Клавиши по умолчанию: R KillAura · J AutoClicker · G Flight · H Speed · K ESP.
- Настройки сохраняются в `.minecraft/config/cheatclient.json`.

Для одиночной игры и своего сервера (для Flight на сервере нужен allow-flight=true).
