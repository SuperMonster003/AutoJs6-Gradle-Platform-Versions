******

### Языки (Languages)

******

В настоящее время CHANGELOG.md поддерживает следующие языки:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ko.md)
- Русский [ru] # текущий
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ar.md)

******

### История выпусков

******

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
