<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>Un plugin de Gradle Settings que decide automáticamente las versiones de AGP y del plugin de Kotlin para el ecosistema de AutoJs6</p>

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
- Los datos de compatibilidad se distribuyen con el plugin, pero un directorio `gradle/data` en el proyecto consumidor tiene prioridad, lo que facilita las correcciones urgentes.
- Se conserva la vía de escape `OVERRIDDEN_*` de `version.properties`, para fijar versiones concretas cuando se necesita una compilación determinista.
- README y CHANGELOG están disponibles en español/francés/ruso/árabe/japonés/coreano/inglés/chino simplificado/chino tradicional de Hong Kong/chino tradicional de Taiwán.

******

### Uso

******

Aplica el plugin en el archivo `settings.gradle.kts` del proyecto consumidor:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("org.autojs.build.platform-versions") version "1.0.0"
    }
}
```

A partir de ahí, el resultado de la decisión puede usarse en el classpath del buildscript:

```kotlin
import org.autojs.build.platform.PlatformVersionsExtension

val platformVersions = gradle.extra["platformVersions"] as PlatformVersionsExtension

buildscript {
    dependencies {
        classpath(platformVersions.agpClasspathNotation)
        classpath(platformVersions.kotlinClasspathNotation)
    }
}
```

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
