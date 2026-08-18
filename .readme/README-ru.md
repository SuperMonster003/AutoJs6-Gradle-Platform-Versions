<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>Gradle Settings-плагин, автоматически определяющий версии AGP и плагина Kotlin для экосистемы AutoJs6</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Gradle-Platform-Versions?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=A24232&label=Issues"/></a>
    <br>
    <a href="https://gradle.org/releases/"><img alt="Gradle" src="https://img.shields.io/badge/Gradle-9.1.0+-02303A"/></a>
    <a href="https://developer.android.com/build/releases/gradle-plugin"><img alt="AGP" src="https://img.shields.io/badge/AGP-9.0+-3DDC84"/></a>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2025.2.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2026.1.2+-EE4677"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Языки (Languages)

******

В настоящее время README.md поддерживает следующие языки:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ko.md)
- Русский [ru] # текущий
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ar.md)

******

### Введение

******

Этот плагин выносит наружу ту самую логику выбора версий сборки, которую приходилось поддерживать одновременно в основном проекте AutoJs6 и во всех проектах-плагинах. Раньше в каждом репозитории файл settings.gradle.kts содержал несколько сотен строк практически одинакового кода: он определял, какая IDE выполняет сборку, и на основании этого подбирал подходящие версии AGP и Kotlin.

После оформления всего этого в виде публикуемого Settings-плагина проектам-потребителям достаточно десятка строк подключения. Логику улучшают один раз, а все проекты получают улучшение простым повышением версии плагина, без копирования изменений по репозиториям.

******

### Возможности

******

- Определение среды сборки: Android Studio, IntelliJ IDEA, Temurin JDK, а также чистая командная строка.
- Подбор версии AGP, которую способна поддержать текущая версия IDE; при отсутствии точного соответствия выбирается ближайшая версия вниз.
- Если версия IDE новее всех записей таблицы соответствий, происходит автоматический возврат к выбору auto, что предотвращает молчаливый откат к слишком старому AGP.
- Ограничение сверху по таблице совместимости AGP и Gradle, гарантирующее, что выбранную версию текущий Gradle точно сможет загрузить.
- Определение версии KSP с автоматическим повышением версии AGP, если выбранная версия KSP требует более высокого AGP.
- Определение версии R8: внешний R8 подключается только тогда, когда встроенного в AGP R8 недостаточно.
- Данные о совместимости поставляются вместе с плагином; если в проекте-потребителе есть каталог `gradle/data`, приоритет отдаётся ему, что удобно для срочной правки данных.
- Сохранён запасной выход `OVERRIDDEN_*` в `version.properties`: когда нужна детерминированная сборка, версию можно зафиксировать напрямую.
- README и CHANGELOG доступны на испанском, французском, русском, арабском, японском, корейском, английском, китайском упрощённом, гонконгском и тайваньском традиционном.

******

### Использование

******

Примените плагин в файле `settings.gradle.kts` проекта-потребителя, до вызова `includeBuild`:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("org.autojs.build.platform-versions") version "1.4.0"
    }
}

plugins {
    id("org.autojs.build.platform-versions")
}
```

После этого скрипты модулей могут объявлять плагины через plugins DSL, а версии берутся из результата выбора:

```kotlin
plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version")
    id("org.jetbrains.kotlin.android") version System.getProperty("gradle.kotlin.version")
}
```

Результат выбора можно также прочитать как объект через `gradle.extra["platformVersions"]`.

******

### Процесс выбора

******

Версия AGP определяется в три шага:

- По текущей версии платформы выполняется подбор ближайшей записи вниз в таблице соответствий AGP для этой платформы.
- Проверяется, не отстала ли таблица соответствий, то есть новее ли текущая IDE всех её записей; если да, происходит возврат к выбору auto.
- Применяется ограничение сверху по таблице совместимости AGP и Gradle, поэтому результат не выходит за пределы того, что текущий Gradle способен загрузить.

Версия Kotlin следует за Gradle, а не за IDE, и всегда выбирается самая свежая из поддерживаемых текущим Gradle.

******

### Фиксация версии

******

Если требуется полностью пропустить автоматический выбор, версию можно задать напрямую в `version.properties`:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

Значение `NONE` или пустое значение означает, что версия не задана и используется автоматический выбор.

******

### Данные о совместимости

******

Ниже перечислены файлы данных, на которых основан выбор; они поставляются вместе с плагином:

```text
gradle/data/agp-releases.list
gradle/data/agp-gradle-compat.properties
gradle/data/gradle-kotlin-compat.properties
gradle/data/java-gradle-compat.properties
gradle/data/android-studio-agp-compat.properties
```

Если проект-потребитель положит файл с тем же именем в свой каталог `gradle/data`, приоритет получит этот файл.

******

### История выпусков

******

# v1.4.0

###### 2026/08/18

* `Исправление` Верхняя граница AGP больше не повышается, если версия IDE отличается лишь на уровне патча. Ранее для IntelliJ IDEA 2026.2.1 выбирался AGP 9.2.1, который IDE отвергала; теперь версия остаётся на линии 9.1, как и для 2026.2
* `Улучшение` В таблицу соответствий IntelliJ IDEA добавлена запись 2026.2, верхняя граница AGP в которой взята из того, что сообщает сама IDE
* `Улучшение` Схема миграции изменена: версия плагина объявляется один раз в корневом build script, а скрипты модулей остаются нетронутыми; прежний способ с добавлением версии в каждый модуль был неприменим к модулям на Groovy, поскольку их plugins block принимает только строковые литералы
* `Улучшение` Примечания в консоли вынесены в отдельный абзац под сводкой версий и больше не чередуются со строками версий

# v1.3.0

###### 2026/08/18

* `Подсказка` Начиная с этой версии Gradle 8 не поддерживается. AGP 9.0 — первая версия, требующая Gradle 9, поэтому поддерживаемый диапазон начинается с AGP 9.0
* `Улучшение` Из таблиц совместимости удалены записи старше 9, а в таблице соответствий IntelliJ IDEA оставлены только записи, дающие AGP 9
* `Улучшение` Если текущий Gradle старше всех записей о совместимости, откат к самой нижней записи больше не выполняется: выдаётся явная ошибка, чтобы версия, которую невозможно загрузить, никогда не попадала в classpath
* `Улучшение` Повышены минимальные поддерживаемые версии: Gradle 9.1.0, Android Studio 2025.2.3, IntelliJ IDEA 2026.1.2, AGP 9.0
* `Улучшение` Значки в README приведены в соответствие с указанными версиями, добавлен значок AGP
* `Улучшение` Скрипт миграции поддерживает синтаксический сахар kotlin(...) и старые короткие имена вроде kotlin-android/kotlin-kapt/kotlin-parcelize, причём короткие имена разворачиваются в полный id плагина
* `Улучшение` Скрипт миграции пропускает два вида репозиториев, которые невозможно мигрировать: те, где подключаемые через apply(from=) фрагменты скриптов ссылаются на типы AGP, и те, где включена проверка зависимостей

# v1.2.0

###### 2026/08/18

* `Функция` Скрипт преобразования скриптов модулей `.python/migrate_modules.py`, переписывающий применение плагинов без версии в форму с версией, где версия берётся из system property
* `Функция` Выбранная версия KSP публикуется теперь и как system property `gradle.ksp.version`, в соответствии с именованием для AGP и Kotlin
* `Исправление` Откат в скрипте преобразования модулей не восстанавливал исходные файлы и оставлял после себя резервные копии
* `Улучшение` Скрипт миграции settings сначала проверяет готовность скриптов модулей и при их неготовности выводит предупреждение, не выполняя перезапись, чтобы не оставить несобираемое промежуточное состояние
* `Улучшение` Скрипт миграции settings теперь добавляет плагин в существующий plugins block и переносит его перед `includeBuild`, вместо создания нового блока

##### Подробную историю выпусков смотрите здесь

* [CHANGELOG-ru.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ru.md)

******

### Сборка

******

```powershell
.\gradlew.bat build
```

Публикация в локальный репозиторий Maven:

```powershell
.\gradlew.bat publishToMavenLocal
```

Номер версии плагина берётся из `VERSION_NAME` в `version.properties`.

******

### Структура ресурсов

******

```text
src/main/kotlin/org/autojs/build/platform/
src/main/resources/org/autojs/build/platform/data/
sample/
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
```

Логика выбора расположена в `src/main/kotlin`, данные о совместимости упакованы как ресурсы в `src/main/resources`; `sample` — минимальный проект-потребитель для проверки результатов выбора. README и CHANGELOG генерируются скриптом `.python/generate_markdown.py` из исходных файлов JSON.

******

### Полезные ссылки

******

- Основной проект AutoJs6: https://github.com/SuperMonster003/AutoJs6
- Примечания к выпускам Android Gradle Plugin: https://developer.android.com/build/releases/gradle-plugin
- Матрица совместимости Gradle: https://docs.gradle.org/current/userguide/compatibility.html
