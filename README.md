[![Final video of fixing issues in your code in VS Code](https://img.youtube.com/vi/30r3LJFjoHE/maxresdefault.jpg)](https://youtu.be/30r3LJFjoHE)

<p align="center">
  <a href="#english-documentation"><strong>English</strong></a> | 
  <a href="#russian-documentation"><strong>Русский</strong></a>
</p>

<a id="english-documentation"></a>
# JediTelekinesis Plugin

**Feel the Force!** This plugin for Spigot/Paper adds true Jedi abilities to Minecraft, inspired by the Star Wars universe. Control entities from a distance, perform powerful dashes, and push enemies away using the Force!

## Features

*   **Telekinesis:**
    *   Grab mobs and other players.
    *   Hold them in front of you.
    *   Gently release or powerfully throw targets.
    *   Obstacle checking when moving a held target.
    *   Configurable time limit for holding players.
*   **Force Dash:** Quickly move in the targeted direction.
*   **Force Push (Targeted):** Push a single target away.
*   **Force Push (Circular/AoE):** Push all hostile entities around you.
*   **Force Crystal:** A special item for focusing your abilities.
*   **Configurable Cooldowns:** Each ability's cooldown can be customized.
*   **Flexible Configuration:** Adjust range, strength, excluded mobs, effects (sounds, particles), and much more in `config.yml`.
*   **Localization:** Multi-language support: English and Russian by default (see `lang_xx.yml` files).
*   **Permissions:** Detailed access control for all commands and abilities.

## Supported Versions

*   **Minecraft:** 1.19.x (tested on 1.19.4, 1.20.1, and 1.21.4)
*   **Server Cores:** Spigot and its forks (Paper, etc.)

## Installation

1.  Download the latest `.jar` file of the plugin from the [Releases](https://github.com/foxdevtime/JediTelekinesisPlugin/releases) page.
2.  Place the `.jar` file into your server's `plugins` folder.
3.  Restart your server.
4.  The plugin will create a `JediTelekinesis` folder with configuration files (`config.yml`, `lang_en.yml`, `lang_ru.yml`).

## How to Use

1.  **Obtaining the Force Crystal:**
    *   Players with the `jeditelekenisis.command.givecrystal` permission (operators by default) can use the `/telekinesis` (or `/tk`) command to get a Force Crystal.
2.  **Using the Force Crystal:**
    *   Hold the Force Crystal in your main hand.
    *   **Free Hands (when not holding a target):**
        *   **RMB (Right Mouse Button):** Telekinesis (Grab target).
        *   **LMB (Left Mouse Button):** Force Dash.
        *   **Shift + RMB:** Targeted Force Push.
        *   **Shift + LMB:** Circular Force Push.
    *   **While Holding a Target:**
        *   **RMB:** Gently release the target.
        *   **Shift + RMB:** Throw the held target.
3.  **Breaking Free (if you are being held):**
    *   Players with the `jeditelekenisis.command.release` permission can use the `/tkrelease` command to try and break free.

## Commands

*   `/telekinesis` (or `/tk`)
    *   Description: Gives a Force Crystal.
    *   Permission: `jeditelekenisis.command.givecrystal`
*   `/tkrelease`
    *   Description: Allows a player to attempt to break free if being held by telekinesis.
    *   Permission: `jeditelekenisis.command.release`
*   `/jeditele [reload/rl]` (Planned command)
    *   Description: Reloads the plugin's configuration.
    *   Permission: `jeditelekenisis.admin.reload`

## Permissions

*   `jeditelekenisis.command.givecrystal`: Access to the `/telekinesis` command. (default: op)
*   `jeditelekenisis.command.release`: Access to the `/tkrelease` command. (default: true)
*   `jeditelekenisis.telekinesis`: Allows using basic telekinesis (grab, hold, throw). (default: true)
*   `jeditelekenisis.telekinesis.player`: Allows using telekinesis on other players. (default: op)
*   `jeditelekenisis.forcedash`: Allows using Force Dash. (default: true)
*   `jeditelekenisis.forcepush.targeted`: Allows using Targeted Force Push. (default: true)
*   `jeditelekenisis.forcepush.aoe`: Allows using Circular Force Push. (default: true)
*   `jeditelekenisis.admin.reload`: (For the planned reload command) Access to reload the configuration. (default: op)

## Configuration

The plugin is fully configurable. The main configuration files are located in the `plugins/JediTelekinesis/` folder:

*   **`config.yml`**: The main configuration file. Allows you to configure:
    *   Plugin language (`language: "ru"` or `"en"`).
    *   Parameters for each ability (range, strength, cooldowns).
    *   List of entities banned from telekinesis.
    *   Sound and visual effects (particles).
    *   And much more! Detailed comments inside the file will help you understand each option.
*   **`lang_en.yml` / `lang_ru.yml`**: Localization files. Contain all plugin messages. You can edit them or add new languages by creating a `lang_xx.yml` file (where `xx` is the language code) and specifying it in the `language` option of the `config.yml` file.

## Future Plans / Ideas for Improvement

*   Command to reload the configuration (`/jeditele reload`).
*   More Force abilities (e.g., Force Choke, Force Lightning, projectile deflection).
*   More complex visual effects.
*   Integration with other plugins (e.g., mana/energy systems).
*   Option to configure ability "costs" (e.g., experience, items).

## Support and Contribution

If you find a bug or have a suggestion for improvement, please create an [Issue](https://github.com/foxdevtime/JediTelekinesisPlugin/issues) on GitHub.

Pull requests are welcome!

---

**May the Force be with you! ;)**

<a id="russian-documentation"></a>
# JediTelekinesis Plugin

**Почувствуй Силу!** Этот плагин для Spigot/Paper добавляет в Minecraft способности настоящего джедая, вдохновленные вселенной Звездных Войн. Управляйте сущностями на расстоянии, совершайте мощные рывки и отталкивайте врагов с помощью Силы!

## Особенности

*   **Телекинез:**
    *   Захватывайте мобов и других игроков.
    *   Удерживайте их перед собой.
    *   Аккуратно отпускайте или мощно бросайте цели.
    *   Проверка на препятствия при перемещении удерживаемой цели.
    *   Настраиваемый лимит на время удержания игроков.
*   **Рывок Силы:** Быстро перемещайтесь в указанном направлении.
*   **Толчок Силы (Направленный):** Отталкивайте одну цель.
*   **Толчок Силы (Круговой/AoE):** Отталкивайте всех враждебных сущностей вокруг себя.
*   **Кристалл Силы:** Специальный предмет для концентрации способностей.
*   **Настраиваемые кулдауны:** Для каждой способности можно настроить время перезарядки.
*   **Гибкая конфигурация:** Настраивайте дальность, силу, исключённых мобов, эффекты (звуки, частицы) и многое другое в `config.yml`.
*   **Локализация:** Поддержка нескольких языков: английский и русский (см. файлы `lang_xx.yml`).
*   **Permissions:** Детальная настройка прав доступа ко всем командам и способностям.

## Поддерживаемые версии

*   **Minecraft:** 1.19.x (протестировано на 1.19.4, 1.20.1 и 1.21.4)
*   **Ядра сервера:** Spigot и его форки(Paper и т.п.)

## Установка

1.  Скачайте последнюю версию плагина `.jar` файла со страницы [Релизов](https://github.com/foxdevtime/JediTelekinesisPlugin/releases).
2.  Поместите `.jar` файл в папку `plugins` вашего сервера.
3.  Перезапустите сервер.
4.  Плагин создаст папку `JediTelekinesis` с файлами конфигурации (`config.yml`, `lang_en.yml`, `lang_ru.yml`).

## Как использовать

1.  **Получение Кристалла Силы:**
    *   Игроки с правом `jeditelekenisis.command.givecrystal` (по умолчанию операторы) могут использовать команду `/telekinesis` (или `/tk`) для получения Кристалла Силы.
2.  **Использование Кристалла Силы:**
    *   Возьмите Кристалл Силы в основную руку.
    *   **Свободные руки (цель не удерживается):**
        *   **ПКМ (Правая кнопка мыши):** Телекинез (Захват цели).
        *   **ЛКМ (Левая кнопка мыши):** Рывок Силы.
        *   **Shift + ПКМ:** Направленный Толчок Силы.
        *   **Shift + ЛКМ:** Круговой Толчок Силы.
    *   **Удержание цели:**
        *   **ПКМ:** Аккуратно отпустить цель.
        *   **Shift + ПКМ:** Бросить удерживаемую цель.
3.  **Освобождение от телекинеза (если вас удерживают):**
    *   Игроки с правом `jeditelekenisis.command.release` могут использовать команду `/tkrelease`, чтобы попытаться освободиться.

## Команды

*   `/telekinesis` (или `/tk`)
    *   Описание: Выдает Кристалл Силы.
    *   Permission: `jeditelekenisis.command.givecrystal`
*   `/tkrelease`
    *   Описание: Позволяет игроку попытаться освободиться, если его удерживают телекинезом.
    *   Permission: `jeditelekenisis.command.release`
*   `/jeditele [reload/rl]` (Планируемая команда)
    *   Описание: Перезагружает конфигурацию плагина.
    *   Permission: `jeditelekenisis.admin.reload`

## Permissions

*   `jeditelekenisis.command.givecrystal`: Доступ к команде `/telekinesis`. (default: op)
*   `jeditelekenisis.command.release`: Доступ к команде `/tkrelease`. (default: true)
*   `jeditelekenisis.telekinesis`: Позволяет использовать базовый телекинез (захват, удержание, бросок). (default: true)
*   `jeditelekenisis.telekinesis.player`: Позволяет использовать телекинез на других игроках. (default: op)
*   `jeditelekenisis.forcedash`: Позволяет использовать Рывок Силы. (default: true)
*   `jeditelekenisis.forcepush.targeted`: Позволяет использовать Направленный Толчок Силы. (default: true)
*   `jeditelekenisis.forcepush.aoe`: Позволяет использовать Круговой Толчок Силы. (default: true)
*   `jeditelekenisis.admin.reload`: (Для планируемой команды перезагрузки) Доступ к перезагрузке конфигурации. (default: op)

## Конфигурация

Плагин полностью настраиваемый. Основные файлы конфигурации находятся в папке `plugins/JediTelekinesis/`:

*   **`config.yml`**: Основной файл конфигурации. Позволяет настроить:
    *   Язык плагина (`language: "ru"` или `"en"`).
    *   Параметры каждой способности (дальность, сила, кулдауны).
    *   Список запрещенных для телекинеза сущностей.
    *   Звуковые и визуальные эффекты (частицы).
    *   И многое другое! Подробные комментарии внутри файла помогут вам разобраться в каждой опции.
*   **`lang_en.yml` / `lang_ru.yml`**: Файлы локализации. Содержат все сообщения плагина. Вы можете редактировать их или добавлять новые языки, создав файл `lang_xx.yml` (где `xx` - код языка) и указав его в опции `language` файла `config.yml`.

## Поддержка и Содействие

Если вы обнаружили ошибку или у вас есть предложение по улучшению, пожалуйста, создайте [Issue](https://github.com/foxdevtime/JediTelekinesisPlugin/issues) на GitHub.

Пулл-реквесты приветствуются!

---

**Да пребудет с вами Сила! ;)**
