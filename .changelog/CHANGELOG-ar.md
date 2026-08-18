******

### اللغات (Languages)

******

يدعم ملف CHANGELOG.md حاليًا اللغات التالية:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Gradle-Platform-Versions/blob/master/.changelog/CHANGELOG-ru.md)
- العربية [ar] # الحالي

******

### سجل الإصدارات

******

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
