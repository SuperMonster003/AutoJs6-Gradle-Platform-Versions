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
