<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <source srcset="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/app/src/main/res/mipmap-night/banner.png?raw=true" media="(prefers-color-scheme: dark)" />
      <img src="https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/app/src/main/res/mipmap/banner.png?raw=true" alt="banner" border="0" width="512" />
    </picture>
  </p>

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
- Distribue les données de compatibilité avec le plugin comme source de données unique par défaut ; les projets officiels de l'hôte et des plugins AutoJs6 ne conservent aucune copie de `gradle/data` côté consommateur.
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
        id("io.github.supermonster003.autojs6-platform-versions") version "1.7.2"
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

- Utiliser la clé la plus ancienne de la table de plateforme comme limite inférieure centrale de l'IDE et l'AGP correspondant comme limite supérieure ; un minimum d'IDE déclaré par le consommateur ne peut que resserrer cette limite. Conserver le repli pour les IDE plus récents lorsque la table est en retard ; pour Temurin et la ligne de commande, utiliser directement la compatibilité Gradle.
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

La valeur `NONE` ou une valeur vide signifie qu'aucune version n'est figée. Utilisez `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION` uniquement pour une véritable limite inférieure propre au projet que le mécanisme central ne peut pas déduire ; les consommateurs officiels AutoJs6 ne doivent pas répéter ainsi la limite générale AGP 9 déjà garantie par la plateforme. Les valeurs numériques de `COMPILE_SDK_VERSION` et `TARGET_SDK_VERSION` sont prises en compte automatiquement. De même, `MIN_SUPPORTED_ANDROID_STUDIO_IDE_VERSION` et `MIN_SUPPORTED_INTELLIJ_IDEA_IDE_VERSION` sont des restrictions facultatives propres au projet : la clé la plus ancienne de chaque table IDE centrale est une base impossible à abaisser, donc omettez ces propriétés sauf si le projet exige réellement un IDE plus récent.

******

### Données de compatibilité

******

La décision s'appuie sur les fichiers de données suivants, distribués avec le plugin:

```text
src/main/resources/org/autojs/build/platform/data/
  agp-releases.list
  agp-gradle-compat.properties
  android-api-agp-compat.properties
  gradle-kotlin-compat.properties
  java-gradle-compat.properties
  android-studio-agp-compat.properties
  android-studio-build-version.properties
  android-studio-codename-version.properties
  android-studio-codename.properties
  kotlin-r8-compat.properties
  ksp-agp-compat.properties
  ksp-releases.properties
```

Un fichier de même nom dans le répertoire `gradle/data` du consommateur reste prioritaire uniquement pour la compatibilité historique ou un diagnostic temporaire ; ce n'est pas le modèle officiel. Les projets officiels de l'hôte et des plugins AutoJs6 ne doivent pas placer ces substitutions sous contrôle de version. Les données de compatibilité doivent être mises à jour dans ce dépôt central et publiées avec une nouvelle version immuable du plugin.

******

### Mise à jour des données

******

Les développeurs peuvent mettre à jour toutes les données de compatibilité en exécutant le point d'entrée interactif depuis la racine du dépôt:

```bat
run-scrapers.bat
```

Le workflow quotidien actualise et valide les données, ne crée un commit et un tag de version corrective qu'en cas de changement sémantique, puis lance la chaîne protégée des deux registres et de GitHub Release ; les modes manuels check et update-pr restent disponibles:

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

# v1.7.2

###### 2026/09/03

* `Correctif` Fusion des propriétés d’identité fournies par Android Studio via Gradle `-P` avec les propriétés système JVM : Quail 3 n’est plus réduit à `2026.1`, ne sélectionne plus AGP 9.2.1 par erreur et configure correctement la cible automatique avec les JDK 25/26
* `Amélioration` Ajout d’une surcharge Facade compatible au niveau binaire pour fournir explicitement les propriétés de projet Gradle et d’un repli sur la version strict pour les builds IDE pas encore collectés ; vérification réussie de Quail 3 avec Gradle 9.5/9.7 et les JDK 25/26, avec sélection d’AGP 9.3.2 et création des tâches Kotlin/KSP

# v1.7.1

###### 2026/09/02

* `Correctif` La plus ancienne entrée de chaque table de compatibilité IDE est désormais imposée comme borne inférieure centrale de support ; les valeurs `MIN_SUPPORTED_*_IDE_VERSION` du consommateur ne peuvent que la resserrer et ne peuvent plus faire basculer un ancien IDE non pris en charge vers le repli fondé uniquement sur Gradle
* `Amélioration` Les contraintes minimales AGP satisfaites restent disponibles sous forme exploitable par machine sans apparaître dans les résumés ordinaires des builds réussis ; les erreurs d'incompatibilité incluent maintenant la version d'IDE détectée et toutes les sources d'exigences
* `Amélioration` Précision que les données de compatibilité embarquées constituent la source de référence des consommateurs AutoJs6 officiels ; les substitutions dans `gradle/data` ne subsistent que pour la compatibilité historique ou les diagnostics temporaires

# v1.7.0

###### 2026/09/02

* `Note` Les builds ordinaires doivent déclarer leurs niveaux de SDK et, uniquement si nécessaire, `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION` ; réservez `OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION` aux tests volontaires d'une version exacte ou à un usage exceptionnel comme échappatoire
* `Fonctionnalité` La sélection d'AGP croise désormais les bornes inférieures d'Android API, du projet et de KSP avec les bornes supérieures du Gradle et de l'IDE actifs, et indique la source lorsqu'aucune version compatible n'existe
* `Fonctionnalité` Ajout de workflows GitHub Actions pour les builds Temurin, les contrôles planifiés des données de compatibilité ou les PR de mise à jour, et la publication protégée par tag et approbation vers Maven Central et le Gradle Plugin Portal
* `Correctif` Les builds Temurin et en ligne de commande pure ne consultent plus les anciens mappages JDK vers AGP : le JDK `21.0.6+7` ne peut donc plus choisir silencieusement AGP 8.7.3 ; Android API 36 exige maintenant automatiquement AGP 8.9.1 ou plus récent
* `Correctif` Correction des mappages d'IDE à deux composantes qui contournaient le plafond AGP de Gradle et du repli des anciens Gradle vers une version de plateforme qu'ils ne peuvent pas charger
* `Amélioration` Ajout de données officielles Android API vers AGP minimal collectées indépendamment et actualisation des données Android Studio, des versions AGP et de compatibilité AGP/Gradle
* `Amélioration` Validation étendue à 70 tests JVM, aux tests Node d'analyse et d'idempotence, ainsi qu'à un véritable build d'exemple sous Temurin 17 en CI exerçant la sélection automatique sans interface

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
