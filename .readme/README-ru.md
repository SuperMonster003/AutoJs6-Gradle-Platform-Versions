<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>Gradle Settings-плагин, автоматически определяющий версии AGP и плагина Kotlin для экосистемы AutoJs6</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Gradle-Platform-Versions?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=A24232&label=Issues"/></a>
    <br>
    <a href="https://gradle.org/releases/"><img alt="Gradle" src="https://img.shields.io/badge/Gradle-8.2+-02303A"/></a>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2023.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2023.3+-EE4677"/></a>
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
        id("org.autojs.build.platform-versions") version "1.2.0"
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

# v1.2.0

###### 2026/08/18

* `Функция` Скрипт преобразования скриптов модулей `.python/migrate_modules.py`, переписывающий применение плагинов без версии в форму с версией, где версия берётся из system property
* `Функция` Выбранная версия KSP публикуется теперь и как system property `gradle.ksp.version`, в соответствии с именованием для AGP и Kotlin
* `Исправление` Откат в скрипте преобразования модулей не восстанавливал исходные файлы и оставлял после себя резервные копии
* `Улучшение` Скрипт миграции settings сначала проверяет готовность скриптов модулей и при их неготовности выводит предупреждение, не выполняя перезапись, чтобы не оставить несобираемое промежуточное состояние
* `Улучшение` Скрипт миграции settings теперь добавляет плагин в существующий plugins block и переносит его перед `includeBuild`, вместо создания нового блока

# v1.1.0

###### 2026/08/18

* `Функция` Выбор версии R8 по таблице на основании текущей версии Kotlin: внешний R8 подключается явно только тогда, когда встроенного в AGP R8 недостаточно
* `Функция` Выбор версии KSP, номер которой следует за целевой версией Kotlin; если выбранная версия KSP требует более высокого AGP, версия AGP автоматически повышается
* `Функция` Новая точка входа `PlatformVersionsFacade` для результата выбора, доступная прямо в теле settings script
* `Функция` Результат выбора публикуется также в виде system property, чтобы скрипты модулей могли объявлять версии плагинов через plugins DSL
* `Функция` Скрипт массовой миграции репозиториев-потребителей `.python/migrate_downstream.py` с поддержкой предпросмотра, применения и отката, сохраняющий резервную копию для каждого репозитория
* `Исправление` В `getMaxSupportedJavaVersion` ранее ошибочно передавалась версия AGP, из-за чего верхняя граница toolchain занижалась; теперь передаётся версия Gradle
* `Улучшение` Удалена запись 2026.2.1 из таблицы соответствий IntelliJ IDEA, так что и 2026.2, и 2026.2.1 получают AGP 9.0.1, что соответствует реальному диапазону поддержки IDE

# v1.0.0

###### 2026/08/18

* `Функция` Gradle Settings-плагин `org.autojs.build.platform-versions` для автоматического определения версий AGP и Kotlin Gradle Plugin
* `Функция` Определение среды сборки с поддержкой Android Studio/IntelliJ IDEA/Temurin JDK, а также чистой командной строки
* `Функция` Выбор версии AGP: подбор ближайшей записи вниз в таблице соответствий по текущей версии IDE
* `Функция` Возврат к автоматическому выбору при отставании таблицы соответствий: если текущая IDE новее всех её записей, используется выбор auto, без молчаливого отката к слишком старому AGP
* `Функция` Ограничение версии AGP сверху по таблице совместимости Gradle, гарантирующее, что выбранную версию текущий Gradle точно сможет загрузить
* `Функция` Выбор версии Kotlin Gradle Plugin вслед за самой свежей версией, поддерживаемой текущим Gradle
* `Функция` Поставка данных о совместимости вместе с плагином; каталог `gradle/data` в проекте-потребителе может переопределить одноимённые файлы данных
* `Функция` Запасной выход `OVERRIDDEN_*` в `version.properties`: версию можно задать напрямую, пропустив автоматический выбор
* `Функция` Результат выбора доступен через `PlatformVersionsExtension` и может использоваться при объявлении classpath для buildscript
* `Функция` Минимальный проект-потребитель `sample` для проверки результатов выбора в трёх типовых сценариях
* `Функция` Многоязычные ресурсы для README и CHANGELOG: испанский, французский, русский, арабский, японский, корейский, английский, китайский упрощённый, гонконгский и тайваньский традиционный

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
