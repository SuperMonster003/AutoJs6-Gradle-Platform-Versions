<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>إضافة Gradle Settings تحدد تلقائيًا إصداري AGP وإضافة Kotlin لمنظومة AutoJs6</p>

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
- معاملة Temurin وسطر الأوامر المجرد صراحةً كبيئتين بلا IDE، واختيار AGP وفق توافق Gradle بدل جدول مرتبط بإصدار IDE.
- تقاطع الحد الأعلى الذي يفرضه IDE وGradle مع الحدود الدنيا لـ AGP المستمدة من مستوى Android API وKSP والمشروع، والفشل مبكرًا عند غياب إصدار متوافق.
- تحديد إصدار R8، فلا يُجلب R8 خارجي إلا إذا كان R8 المرافق لـ AGP غير حديث بما يكفي.
- تُوزَّع بيانات التوافق مع الإضافة بوصفها مصدر البيانات الوحيد افتراضيًا؛ ولا تحتفظ مشاريع مضيف AutoJs6 وإضافاته الرسمية بنسخ داخل `gradle/data` لدى المستهلك.
- الإبقاء على مخرج الطوارئ `OVERRIDDEN_*` في `version.properties`، إذ يمكن تثبيت الإصدار مباشرة عند الحاجة إلى بناء حتمي.
- يدعم ملفا README وCHANGELOG الإسبانية والفرنسية والروسية والعربية واليابانية والكورية والإنجليزية والصينية المبسطة والصينية التقليدية (هونغ كونغ) والصينية التقليدية (تايوان).

******

### طريقة الاستخدام

******

طبّق الإضافة في ملف `settings.gradle.kts` الخاص بالمشروع المستهلك، على أن يسبق موضعُها `includeBuild`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        id("io.github.supermonster003.autojs6-platform-versions") version "1.7.1"
    }
}

plugins {
    id("io.github.supermonster003.autojs6-platform-versions")
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

- استخدام أقدم مفتاح في جدول المنصة حدًا أدنى مركزيًا لدعم IDE، واستخدام AGP المطابق حدًا أعلى؛ ولا يستطيع الحد الأدنى لـ IDE في المشروع المستهلك إلا تشديد ذلك الحد. ويظل مسار الرجوع متاحًا لبيئات IDE الأحدث عند تأخر الجدول، أما Temurin وسطر الأوامر المجرد فيستخدمان حد توافق Gradle مباشرةً.
- تقييد ذلك الحد مرة أخرى بجدول التوافق الرسمي بين AGP وGradle، كي يستطيع Gradle الجاري تحميل الإصدار المرشح.
- اشتقاق الحدود الدنيا من compileSdk/targetSdk وKSP ومن حد أدنى اختياري للمشروع، وعدم إرجاع AGP إلا عند وجود تقاطع بين الحدود.

أما إصدار Kotlin فيتبع Gradle لا بيئة التطوير، ويُختار دائمًا أحدث إصدار يدعمه Gradle الحالي.

******

### تثبيت الإصدار

******

للاختبار أو لبناء حتمي يمكن تثبيت الإصدارات الدقيقة مباشرةً في `version.properties`:

```properties
OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION=9.0.1
OVERRIDDEN_KOTLIN_GRADLE_PLUGIN_VERSION=2.2.21
```

وتعني القيمة `NONE` أو القيمة الفارغة عدم تثبيت أي إصدار. ولا يُستخدم `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION` إلا لحد أدنى فعلي خاص بالمشروع لا تستطيع الآلية المركزية استنتاجه؛ ويجب ألا تكرر به مشاريع AutoJs6 الرسمية حد AGP 9 العام الذي تضمنه المنصة. وتدخل القيم الرقمية لـ `COMPILE_SDK_VERSION` و`TARGET_SDK_VERSION` في القرار تلقائيًا. وبالمثل، فإن `MIN_SUPPORTED_ANDROID_STUDIO_IDE_VERSION` و`MIN_SUPPORTED_INTELLIJ_IDEA_IDE_VERSION` قيدان اختياريان خاصان بالمشروع؛ فأقدم مفتاح في كل جدول IDE مركزي هو خط أساس لا يستطيع المستهلك خفضه، لذا ينبغي حذف الخاصيتين ما لم يحتج المشروع فعلًا إلى IDE أحدث.

******

### بيانات التوافق

******

فيما يلي ملفات البيانات التي يستند إليها التحديد، وهي تُوزَّع مع الإضافة:

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

ويبقى الملف المطابق في `gradle/data` لدى المستهلك ذا أولوية للتوافق مع الإصدارات القديمة أو للتشخيص المؤقت فقط، وليس ذلك أسلوب التشغيل الرسمي. ويجب ألا تُودع مشاريع مضيف AutoJs6 وإضافاته الرسمية هذه التجاوزات؛ بل تُحدَّث بيانات التوافق في هذا المستودع المركزي وتُنشر ضمن إصدار جديد غير قابل للتغيير من الإضافة.

******

### تحديث البيانات

******

يمكن للمطورين تحديث جميع بيانات التوافق بتشغيل نقطة دخول الدفعة التفاعلية من جذر المستودع:

```bat
run-scrapers.bat
```

يستخدم workflow الدوري للمستودع نقطة الدخول التالية للفحص فقط، بينما ينشئ وضع update اليدوي pull request للبيانات بعد التحقق:

```bash
npm --prefix .utils ci
npm --prefix .utils test
npm --prefix .utils run check-data
```

لا يعدل `check-data` مساحة العمل: يعني رمز الخروج `0` أن البيانات محدثة، و`2` وجود تحديثات، و`1` فشل المهمة.

للاطلاع على نطاق التحديث الكامل واتفاقية التشغيل، راجع [.utils/README.md](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.utils/README.md).

******

### سجل الإصدارات

******

# v1.7.1

###### 2026/09/02

* `إصلاح` فُرض أقدم إدخال في كل خريطة توافق IDE بوصفه الحد الأدنى المركزي للدعم؛ ولا تستطيع قيم `MIN_SUPPORTED_*_IDE_VERSION` لدى المستهلك إلا تشديده، ولم يعد بإمكانها تحويل IDE قديم غير مدعوم إلى مسار الرجوع الذي يعتمد على Gradle وحده
* `تحسين` تظل متطلبات الحد الأدنى لـ AGP المستوفاة متاحة في النتيجة القابلة للقراءة آليًا من دون الظهور في ملخصات البناء الناجح المعتادة؛ وتعرض أخطاء عدم التوافق الآن إصدار IDE المكتشف وجميع مصادر المتطلبات
* `تحسين` وُضح أن بيانات التوافق المضمنة هي المصدر المعتمد للمستهلكين الرسميين في AutoJs6؛ ولا تبقى تجاوزات `gradle/data` إلا للتوافق مع الإصدارات القديمة أو للتشخيص المؤقت

# v1.7.0

###### 2026/09/02

* `تلميح` ينبغي للبناء العادي التصريح بمستويات SDK، وبـ `MIN_SUPPORTED_ANDROID_GRADLE_PLUGIN_VERSION` عند الحاجة فقط؛ ويُحفظ `OVERRIDDEN_ANDROID_GRADLE_PLUGIN_VERSION` لاختبارات الإصدار الدقيق المقصودة أو كمسار استثنائي للخروج
* `ميزة` يختار AGP الآن من تقاطع الحدود الدنيا لـ Android API والمشروع وKSP مع الحدود العليا لـ Gradle وIDE النشطين، مع بيان مصدر القيد عندما لا يتوفر إصدار متوافق
* `ميزة` أضيفت مسارات GitHub Actions لبناء Temurin والفحص الدوري لبيانات التوافق أو إنشاء طلبات تحديث، وللنشر المحمي بالوسم والموافقة إلى Maven Central وGradle Plugin Portal
* `إصلاح` لم تعد عمليات البناء عبر Temurin أو سطر الأوامر المجرد تستخدم خرائط JDK إلى AGP القديمة، لذلك لن يختار JDK `21.0.6+7` الإصدار AGP 8.7.3 بصمت؛ ويتطلب Android API 36 الآن AGP 8.9.1 أو أحدث تلقائيا
* `إصلاح` أصلحت خرائط IDE ثنائية الأجزاء التي كانت تتجاوز سقف AGP الخاص بـ Gradle، وارتداد إصدارات Gradle القديمة إلى إصدار منصة لا يمكنها تحميله
* `تحسين` أضيفت بيانات رسمية مستقلة الجمع لربط Android API بالحد الأدنى من AGP، وحُدثت بيانات Android Studio وإصدارات AGP وتوافق AGP مع Gradle
* `تحسين` وُسع التحقق ليشمل 70 اختبار JVM واختبارات Node للتحليل وثبات النتيجة، وبناء مثال حقيقي على Temurin 17 في CI يختبر الاختيار التلقائي دون IDE

# v1.6.0

###### 2026/08/29

* `تلميح` أصبح معرف إضافة Gradle الدائم `io.github.supermonster003.autojs6-platform-versions` بدلا من `org.autojs.build.platform-versions`، وصارت إحداثيات Maven هي `io.github.supermonster003:autojs6-gradle-platform-versions`؛ فيما تبقى حزمة Java/Kotlin باسم `org.autojs.build.platform`
* `ميزة` أول مسار إصدار عام عبر الإنترنت إلى Maven Central وGradle Plugin Portal، مع توقيع مكونات التنفيذ والمصادر وJavadoc وبيانات الوحدة وملف تعريف الإضافة
* `تحسين` أضيف مستودع GitHub العام وبيانات POM الكاملة لـ Central وحزمة Portal قابلة لإعادة الإنتاج والتحقق المعزول من جانب المستهلك ومسارا توقيع آمنان منفصلان لـ GPG المحلي وCI
* `تحسين` يُحل الاستخدام الرسمي الآن من المستودعات العامة وحدها من دون `mavenLocal()`؛ كما تتعرف أداة الترحيل على معرف الإضافة القديم وتحدثه

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
run-scrapers.bat
.utils/
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
