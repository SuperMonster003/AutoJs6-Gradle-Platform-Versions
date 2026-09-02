<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>Un plugin de Gradle Settings que decide automáticamente las versiones de AGP y del plugin de Kotlin para el ecosistema de AutoJs6</p>

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
- Tratamiento explícito de Temurin y la línea de comandos como entornos sin IDE, eligiendo AGP según la compatibilidad con Gradle y no mediante una tabla de versiones de IDE.
- Intersección del límite superior de IDE/Gradle con los requisitos mínimos de AGP derivados del nivel de API de Android, KSP y el proyecto, con error temprano si no hay una versión compatible.
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
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("io.github.supermonster003.autojs6-platform-versions") version "1.6.0"
    }
}

plugins {
    id("io.github.supermonster003.autojs6-platform-versions")
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

- Usar la tabla de la plataforma como límite superior en un IDE, incluido el retorno cuando queda desactualizada; para Temurin y la línea de comandos, usar directamente la compatibilidad con Gradle.
- Limitar de nuevo ese máximo con la tabla oficial de compatibilidad entre AGP y Gradle, para que el candidato pueda cargarlo el Gradle en ejecución.
- Derivar los límites inferiores de compileSdk/targetSdk, KSP y un mínimo opcional del proyecto, y devolver AGP solo si ambos límites se intersectan.

La versión de Kotlin, en cambio, sigue a Gradle y no al IDE: siempre se toma la más reciente que admite el Gradle actual.

******

### Versiones fijadas

******

Para pruebas o una compilación determinista, las versiones exactas pueden fijarse directamente en `version.properties`:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

El valor `NONE` o un valor vacío significa que no se fija nada. Para declarar un límite inferior sin fijar una versión exacta, usa `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION`; los valores numéricos de `COMPILE_SDK_VERSION` y `TARGET_SDK_VERSION` se tienen en cuenta automáticamente.

******

### Datos de compatibilidad

******

La decisión se basa en los siguientes archivos de datos, que se distribuyen junto con el plugin:

```text
gradle/data/agp-releases.list
gradle/data/agp-gradle-compat.properties
gradle/data/android-api-agp-compat.properties
gradle/data/gradle-kotlin-compat.properties
gradle/data/java-gradle-compat.properties
gradle/data/android-studio-agp-compat.properties
gradle/data/android-studio-build-version.properties
gradle/data/android-studio-codename-version.properties
gradle/data/android-studio-codename.properties
gradle/data/kotlin-r8-compat.properties
gradle/data/ksp-agp-compat.properties
gradle/data/ksp-releases.properties
```

Si el proyecto consumidor coloca un archivo con el mismo nombre en su propio directorio `gradle/data`, ese archivo tiene prioridad.

******

### Actualización de datos

******

Los desarrolladores pueden actualizar todos los datos de compatibilidad ejecutando el punto de entrada interactivo desde la raíz del repositorio:

```bat
run-scrapers.bat
```

El workflow programado del repositorio usa este punto de entrada de solo lectura; el modo update manual abre un pull request de datos después de validarlo:

```bash
npm --prefix .utils ci
npm --prefix .utils test
npm --prefix .utils run check-data
```

`check-data` no modifica el espacio de trabajo: el código de salida `0` indica que los datos están actualizados, `2` que hay actualizaciones y `1` que la tarea falló.

Para consultar el alcance completo y las convenciones de ejecución, consulte [.utils/README.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.utils/README.md).

******

### Historial de versiones

******

# v1.6.0

###### 2026/08/29

* `Aviso` El ID permanente del plugin de Gradle ahora es `io.github.supermonster003.autojs6-platform-versions` en lugar de `org.autojs.build.platform-versions`, y las coordenadas de Maven son `io.github.supermonster003:autojs6-gradle-platform-versions`; el paquete Java/Kotlin sigue siendo `org.autojs.build.platform`
* `Función` Primera cadena de publicación pública en línea para Maven Central y Gradle Plugin Portal, con artefactos firmados de implementación, fuentes, Javadoc, metadatos de módulo y marcador del plugin
* `Mejora` Se añadieron el repositorio público de GitHub, los metadatos POM completos para Central, un bundle reproducible para el Portal, la validación aislada del consumidor y rutas de firma seguras separadas para GPG local y CI
* `Mejora` El uso oficial ahora resuelve únicamente desde repositorios públicos sin `mavenLocal()`; la herramienta de migración también reconoce y actualiza el ID antiguo del plugin

# v1.5.0

###### 2026/08/28

* `Función` El conjunto completo para actualizar los datos de compatibilidad se trasladó a este repositorio, con un `run-scrapers.bat` interactivo para actualizaciones manuales y comandos multiplataforma de actualización y comprobación de solo lectura preparados para futuras ejecuciones periódicas en CI
* `Mejora` Los scrapers ya no dependen de Puppeteer ni Chrome: analizan fuentes oficiales estáticas, centralizan los límites de conservación y la validación de salida, y evitan reescrituras causadas solo por marcas de tiempo
* `Mejora` Los datos integrados se actualizaron a Gradle 9.7 con Kotlin 2.4, las líneas más recientes de AGP hasta 9.5.0-alpha03, 9.4.0-rc02 y 9.3.2, Android Studio Rabbit y KSP 2.3.11

# v1.4.1

###### 2026/08/18

* `Mejora` Ya no se imprime en la consola ninguna nota explicativa cuando la tabla de correspondencias se ha quedado atrás. El sufijo [auto-specified] al final de la línea de versión ya indica cómo se llegó a ella, y la nota resultaba más larga que el resumen que explicaba
* `Mejora` Con ello se elimina la API de notas: PlatformVersionsExtension.notes y el parámetro notes de Formatted dejan de existir, de modo que un script consumidor que leyera esa propiedad debe ajustarse

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
run-scrapers.bat
.utils/
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
