<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>Un plugin Gradle Settings qui détermine automatiquement les versions d'AGP et du plugin Kotlin pour l'écosystème AutoJs6</p>

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
        id("org.autojs.build.platform-versions") version "1.4.0"
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

# v1.4.0

###### 2026/08/18

* `Correctif` Le plafond d'AGP n'est plus relevé lorsque la version de l'IDE ne constitue qu'une mise à jour de niveau correctif. IntelliJ IDEA 2026.2.1 aboutissait jusqu'ici à AGP 9.2.1 et se voyait refusé par l'IDE ; il reste désormais sur la ligne 9.1, comme 2026.2
* `Amélioration` Ajout d'une entrée 2026.2 à la table de correspondance IntelliJ IDEA, dont le plafond d'AGP reprend la valeur signalée par l'IDE lui-même
* `Amélioration` La migration déclare désormais la version du plugin une seule fois dans le script de build racine, sans toucher aux scripts de module ; ajouter la version module par module ne pouvait pas fonctionner pour les modules Groovy, dont le bloc plugins n'accepte que des littéraux de chaîne
* `Amélioration` Les notes affichées dans la console sont déplacées dans un paragraphe distinct sous le résumé des versions, au lieu d'être entrelacées avec les lignes de versions

# v1.3.0

###### 2026/08/18

* `Note` Gradle 8 n'est plus pris en charge. AGP 9.0 est la première version à exiger Gradle 9, la plage prise en charge démarre donc à AGP 9.0
* `Amélioration` Les entrées antérieures à 9 sont retirées des tables de compatibilité, et la table de correspondance IntelliJ IDEA ne conserve que les entrées aboutissant à AGP 9
* `Amélioration` Lorsque le Gradle courant est plus ancien que toutes les entrées de compatibilité, il n'y a plus de repli sur l'entrée la plus basse : une erreur explicite est signalée, afin qu'une version impossible à charger ne se retrouve jamais sur le classpath
* `Amélioration` Versions minimales prises en charge relevées : Gradle 9.1.0, Android Studio 2025.2.3, IntelliJ IDEA 2026.1.2, AGP 9.0
* `Amélioration` Badges du README alignés sur les versions ci-dessus, avec l'ajout d'un badge AGP
* `Amélioration` Le script de migration prend en charge le sucre syntaxique kotlin(...) ainsi que les anciens noms courts tels que kotlin-android/kotlin-kapt/kotlin-parcelize, ces noms courts étant développés en identifiants de plugin complets
* `Amélioration` Le script de migration ignore deux catégories de dépôts impossibles à migrer : ceux dont les fragments de script inclus via apply(from=) référencent des types AGP, et ceux pour lesquels la vérification des dépendances est activée

# v1.2.0

###### 2026/08/18

* `Fonctionnalité` Script de conversion des scripts de module `.python/migrate_modules.py`, qui réécrit les applications de plugins sans version sous la forme versionnée, la version étant lue depuis une propriété système
* `Fonctionnalité` La version de KSP retenue est désormais publiée aussi sous la propriété système `gradle.ksp.version`, conformément au nommage employé pour AGP et Kotlin
* `Correctif` Le retour arrière du script de conversion des modules ne restaurait pas les fichiers d'origine et laissait les sauvegardes derrière lui
* `Amélioration` Le script de migration settings vérifie d'abord que les scripts de module sont prêts et se contente d'un avertissement sans réécrire lorsqu'ils ne le sont pas, afin de ne pas laisser un état intermédiaire non constructible
* `Amélioration` Le script de migration settings fusionne désormais le plugin dans le bloc plugins existant et le place avant `includeBuild`, au lieu d'ajouter un nouveau bloc

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
