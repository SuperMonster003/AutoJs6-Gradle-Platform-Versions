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

# v1.4.1

###### 2026/08/18

* `Mejora` Ya no se imprime en la consola ninguna nota explicativa cuando la tabla de correspondencias se ha quedado atrás. El sufijo [auto-specified] al final de la línea de versión ya indica cómo se llegó a ella, y la nota resultaba más larga que el resumen que explicaba
* `Mejora` Con ello se elimina la API de notas: PlatformVersionsExtension.notes y el parámetro notes de Formatted dejan de existir, de modo que un script consumidor que leyera esa propiedad debe ajustarse

# v1.4.0

###### 2026/08/18

* `Corrección` El límite de AGP ya no se relaja cuando la versión del IDE es solo una actualización de nivel de parche. Antes IntelliJ IDEA 2026.2.1 obtenía AGP 9.2.1 y el IDE lo rechazaba; ahora se queda en la línea 9.1, igual que 2026.2
* `Mejora` Se incorpora una entrada 2026.2 a la tabla de correspondencias de IntelliJ IDEA, cuyo límite de AGP se toma de lo que informa el propio IDE
* `Mejora` La migración pasa a declarar la versión del plugin una sola vez en el script de compilación raíz, sin tocar los scripts de módulo; añadir la versión módulo a módulo no podía funcionar con los módulos de Groovy, cuyo bloque plugins solo admite literales de cadena
* `Mejora` Las notas mostradas en la consola se trasladan a un párrafo aparte, debajo del resumen de versiones, en lugar de intercalarse con las líneas de versiones

# v1.3.0

###### 2026/08/18

* `Aviso` Gradle 8 deja de ser compatible. AGP 9.0 es la primera versión que exige Gradle 9, por lo que el rango admitido empieza en AGP 9.0
* `Mejora` Se eliminan de las tablas de compatibilidad las entradas anteriores a la 9, y la tabla de correspondencias de IntelliJ IDEA conserva solo las entradas que dan AGP 9
* `Mejora` Cuando el Gradle actual es anterior a todas las entradas de compatibilidad, ya no se recurre a la entrada más baja, sino que se informa de un error explícito, para que nunca llegue al classpath una versión que no se puede cargar
* `Mejora` Versiones mínimas admitidas elevadas: Gradle 9.1.0, Android Studio 2025.2.3, IntelliJ IDEA 2026.1.2, AGP 9.0
* `Mejora` Insignias del README ajustadas a las versiones anteriores, con la incorporación de una insignia de AGP
* `Mejora` El script de migración admite el azúcar sintáctico kotlin(...) y los nombres cortos antiguos como kotlin-android/kotlin-kapt/kotlin-parcelize, que se expanden al id completo del plugin
* `Mejora` El script de migración omite dos clases de repositorio que no se pueden migrar: aquellos cuyos fragmentos de script incorporados mediante apply(from=) hacen referencia a tipos de AGP, y aquellos que tienen activada la verificación de dependencias

# v1.2.0

###### 2026/08/18

* `Función` Script de conversión de los scripts de módulo `.python/migrate_modules.py`, que reescribe las aplicaciones de plugins sin versión a la forma con versión, tomando la versión de una propiedad del sistema
* `Función` La versión de KSP decidida se publica ahora también como la propiedad del sistema `gradle.ksp.version`, en consonancia con la nomenclatura usada para AGP y Kotlin
* `Corrección` La reversión del script de conversión de módulos no restauraba los archivos originales y dejaba atrás las copias de seguridad
* `Mejora` El script de migración de settings comprueba primero si los scripts de módulo están preparados y, cuando no lo están, avisa en lugar de reescribir, para no dejar un estado intermedio que no se pueda compilar
* `Mejora` El script de migración de settings pasa a integrar el plugin en el bloque plugins existente y lo mueve delante de `includeBuild`, en vez de añadir un bloque nuevo

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
