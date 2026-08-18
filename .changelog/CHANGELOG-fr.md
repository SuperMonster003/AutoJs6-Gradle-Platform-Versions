******

### Langues (Languages)

******

CHANGELOG.md est actuellement disponible dans les langues suivantes:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-en.md)
- Français [fr] # actuel
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ar.md)

******

### Historique des versions

******

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
