<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>Un plugin Gradle Settings qui détermine automatiquement les versions d'AGP et du plugin Kotlin pour l'écosystème AutoJs6</p>

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

### Langues (Languages)

******

README.md est actuellement disponible dans les langues suivantes:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-en.md)
- Français [fr] # actuel
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ar.md)

******

### Introduction

******

Ce plugin extrait la logique de décision des versions de build qui était jusqu'ici dupliquée dans le projet principal AutoJs6 et dans chacun de ses projets de plugins. Chaque dépôt traînait dans son settings.gradle.kts plusieurs centaines de lignes quasi identiques, chargées de déterminer quel IDE pilotait le build, puis de choisir en conséquence les versions d'AGP et de Kotlin.

Désormais publié comme plugin Settings, il ne demande plus qu'une dizaine de lignes aux projets en aval. Une amélioration de la logique profite à tous les projets dès qu'ils montent la version du plugin, sans avoir à recopier le même code dépôt après dépôt.

******

### Fonctionnalités

******

- Détection de l'hôte de build : Android Studio, IntelliJ IDEA, Temurin JDK et la simple ligne de commande.
- Choix d'une version d'AGP prise en charge par l'IDE courant, avec repli sur l'entrée immédiatement inférieure en l'absence de correspondance exacte.
- Repli automatique sur la sélection auto lorsque l'IDE est plus récent que toutes les entrées de la table de correspondance, afin d'éviter une rétrogradation silencieuse vers un AGP trop ancien.
- Traitement explicite de Temurin et de la ligne de commande comme environnements sans IDE, avec sélection d'AGP selon la compatibilité Gradle plutôt que selon une table de versions d'IDE.
- Intersection de la limite supérieure IDE/Gradle avec les versions minimales d'AGP requises par le niveau d'API Android, KSP et le projet, avec échec anticipé lorsqu'aucune version n'est compatible.
- Détermination de la version de R8, un R8 externe n'étant introduit que lorsque celui fourni avec AGP n'est pas assez récent.
- Données de compatibilité distribuées avec le plugin, un répertoire `gradle/data` présent dans le projet consommateur restant prioritaire, ce qui facilite les corrections urgentes.
- Porte de sortie `OVERRIDDEN_*` conservée dans `version.properties`, pour figer directement les versions lorsqu'un build déterministe est requis.
- README et CHANGELOG disponibles en espagnol/français/russe/arabe/japonais/coréen/anglais/chinois simplifié/chinois traditionnel de Hong Kong/chinois traditionnel de Taïwan.

******

### Utilisation

******

Appliquez le plugin dans le fichier `settings.gradle.kts` du projet consommateur, avant `includeBuild`:

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

Les scripts de module peuvent ensuite déclarer les plugins via le plugins DSL, les versions provenant du résultat de la décision:

```kotlin
plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version")
    id("org.jetbrains.kotlin.android") version System.getProperty("gradle.kotlin.version")
}
```

Le résultat de la décision est également lisible sous forme d'objet via `gradle.extra["platformVersions"]`.

******

### Processus de décision

******

La version d'AGP est déterminée en trois étapes:

- Utiliser la table de la plateforme comme limite supérieure dans un IDE, y compris son repli lorsqu'elle est en retard ; pour Temurin et la ligne de commande, utiliser directement la compatibilité Gradle.
- Plafonner à nouveau cette limite avec la table officielle de compatibilité AGP/Gradle, afin que le candidat soit chargeable par le Gradle en cours d'exécution.
- Déduire les limites inférieures de compileSdk/targetSdk, de KSP et d'un minimum facultatif du projet, puis ne renvoyer AGP que si les limites se croisent.

La version de Kotlin suit quant à elle Gradle et non l'IDE : c'est toujours la plus récente que le Gradle courant prend en charge.

******

### Versions figées

******

Pour les tests ou un build déterministe, les versions exactes peuvent être figées directement dans `version.properties`:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

La valeur `NONE` ou une valeur vide signifie qu'aucune version n'est figée. Pour déclarer une limite inférieure sans imposer une version exacte, utilisez `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION` ; les valeurs numériques de `COMPILE_SDK_VERSION` et `TARGET_SDK_VERSION` sont prises en compte automatiquement.

******

### Données de compatibilité

******

La décision s'appuie sur les fichiers de données suivants, distribués avec le plugin:

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

Si le projet consommateur place un fichier du même nom dans son propre répertoire `gradle/data`, c'est ce fichier qui prévaut.

******

### Mise à jour des données

******

Les développeurs peuvent mettre à jour toutes les données de compatibilité en exécutant le point d'entrée interactif depuis la racine du dépôt:

```bat
run-scrapers.bat
```

Le workflow planifié du dépôt utilise ce point d'entrée en lecture seule ; le mode update manuel ouvre une pull request de données après validation:

```bash
npm --prefix .utils ci
npm --prefix .utils test
npm --prefix .utils run check-data
```

`check-data` ne modifie pas l'espace de travail : le code de sortie `0` indique des données à jour, `2` indique des mises à jour disponibles et `1` indique un échec.

Pour la portée complète et les conventions d'exécution, consultez [.utils/README.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.utils/README.md).

******

### Historique des versions

******

# v1.6.0

###### 2026/08/29

* `Note` L'identifiant permanent du plugin Gradle est désormais `io.github.supermonster003.autojs6-platform-versions` au lieu de `org.autojs.build.platform-versions`, et les coordonnées Maven sont `io.github.supermonster003:autojs6-gradle-platform-versions` ; le package Java/Kotlin reste `org.autojs.build.platform`
* `Fonctionnalité` Première chaîne de publication publique en ligne pour Maven Central et le Gradle Plugin Portal, avec des artefacts signés pour l'implémentation, les sources, le Javadoc, les métadonnées de module et le marqueur du plugin
* `Amélioration` Ajout du dépôt GitHub public, des métadonnées POM complètes pour Central, d'un bundle Portal reproductible, d'une validation isolée côté consommateur et de voies de signature sécurisées distinctes pour GPG local et la CI
* `Amélioration` L'utilisation officielle se résout désormais uniquement depuis les dépôts publics sans `mavenLocal()` ; l'outil de migration reconnaît et met également à jour l'ancien identifiant du plugin

# v1.5.0

###### 2026/08/28

* `Fonctionnalité` La suite complète de mise à jour des données de compatibilité réside désormais dans ce dépôt, avec un `run-scrapers.bat` interactif pour les mises à jour manuelles et des commandes multiplateformes de mise à jour et de contrôle en lecture seule prêtes pour de futures exécutions CI planifiées
* `Amélioration` Les scrapers ne dépendent plus de Puppeteer ni de Chrome : ils analysent les sources officielles statiques, centralisent les limites de conservation et la validation des sorties, et évitent les réécritures dues uniquement aux horodatages
* `Amélioration` Les données embarquées ont été actualisées jusqu'à Gradle 9.7 avec Kotlin 2.4, aux dernières lignes AGP jusqu'à 9.5.0-alpha03, 9.4.0-rc02 et 9.3.2, à Android Studio Rabbit et à KSP 2.3.11

# v1.4.1

###### 2026/08/18

* `Amélioration` Plus aucune note explicative n'est affichée dans la console lorsque la table de correspondance a pris du retard. Le suffixe [auto-specified] en fin de ligne de version indique déjà comment la version a été obtenue, et la note était plus longue que le résumé qu'elle expliquait
* `Amélioration` L'API notes disparaît par la même occasion : PlatformVersionsExtension.notes et le paramètre notes de Formatted n'existent plus, un script consommateur qui lisait cette propriété doit donc être adapté

##### Pour un historique plus complet, voir

* [CHANGELOG-fr.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-fr.md)

******

### Build

******

```powershell
.\gradlew.bat build
```

Publier dans le dépôt Maven local:

```powershell
.\gradlew.bat publishToMavenLocal
```

Le numéro de version du plugin provient de `VERSION_NAME` dans `version.properties`.

******

### Organisation des ressources

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

La logique de décision se trouve dans `src/main/kotlin` et les données de compatibilité sont empaquetées comme ressources dans `src/main/resources` ; `sample` est un projet consommateur minimal servant à vérifier le résultat des décisions. README et CHANGELOG sont générés à partir des sources JSON par `.python/generate_markdown.py`.

******

### Liens

******

- Projet principal AutoJs6: https://github.com/SuperMonster003/AutoJs6
- Notes de version du plugin Android Gradle: https://developer.android.com/build/releases/gradle-plugin
- Matrice de compatibilité Gradle: https://docs.gradle.org/current/userguide/compatibility.html
