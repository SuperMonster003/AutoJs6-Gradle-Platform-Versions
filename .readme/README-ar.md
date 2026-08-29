<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>إضافة Gradle Settings تحدد تلقائيًا إصداري AGP وإضافة Kotlin لمنظومة AutoJs6</p>

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
gradle/data/android-studio-build-version.properties
gradle/data/android-studio-codename-version.properties
gradle/data/android-studio-codename.properties
gradle/data/kotlin-r8-compat.properties
gradle/data/ksp-agp-compat.properties
gradle/data/ksp-releases.properties
```

وإذا وضع المشروع المستهلك ملفًا بالاسم نفسه في مجلد `gradle/data` الخاص به، فإن هذا الملف هو الذي يسري.

******

### تحديث البيانات

******

يمكن للمطورين تحديث جميع بيانات التوافق بتشغيل نقطة دخول الدفعة التفاعلية من جذر المستودع:

```bat
run-scrapers.bat
```

يمكن لمهمة CI مجدولة مستقبلًا استخدام نقطة دخول الفحص للقراءة فقط التالية:

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

# v1.6.0

###### 2026/08/29

* `تلميح` أصبح معرف إضافة Gradle الدائم `io.github.supermonster003.autojs6-platform-versions` بدلا من `org.autojs.build.platform-versions`، وصارت إحداثيات Maven هي `io.github.supermonster003:autojs6-gradle-platform-versions`؛ فيما تبقى حزمة Java/Kotlin باسم `org.autojs.build.platform`
* `ميزة` أول مسار إصدار عام عبر الإنترنت إلى Maven Central وGradle Plugin Portal، مع توقيع مكونات التنفيذ والمصادر وJavadoc وبيانات الوحدة وملف تعريف الإضافة
* `تحسين` أضيف مستودع GitHub العام وبيانات POM الكاملة لـ Central وحزمة Portal قابلة لإعادة الإنتاج والتحقق المعزول من جانب المستهلك ومسارا توقيع آمنان منفصلان لـ GPG المحلي وCI
* `تحسين` يُحل الاستخدام الرسمي الآن من المستودعات العامة وحدها من دون `mavenLocal()`؛ كما تتعرف أداة الترحيل على معرف الإضافة القديم وتحدثه

# v1.5.0

###### 2026/08/28

* `ميزة` انتقلت مجموعة تحديث بيانات التوافق بالكامل إلى هذا المستودع، مع ملف `run-scrapers.bat` تفاعلي للتحديث اليدوي وأوامر متعددة المنصات للتحديث والفحص للقراءة فقط استعدادا للتشغيل الدوري مستقبلا عبر CI
* `تحسين` لم تعد أدوات الجمع تعتمد على Puppeteer أو Chrome؛ فهي تحلل المصادر الرسمية الثابتة، وتجمع حدود الاحتفاظ والتحقق من المخرجات في إعداد واحد، وتتجنب إعادة الكتابة الناتجة عن تغير الطابع الزمني وحده
* `تحسين` حدثت البيانات المضمنة لتشمل Gradle 9.7 مع Kotlin 2.4، وإصدارات AGP الأحدث حتى 9.5.0-alpha03 و9.4.0-rc02 و9.3.2، وAndroid Studio Rabbit، وKSP 2.3.11

# v1.4.1

###### 2026/08/18

* `تحسين` لم تعد تُطبع في وحدة التحكم ملاحظة تفسيرية عندما يتخلف جدول المطابقة، إذ تكفي اللاحقة [auto-specified] في آخر سطر الإصدار لبيان كيفية بلوغه، وقد كانت الملاحظة أطول من الملخص الذي تفسره
* `تحسين` وأُزيلت معها واجهة الملاحظات: لم تعد PlatformVersionsExtension.notes ولا معامل notes في Formatted موجودَين، فيلزم تعديل أي برنامج مستهلك كان يقرأ تلك الخاصية

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
