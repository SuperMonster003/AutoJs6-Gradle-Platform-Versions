<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>Un plugin de Gradle Settings que decide automáticamente las versiones de AGP y del plugin de Kotlin para el ecosistema de AutoJs6</p>

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

### Idiomas (Languages)

******

README.md está disponible actualmente en los siguientes idiomas:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-fr.md)
- Español [es] # actual
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ar.md)

******

### Introducción

******

Este plugin extrae la lógica de decisión de versiones de compilación que antes se mantenía por duplicado en el proyecto principal de AutoJs6 y en cada uno de sus proyectos de plugins. Cada repositorio arrastraba en su settings.gradle.kts varios cientos de líneas casi idénticas, dedicadas a averiguar qué IDE estaba ejecutando la compilación y a elegir en consecuencia las versiones de AGP y de Kotlin.

Convertido en un plugin de Settings publicable, a los proyectos que lo consumen les bastan poco más de diez líneas para adoptarlo. Cada mejora de la lógica llega a todos los proyectos con solo subir la versión del plugin, sin tener que copiar y pegar el mismo código repositorio por repositorio.

******

### Funciones

******

- Detección del entorno de compilación: Android Studio, IntelliJ IDEA, Temurin JDK y la línea de comandos sin IDE.
- Selección de una versión de AGP compatible con el IDE actual, tomando la entrada inmediatamente inferior cuando no hay coincidencia exacta.
- Retorno automático a la selección auto cuando el IDE es más reciente que todas las entradas de la tabla de correspondencias, evitando una degradación silenciosa a un AGP demasiado antiguo.
- Límite superior según la compatibilidad entre AGP y Gradle, de modo que la versión elegida siempre pueda cargarla el Gradle actual.
- Decisión de la versión de KSP, con elevación automática de la versión de AGP cuando el KSP elegido exige un AGP más reciente.
- Decisión de la versión de R8, incorporando un R8 externo solo cuando el que trae AGP no es lo bastante reciente.
- Los datos de compatibilidad se distribuyen con el plugin, pero un directorio `gradle/data` en el proyecto consumidor tiene prioridad, lo que facilita las correcciones urgentes.
- Se conserva la vía de escape `OVERRIDDEN_*` de `version.properties`, para fijar versiones concretas cuando se necesita una compilación determinista.
- README y CHANGELOG están disponibles en español/francés/ruso/árabe/japonés/coreano/inglés/chino simplificado/chino tradicional de Hong Kong/chino tradicional de Taiwán.

******

### Uso

******

Aplica el plugin en el archivo `settings.gradle.kts` del proyecto consumidor, antes de `includeBuild`:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("org.autojs.build.platform-versions") version "1.3.0"
    }
}

plugins {
    id("org.autojs.build.platform-versions")
}
```

Los scripts de módulo pueden declarar después los plugins mediante el plugins DSL, tomando las versiones del resultado de la decisión:

```kotlin
plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version")
    id("org.jetbrains.kotlin.android") version System.getProperty("gradle.kotlin.version")
}
```

El resultado de la decisión también puede leerse como objeto mediante `gradle.extra["platformVersions"]`.

******

### Proceso de decisión

******

La versión de AGP se decide en tres pasos:

- Buscar la versión de la plataforma actual en la tabla de correspondencias de AGP de esa plataforma, tomando la entrada inmediatamente inferior.
- Comprobar si la tabla está desactualizada, es decir, si el IDE actual es más reciente que todas sus entradas; en ese caso se vuelve a la selección auto.
- Limitar el resultado con la tabla de compatibilidad entre AGP y Gradle, de forma que nunca supere lo que el Gradle actual puede cargar.

La versión de Kotlin, en cambio, sigue a Gradle y no al IDE: siempre se toma la más reciente que admite el Gradle actual.

******

### Versiones fijadas

******

Si quieres omitir por completo la decisión automática, indica las versiones directamente en `version.properties`:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

El valor `NONE` o un valor vacío significa que no se fija nada y se aplica el proceso de decisión automática.

******

### Datos de compatibilidad

******

La decisión se basa en los siguientes archivos de datos, que se distribuyen junto con el plugin:

```text
gradle/data/agp-releases.list
gradle/data/agp-gradle-compat.properties
gradle/data/gradle-kotlin-compat.properties
gradle/data/java-gradle-compat.properties
gradle/data/android-studio-agp-compat.properties
```

Si el proyecto consumidor coloca un archivo con el mismo nombre en su propio directorio `gradle/data`, ese archivo tiene prioridad.

******

### Historial de versiones

******

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

##### Para consultar un historial más completo, véase

* [CHANGELOG-es.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-es.md)

******

### Compilación

******

```powershell
.\gradlew.bat build
```

Publicar en el repositorio Maven local:

```powershell
.\gradlew.bat publishToMavenLocal
```

El número de versión del plugin se toma de `VERSION_NAME` en `version.properties`.

******

### Estructura de recursos

******

```text
src/main/kotlin/org/autojs/build/platform/
src/main/resources/org/autojs/build/platform/data/
sample/
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
```

La lógica de decisión se encuentra en `src/main/kotlin` y los datos de compatibilidad se empaquetan como recursos en `src/main/resources`; `sample` es un proyecto consumidor mínimo que sirve para verificar el resultado de las decisiones. README y CHANGELOG se generan a partir de las fuentes JSON mediante `.python/generate_markdown.py`.

******

### Enlaces

******

- Proyecto principal de AutoJs6: https://github.com/SuperMonster003/AutoJs6
- Notas de versión del plugin de Android Gradle: https://developer.android.com/build/releases/gradle-plugin
- Matriz de compatibilidad de Gradle: https://docs.gradle.org/current/userguide/compatibility.html
