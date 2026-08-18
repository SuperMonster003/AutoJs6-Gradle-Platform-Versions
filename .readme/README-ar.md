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

# v1.4.0

###### 2026/08/18

* `إصلاح` لم يعد الحد الأعلى لـ AGP يُخفَّف إذا كان إصدار بيئة التطوير تحديثًا على مستوى التصحيح فحسب، فقد كان IntelliJ IDEA 2026.2.1 يحصل على AGP 9.2.1 فترفضه بيئة التطوير؛ وصار الآن يبقى على خط 9.1 شأنه شأن 2026.2
* `تحسين` إضافة مدخلة 2026.2 إلى جدول مطابقة IntelliJ IDEA، مع أخذ الحد الأعلى لـ AGP مما تبلغ عنه بيئة التطوير نفسها
* `تحسين` تغيير أسلوب الترحيل، فصار إصدار الإضافة يُصرَّح به مرة واحدة في برنامج البناء الجذري دون المساس ببرامج الوحدات؛ إذ كان الأسلوب السابق القائم على إضافة الإصدار في كل وحدة متعذرًا في وحدات Groovy، لأن plugins block فيها لا يقبل إلا القيم النصية الحرفية
* `تحسين` نقل ملاحظات وحدة التحكم إلى فقرة مستقلة أسفل ملخص الإصدارات، فلم تعد تتخلل أسطر الإصدارات

# v1.3.0

###### 2026/08/18

* `تلميح` لم يعد Gradle 8 مدعومًا اعتبارًا من هذا الإصدار، فـ AGP 9.0 هو أول إصدار يشترط Gradle 9، ومن ثم يبدأ نطاق الدعم من AGP 9.0
* `تحسين` حذف المدخلات الأقدم من 9 من جداول التوافق، ولم يُبقَ في جدول مطابقة IntelliJ IDEA إلا المدخلات التي تعطي AGP 9
* `تحسين` إذا كان Gradle الحالي أقدم من جميع مدخلات التوافق فلم يعد يرجع إلى أدنى مدخلة، بل صار يبلغ عن خطأ صريح؛ تفاديًا لوضع إصدار يتعذر تحميله على classpath
* `تحسين` رفع أدنى الإصدارات المدعومة: Gradle 9.1.0 وAndroid Studio 2025.2.3 وIntelliJ IDEA 2026.1.2 وAGP 9.0
* `تحسين` مواءمة شارات README مع الإصدارات المذكورة، وإضافة شارة AGP
* `تحسين` صار برنامج الترحيل يدعم السكر النحوي kotlin(...) والأسماء المختصرة القديمة مثل kotlin-android وkotlin-kapt وkotlin-parcelize، وتُوسَّع الأسماء المختصرة إلى معرّف الإضافة الكامل
* `تحسين` يتخطى برنامج الترحيل نوعين من المستودعات يتعذر ترحيلهما: مستودعات تشير فيها مقاطع البرامج المضمَّنة عبر apply(from=) إلى أنواع AGP، ومستودعات فُعِّل فيها التحقق من التبعيات

# v1.2.0

###### 2026/08/18

* `ميزة` برنامج تحويل برامج الوحدات `.python/migrate_modules.py`، يعيد كتابة تطبيق الإضافات غير المقترن بإصدار إلى الصيغة المقترنة بإصدار، ويؤخذ الإصدار من system property
* `ميزة` نشر إصدار KSP المحدد أيضًا بوصفه system property باسم `gradle.ksp.version`، بما يوافق تسمية AGP وKotlin
* `إصلاح` كان التراجع في برنامج تحويل الوحدات لا يستعيد الملفات الأصلية، ويخلّف وراءه النسخ الاحتياطية
* `تحسين` صار برنامج ترحيل settings يتحقق أولًا من جاهزية برامج الوحدات، فإذا لم تكن جاهزة نبّه دون إعادة كتابة، تفاديًا لترك حالة وسيطة لا يمكن بناؤها
* `تحسين` صار برنامج ترحيل settings يضم الإضافة إلى plugins block الموجود وينقلها قبل `includeBuild`، بدلًا من إضافة كتلة جديدة

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
