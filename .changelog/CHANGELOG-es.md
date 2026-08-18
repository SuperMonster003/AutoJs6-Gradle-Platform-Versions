******

### Idiomas (Languages)

******

CHANGELOG.md está disponible actualmente en los siguientes idiomas:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-fr.md)
- Español [es] # actual
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ar.md)

******

### Historial de versiones

******

# v1.1.0

###### 2026/08/18

* `Función` Decisión de la versión de R8, consultada según la versión de Kotlin actual, incorporando de forma explícita un R8 externo solo cuando el que trae AGP no es lo bastante reciente
* `Función` Decisión de la versión de KSP, cuyo número sigue a la versión de Kotlin de destino; la versión de AGP se eleva automáticamente cuando el KSP elegido exige un AGP más reciente
* `Función` El resultado de la decisión es accesible ahora mediante el punto de entrada `PlatformVersionsFacade`, utilizable directamente en el cuerpo del settings script
* `Función` El resultado de la decisión se publica además como propiedades del sistema, para que los scripts de módulo declaren las versiones de los plugins mediante el plugins DSL
* `Función` Script de migración por lotes `.python/migrate_downstream.py` para los repositorios posteriores, con soporte para vista previa, aplicación y reversión, conservando una copia de seguridad por repositorio
* `Corrección` A `getMaxSupportedJavaVersion` se le pasaba antes la versión de AGP, lo que rebajaba el límite del toolchain; ahora se le pasa la versión de Gradle
* `Mejora` Se elimina la entrada 2026.2.1 de la tabla de correspondencias de IntelliJ IDEA, de modo que tanto 2026.2 como 2026.2.1 obtienen AGP 9.0.1, en consonancia con lo que el IDE admite realmente

# v1.0.0

###### 2026/08/18

* `Función` Plugin de Gradle Settings `org.autojs.build.platform-versions`, que decide automáticamente las versiones de AGP y del plugin de Gradle para Kotlin
* `Función` Detección del entorno de compilación, compatible con Android Studio/IntelliJ IDEA/Temurin JDK y con la línea de comandos sin IDE
* `Función` Decisión de la versión de AGP, buscando la versión del IDE actual en la tabla de correspondencias y tomando la entrada inmediatamente inferior
* `Función` Respaldo ante una tabla de correspondencias desactualizada: cuando el IDE actual es más reciente que todas las entradas de la tabla se pasa a la selección auto, sin degradar en silencio a un AGP demasiado antiguo
* `Función` Versión de AGP limitada por la tabla de compatibilidad de Gradle, de modo que la versión elegida siempre pueda cargarla el Gradle actual
* `Función` Decisión de la versión del plugin de Gradle para Kotlin, siguiendo la versión más reciente que admite el Gradle actual
* `Función` Datos de compatibilidad distribuidos con el plugin, pudiendo el directorio `gradle/data` del proyecto consumidor sustituir cualquier archivo de datos con el mismo nombre
* `Función` Vía de escape `OVERRIDDEN_*` en `version.properties`, que permite fijar versiones directamente y omitir la decisión automática
* `Función` Resultado de la decisión expuesto a través de `PlatformVersionsExtension`, listo para las declaraciones de classpath del buildscript
* `Función` Proyecto consumidor mínimo `sample`, para verificar el resultado de las decisiones en tres escenarios típicos
* `Función` Recursos multilingües para README y CHANGELOG: español/francés/ruso/árabe/japonés/coreano/inglés/chino simplificado/chino tradicional de Hong Kong/chino tradicional de Taiwán
