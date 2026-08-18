<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>إضافة Gradle Settings تحدد تلقائيًا إصداري AGP وإضافة Kotlin لمنظومة AutoJs6</p>

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

### اللغات (Languages)

******

يدعم ملف README.md حاليًا اللغات التالية:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.readme/README-ru.md)
- العربية [ar] # الحالي

******

### مقدمة

******

تستخلص هذه الإضافة منطق تحديد إصدارات البناء الذي كان يُصان مكررًا في مشروع AutoJs6 الرئيسي وفي مشاريع الإضافات كافة. فقد كان ملف settings.gradle.kts في كل مستودع يحمل مئات الأسطر المتطابقة تقريبًا؛ مهمتها معرفة أي بيئة تطوير تنفذ البناء حاليًا، ثم اختيار إصداري AGP وKotlin المناسبين بناءً على ذلك.

وبعد تحويل ذلك إلى إضافة Settings قابلة للنشر، لم تعد المشاريع المستهلكة بحاجة إلا إلى بضعة عشر سطرًا للتضمين. يكفي تحسين المنطق مرة واحدة لتحصل جميع المشاريع على التحسين برفع إصدار الإضافة فحسب، دون نسخ التعديلات ولصقها في كل مستودع على حدة.

******

### الميزات

******

- تمييز بيئة البناء: Android Studio وIntelliJ IDEA وTemurin JDK إضافة إلى سطر الأوامر المجرد.
- اختيار إصدار AGP الذي تستطيع بيئة التطوير الحالية دعمه، ومع غياب التطابق التام يُختار أقرب إصدار أدنى.
- عند كون إصدار بيئة التطوير أحدث من جميع مدخلات جدول المطابقة، يجري الرجوع تلقائيًا إلى اختيار auto، تفاديًا للهبوط الصامت إلى إصدار AGP قديم أكثر من اللازم.
- فرض حد أعلى وفق علاقة التوافق بين AGP وGradle، بما يضمن أن الإصدار المختار قادر Gradle الحالي على تحميله قطعًا.
- تحديد إصدار KSP، مع رفع إصدار AGP تلقائيًا عندما يتطلب KSP المختار إصدار AGP أعلى.
- تحديد إصدار R8، فلا يُجلب R8 خارجي إلا إذا كان R8 المرافق لـ AGP غير حديث بما يكفي.
- تُوزَّع بيانات التوافق مع الإضافة نفسها، وإذا وُجد المجلد `gradle/data` في المشروع المستهلك فله الأولوية؛ ما يسهّل تصحيح البيانات على وجه السرعة.
- الإبقاء على مخرج الطوارئ `OVERRIDDEN_*` في `version.properties`، إذ يمكن تثبيت الإصدار مباشرة عند الحاجة إلى بناء حتمي.
- يدعم ملفا README وCHANGELOG الإسبانية والفرنسية والروسية والعربية واليابانية والكورية والإنجليزية والصينية المبسطة والصينية التقليدية (هونغ كونغ) والصينية التقليدية (تايوان).

******

### طريقة الاستخدام

******

طبّق الإضافة في ملف `settings.gradle.kts` الخاص بالمشروع المستهلك، على أن يسبق موضعُها `includeBuild`:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("org.autojs.build.platform-versions") version "1.1.0"
    }
}

plugins {
    id("org.autojs.build.platform-versions")
}
```

بعد ذلك يمكن لبرامج الوحدات التصريح بالإضافات عبر plugins DSL، وتُؤخذ الإصدارات من نتيجة التحديد:

```kotlin
plugins {
    id("com.android.application") version System.getProperty("gradle.agp.version")
    id("org.jetbrains.kotlin.android") version System.getProperty("gradle.kotlin.version")
}
```

ويمكن أيضًا قراءة نتيجة التحديد بوصفها كائنًا عبر `gradle.extra["platformVersions"]`.

******

### مسار التحديد

******

تمر عملية تحديد إصدار AGP بثلاث خطوات:

- مطابقة إصدار المنصة الحالي بأقرب مدخلة أدنى في جدول مطابقة AGP الخاص بتلك المنصة.
- تحديد ما إذا كان جدول المطابقة متأخرًا، أي هل بيئة التطوير الحالية أحدث من جميع مدخلاته؛ فإن كان كذلك جرى الرجوع إلى اختيار auto.
- فرض حد أعلى وفق جدول التوافق بين AGP وGradle، فلا تتجاوز النتيجة ما يستطيع Gradle الحالي تحميله.

أما إصدار Kotlin فيتبع Gradle لا بيئة التطوير، ويُختار دائمًا أحدث إصدار يدعمه Gradle الحالي.

******

### تثبيت الإصدار

******

إن أردت تخطي التحديد التلقائي بالكامل، فبإمكانك تعيين الإصدار مباشرة في `version.properties`:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

والقيمة `NONE` أو القيمة الفارغة تعني عدم التعيين، فيسلك البناء مسار التحديد التلقائي.

******

### بيانات التوافق

******

فيما يلي ملفات البيانات التي يستند إليها التحديد، وهي تُوزَّع مع الإضافة:

```text
gradle/data/agp-releases.list
gradle/data/agp-gradle-compat.properties
gradle/data/gradle-kotlin-compat.properties
gradle/data/java-gradle-compat.properties
gradle/data/android-studio-agp-compat.properties
```

وإذا وضع المشروع المستهلك ملفًا بالاسم نفسه في مجلد `gradle/data` الخاص به، فإن هذا الملف هو الذي يسري.

******

### سجل الإصدارات

******

# v1.1.0

###### 2026/08/18

* `ميزة` تحديد إصدار R8 بالرجوع إلى الجدول وفق إصدار Kotlin الحالي، فلا يُجلب R8 خارجي صراحةً إلا إذا كان R8 المرافق لـ AGP غير حديث بما يكفي
* `ميزة` تحديد إصدار KSP، ويتبع رقم الإصدار إصدار Kotlin المستهدف؛ وعندما يتطلب KSP المختار إصدار AGP أعلى يُرفع إصدار AGP تلقائيًا
* `ميزة` إضافة نقطة الاستدعاء `PlatformVersionsFacade` إلى نتيجة التحديد، ويمكن استعمالها مباشرة داخل متن settings script
* `ميزة` نشر نتيجة التحديد أيضًا بوصفها system property، ليتسنى لبرامج الوحدات التصريح بإصدارات الإضافات عبر plugins DSL
* `ميزة` برنامج الترحيل الجماعي للمستودعات المستهلكة `.python/migrate_downstream.py`، يدعم المعاينة والتطبيق والتراجع، ويحتفظ بنسخة احتياطية لكل مستودع
* `إصلاح` كان `getMaxSupportedJavaVersion` يتلقى إصدار AGP خطأً، فينخفض الحد الأعلى لـ toolchain؛ وقد صار يتلقى إصدار Gradle
* `تحسين` إزالة مدخلة 2026.2.1 من جدول مطابقة IntelliJ IDEA، بحيث يحصل كل من 2026.2 و2026.2.1 على AGP 9.0.1، بما يوافق نطاق الدعم الفعلي لبيئة التطوير

# v1.0.0

###### 2026/08/18

* `ميزة` إضافة Gradle Settings باسم `org.autojs.build.platform-versions`، تحدد تلقائيًا إصداري AGP وKotlin Gradle Plugin
* `ميزة` تمييز بيئة البناء، بدعم Android Studio/IntelliJ IDEA/Temurin JDK إضافة إلى سطر الأوامر المجرد
* `ميزة` تحديد إصدار AGP، بمطابقة أقرب مدخلة أدنى في جدول المطابقة وفق إصدار بيئة التطوير الحالي
* `ميزة` الرجوع عند تأخر جدول المطابقة، فإذا كانت بيئة التطوير الحالية أحدث من جميع مدخلاته استُعمل اختيار auto بدلًا من الهبوط الصامت إلى إصدار AGP قديم أكثر من اللازم
* `ميزة` فرض حد أعلى على إصدار AGP وفق جدول توافق Gradle، بما يضمن أن الإصدار المختار قادر Gradle الحالي على تحميله قطعًا
* `ميزة` تحديد إصدار Kotlin Gradle Plugin، متابعًا أحدث إصدار يدعمه Gradle الحالي
* `ميزة` توزيع بيانات التوافق مع الإضافة، مع إمكانية تجاوز ملفات البيانات المتماثلة الاسم عبر مجلد `gradle/data` في المشروع المستهلك
* `ميزة` مخرج الطوارئ `OVERRIDDEN_*` في `version.properties`، إذ يمكن تعيين الإصدار مباشرة لتخطي التحديد التلقائي
* `ميزة` إتاحة نتيجة التحديد عبر `PlatformVersionsExtension`، وهي صالحة للاستعمال في تصريح classpath الخاص بـ buildscript
* `ميزة` مشروع مستهلك مصغّر باسم `sample`، للتحقق من نتيجة التحديد في ثلاثة سيناريوهات نموذجية
* `ميزة` موارد متعددة اللغات لملفي README وCHANGELOG: الإسبانية والفرنسية والروسية والعربية واليابانية والكورية والإنجليزية والصينية المبسطة والصينية التقليدية (هونغ كونغ) والصينية التقليدية (تايوان)

##### لمزيد من سجل الإصدارات يمكن الرجوع إلى

* [CHANGELOG-ar.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ar.md)

******

### البناء

******

```powershell
.\gradlew.bat build
```

النشر إلى مستودع Maven المحلي:

```powershell
.\gradlew.bat publishToMavenLocal
```

يُؤخذ رقم إصدار الإضافة من `VERSION_NAME` في `version.properties`.

******

### بنية الموارد

******

```text
src/main/kotlin/org/autojs/build/platform/
src/main/resources/org/autojs/build/platform/data/
sample/
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
```

يقع منطق التحديد في `src/main/kotlin`، وتُحزَم بيانات التوافق بوصفها موارد في `src/main/resources`؛ و`sample` مشروع مستهلك مصغّر يُستعمل للتحقق من نتيجة التحديد. ويُولَّد ملفا README وCHANGELOG بواسطة `.python/generate_markdown.py` انطلاقًا من ملفات JSON المصدرية.

******

### روابط ذات صلة

******

- مشروع AutoJs6 الرئيسي: https://github.com/SuperMonster003/AutoJs6
- ملاحظات إصدار Android Gradle Plugin: https://developer.android.com/build/releases/gradle-plugin
- مصفوفة توافق Gradle: https://docs.gradle.org/current/userguide/compatibility.html
