<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>Gradle Settings-плагин, автоматически определяющий версии AGP и плагина Kotlin для экосистемы AutoJs6</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Gradle-Platform-Versions?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Gradle-Platform-Versions?color=534BAE&label=License"/></a>
    <br>
    <a href="https://gradle.org/releases/"><img alt="Gradle" src="https://img.shields.io/badge/Gradle-9.1.0+-02303A"/></a>
    <a href="https://developer.android.com/build/releases/gradle-plugin"><img alt="AGP" src="https://img.shields.io/badge/AGP-9.0+-335544"/></a>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2025.2.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2026.1.2+-EE4677"/></a>
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
- Явная обработка Temurin и чистой командной строки как сред без IDE: AGP выбирается по совместимости с Gradle, а не по таблице версий IDE.
- Пересечение верхней границы IDE/Gradle с минимальными требованиями к AGP от уровня Android API, KSP и проекта; при отсутствии совместимой версии ошибка выдаётся заранее.
- Определение версии R8: внешний R8 подключается только тогда, когда встроенного в AGP R8 недостаточно.
- Поставляет данные о совместимости вместе с плагином как единственный источник по умолчанию; официальные проекты хоста и плагинов AutoJs6 не поддерживают копии `gradle/data` на стороне потребителя.
- Сохранён запасной выход `OVERRIDDEN_*` в `version.properties`: когда нужна детерминированная сборка, версию можно зафиксировать напрямую.
- README и CHANGELOG доступны на испанском, французском, русском, арабском, японском, корейском, английском, китайском упрощённом, гонконгском и тайваньском традиционном.

******

### Использование

******

Примените плагин в файле `settings.gradle.kts` проекта-потребителя, до вызова `includeBuild`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("io.github.supermonster003.autojs6-platform-versions") version "1.7.1"
    }
}

plugins {
    id("io.github.supermonster003.autojs6-platform-versions")
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

- В IDE самый старый ключ таблицы платформы задаёт центральную нижнюю границу поддержки, а найденный AGP — верхнюю; минимум IDE у потребителя может только ужесточить эту границу. Для более новых IDE сохраняется откат при устаревании таблицы, а для Temurin и чистой командной строки сразу используется граница совместимости Gradle.
- Эта граница дополнительно ограничивается официальной таблицей совместимости AGP/Gradle, чтобы запущенный Gradle мог загрузить кандидата.
- Нижняя граница выводится из compileSdk/targetSdk, KSP и необязательного минимума проекта; AGP возвращается только при пересечении границ.

Версия Kotlin следует за Gradle, а не за IDE, и всегда выбирается самая свежая из поддерживаемых текущим Gradle.

******

### Фиксация версии

******

Для тестирования или детерминированной сборки точную версию можно зафиксировать напрямую в `version.properties`:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

Значение `NONE` или пустое значение означает отсутствие фиксации. Используйте `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION` только для реальной нижней границы конкретного проекта, которую центральный механизм не может вывести; официальные потребители AutoJs6 не должны повторять с его помощью общую границу AGP 9, уже гарантированную платформой. Числовые значения `COMPILE_SDK_VERSION` и `TARGET_SDK_VERSION` учитываются автоматически. Аналогично, `MIN_SUPPORTED_ANDROID_STUDIO_IDE_VERSION` и `MIN_SUPPORTED_INTELLIJ_IDEA_IDE_VERSION` являются необязательными ограничениями конкретного проекта: самый старый ключ каждой центральной таблицы IDE задаёт базу, которую нельзя понизить, поэтому не указывайте эти свойства, если проекту действительно не нужна более новая IDE.

******

### Данные о совместимости

******

Ниже перечислены файлы данных, на которых основан выбор; они поставляются вместе с плагином:

```text
src/main/resources/org/autojs/build/platform/data/
  agp-releases.list
  agp-gradle-compat.properties
  android-api-agp-compat.properties
  gradle-kotlin-compat.properties
  java-gradle-compat.properties
  android-studio-agp-compat.properties
  android-studio-build-version.properties
  android-studio-codename-version.properties
  android-studio-codename.properties
  kotlin-r8-compat.properties
  ksp-agp-compat.properties
  ksp-releases.properties
```

Одноимённый файл в каталоге `gradle/data` потребителя по-прежнему имеет приоритет только для обратной совместимости или временной диагностики; это не официальный режим работы. Официальные проекты хоста и плагинов AutoJs6 не должны фиксировать такие переопределения. Данные о совместимости следует обновлять в этом центральном репозитории и публиковать с новой неизменяемой версией плагина.

******

### Обновление данных

******

Разработчики могут обновить все данные совместимости, запустив интерактивный пакетный файл из корня репозитория:

```bat
run-scrapers.bat
```

Периодический workflow репозитория использует эту проверку без изменения файлов; ручной режим update после валидации открывает pull request с данными:

```bash
npm --prefix .utils ci
npm --prefix .utils test
npm --prefix .utils run check-data
```

`check-data` не изменяет рабочую область: код выхода `0` означает актуальные данные, `2` — найденные обновления, а `1` — сбой задачи.

Полный охват обновления и правила запуска описаны в [.utils/README.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.utils/README.md).

******

### История выпусков

******

# v1.7.1

###### 2026/09/02

* `Исправление` Самая старая запись каждой таблицы совместимости IDE теперь принудительно задаёт центральную нижнюю границу поддержки; значения `MIN_SUPPORTED_*_IDE_VERSION` у потребителя могут только ужесточить её и больше не позволяют направить неподдерживаемую старую IDE в резервный выбор только по Gradle
* `Улучшение` Удовлетворённые минимальные ограничения AGP остаются в машиночитаемом результате, но больше не выводятся в обычной сводке успешной сборки; ошибки несовместимости теперь содержат обнаруженную версию IDE и все источники требований
* `Улучшение` Уточнено, что встроенные данные совместимости являются авторитетным источником для официальных потребителей AutoJs6; переопределения в `gradle/data` сохраняются только для совместимости со старыми версиями или временной диагностики

# v1.7.0

###### 2026/09/02

* `Подсказка` Обычным сборкам следует задавать уровни SDK и, только при необходимости, `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION`; оставьте `OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION` для намеренных тестов точной версии или исключительных обходных сценариев
* `Функция` Выбор AGP теперь пересекает нижние границы Android API, проекта и KSP с верхними границами активных Gradle и IDE и сообщает источник ограничения, если совместимой версии нет
* `Функция` Добавлены процессы GitHub Actions для сборок Temurin, плановых проверок данных совместимости или PR с обновлениями, а также защищённой тегом и подтверждением публикации в Maven Central и Gradle Plugin Portal
* `Исправление` Сборки Temurin и из чистой командной строки больше не используют прежние сопоставления JDK с AGP, поэтому JDK `21.0.6+7` не сможет незаметно выбрать AGP 8.7.3; Android API 36 теперь автоматически требует AGP 8.9.1 или новее
* `Исправление` Исправлены обход предела AGP для Gradle в двухкомпонентных сопоставлениях IDE и откат старых Gradle к версии платформы, которую они не способны загрузить
* `Улучшение` Добавлены независимо собранные официальные данные о минимальном AGP для Android API и обновлены данные Android Studio, выпусков AGP и совместимости AGP/Gradle
* `Улучшение` Проверка расширена до 70 JVM-тестов, тестов разбора и идемпотентности Node и реальной примерной сборки в CI на Temurin 17, проверяющей автоматический выбор без IDE

# v1.6.0

###### 2026/08/29

* `Подсказка` Постоянный идентификатор плагина Gradle изменён с `org.autojs.build.platform-versions` на `io.github.supermonster003.autojs6-platform-versions`, а координаты Maven заданы как `io.github.supermonster003:autojs6-gradle-platform-versions`; имя пакета Java/Kotlin остаётся `org.autojs.build.platform`
* `Функция` Первый конвейер публичного онлайн-выпуска для Maven Central и Gradle Plugin Portal с подписанными артефактами реализации, исходников, Javadoc, метаданных модуля и маркера плагина
* `Улучшение` Добавлены публичный репозиторий GitHub, полные метаданные POM для Central, воспроизводимый Portal bundle, изолированная проверка потребителем и отдельные безопасные пути подписи для локального GPG и CI
* `Улучшение` Официальный пример теперь разрешает зависимости только из публичных репозиториев без `mavenLocal()`; средство миграции также распознаёт и обновляет прежний идентификатор плагина

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
run-scrapers.bat
.utils/
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
