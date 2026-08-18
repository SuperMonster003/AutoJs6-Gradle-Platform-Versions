<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>Un plugin Gradle Settings qui détermine automatiquement les versions d'AGP et du plugin Kotlin pour l'écosystème AutoJs6</p>

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
- Plafonnement selon la compatibilité entre AGP et Gradle, ce qui garantit que la version retenue est toujours chargeable par le Gradle courant.
- Détermination de la version de KSP, avec relèvement automatique de la version d'AGP lorsque le KSP retenu exige un AGP plus récent.
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

- Rechercher la version de la plateforme courante dans la table de correspondance AGP de cette plateforme, en retenant l'entrée immédiatement inférieure.
- Vérifier si la table est en retard, c'est-à-dire si l'IDE courant est plus récent que toutes ses entrées ; si tel est le cas, revenir à la sélection auto.
- Plafonner le résultat avec la table de compatibilité entre AGP et Gradle, afin de ne jamais dépasser ce que le Gradle courant peut charger.

La version de Kotlin suit quant à elle Gradle et non l'IDE : c'est toujours la plus récente que le Gradle courant prend en charge.

******

### Versions figées

******

Pour contourner entièrement la décision automatique, indiquez directement les versions dans `version.properties`:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

La valeur `NONE` ou une valeur vide signifie qu'aucune version n'est figée et que le processus automatique s'applique.

******

### Données de compatibilité

******

La décision s'appuie sur les fichiers de données suivants, distribués avec le plugin:

```text
gradle/data/agp-releases.list
gradle/data/agp-gradle-compat.properties
gradle/data/gradle-kotlin-compat.properties
gradle/data/java-gradle-compat.properties
gradle/data/android-studio-agp-compat.properties
```

Si le projet consommateur place un fichier du même nom dans son propre répertoire `gradle/data`, c'est ce fichier qui prévaut.

******

### Historique des versions

******

# v1.2.0

###### 2026/08/18

* `Fonctionnalité` Script de conversion des scripts de module `.python/migrate_modules.py`, qui réécrit les applications de plugins sans version sous la forme versionnée, la version étant lue depuis une propriété système
* `Fonctionnalité` La version de KSP retenue est désormais publiée aussi sous la propriété système `gradle.ksp.version`, conformément au nommage employé pour AGP et Kotlin
* `Correctif` Le retour arrière du script de conversion des modules ne restaurait pas les fichiers d'origine et laissait les sauvegardes derrière lui
* `Amélioration` Le script de migration settings vérifie d'abord que les scripts de module sont prêts et se contente d'un avertissement sans réécrire lorsqu'ils ne le sont pas, afin de ne pas laisser un état intermédiaire non constructible
* `Amélioration` Le script de migration settings fusionne désormais le plugin dans le bloc plugins existant et le place avant `includeBuild`, au lieu d'ajouter un nouveau bloc

# v1.1.0

###### 2026/08/18

* `Fonctionnalité` Décision de la version de R8, obtenue par recherche selon la version de Kotlin courante, un R8 externe n'étant introduit explicitement que lorsque celui fourni avec AGP n'est pas assez récent
* `Fonctionnalité` Décision de la version de KSP, dont le numéro suit la version de Kotlin ciblée ; la version d'AGP est relevée automatiquement lorsque le KSP retenu exige un AGP plus récent
* `Fonctionnalité` Résultat de la décision désormais accessible via le point d'entrée `PlatformVersionsFacade`, utilisable directement dans le corps du script settings
* `Fonctionnalité` Résultat de la décision également publié sous forme de propriétés système, afin que les scripts de module déclarent les versions de plugins via le plugins DSL
* `Fonctionnalité` Script de migration par lots `.python/migrate_downstream.py` pour les dépôts en aval, prenant en charge la prévisualisation, l'application et le retour arrière, avec une sauvegarde conservée par dépôt
* `Correctif` `getMaxSupportedJavaVersion` recevait jusqu'ici la version d'AGP, ce qui abaissait le plafond de la toolchain ; il reçoit désormais la version de Gradle
* `Amélioration` Suppression de l'entrée 2026.2.1 de la table de correspondance IntelliJ IDEA, si bien que 2026.2 comme 2026.2.1 aboutissent à AGP 9.0.1, conformément à ce que l'IDE prend réellement en charge

# v1.0.0

###### 2026/08/18

* `Fonctionnalité` Plugin Gradle Settings `org.autojs.build.platform-versions`, qui détermine automatiquement les versions d'AGP et du plugin Gradle Kotlin
* `Fonctionnalité` Détection de l'hôte de build, prenant en charge Android Studio/IntelliJ IDEA/Temurin JDK ainsi que la simple ligne de commande
* `Fonctionnalité` Décision de la version d'AGP, par recherche de la version de l'IDE courant dans la table de correspondance avec repli sur l'entrée immédiatement inférieure
* `Fonctionnalité` Repli en cas de table de correspondance en retard : lorsque l'IDE courant est plus récent que toutes les entrées de la table, la sélection auto prend le relais, sans rétrogradation silencieuse vers un AGP trop ancien
* `Fonctionnalité` Version d'AGP plafonnée par la table de compatibilité Gradle, ce qui garantit que la version retenue est toujours chargeable par le Gradle courant
* `Fonctionnalité` Décision de la version du plugin Gradle Kotlin, alignée sur la version la plus récente prise en charge par le Gradle courant
* `Fonctionnalité` Données de compatibilité distribuées avec le plugin, le répertoire `gradle/data` du projet consommateur pouvant remplacer tout fichier de données de même nom
* `Fonctionnalité` Porte de sortie `OVERRIDDEN_*` dans `version.properties`, permettant de figer directement les versions et de contourner la décision automatique
* `Fonctionnalité` Résultat de la décision exposé via `PlatformVersionsExtension`, utilisable dans les déclarations de classpath du buildscript
* `Fonctionnalité` Projet consommateur minimal `sample`, servant à vérifier le résultat des décisions dans trois scénarios typiques
* `Fonctionnalité` Ressources multilingues pour README et CHANGELOG : espagnol/français/russe/arabe/japonais/coréen/anglais/chinois simplifié/chinois traditionnel de Hong Kong/chinois traditionnel de Taïwan

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
