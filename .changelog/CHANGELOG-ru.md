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
