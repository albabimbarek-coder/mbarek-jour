# خطة تنفيذ: إصلاح تصدير تقارير PDF وإكسل وضبط معامل الإضافي الثاني 1.0x

تهدف هذه الخطة إلى حل مشاكل تصدير ملفات التقارير كـ PDF و Excel (CSV)، من خلال تصحيح تكوينات FileProvider وصلاحيات URI، وتوفير نافذة تفاعلية تتيح للمستخدم إما مشاركة الملف، أو حفظه مباشرة في مجلد التنزيلات (Downloads)، أو فتحه في التطبيق المناسب، بالإضافة إلى ضبط معامل الساعة الإضافية الثانية ليكون 1.0x كخيار افتراضي ضمن الخيارات (1.0x, 1.25x, 1.5x, 1.75x, 2.0x, 2.5x).

---

## مراجعة المستخدم والقرارات المؤكدة (User Review & Confirmed Decisions)

> [!IMPORTANT]
> تم تأكيد تفضيلات المستخدم خلال مرحلة الأسئلة التوضيحية وتثبيتها كالتالي:

- **آلية تصدير الملفات (PDF / Excel)**:
  - توفير خيارات شاملة عند الضغط على التصدير:
    1. **مشاركة الملف (Share Sheet)** مع ضمان منح صلاحيات القراءة التامة لكافة التطبيقات عبر FileProvider و ClipData.
    2. **حفظ في مجلد التنزيلات (Downloads)** باستخدام MediaStore API على أندرويد الحديث، مع إشعار بنجاح الحفظ وتحديد مسار الملف.
    3. **فتح الملف مباشرة (Open / View)** عبر التطبيقات المثبتة الداعمة لصيغ PDF أو Excel/CSV (مثل Sheets أو Office أو قارئ PDF).
- **خيارات الساعة الإضافية الثانية (OT2 Multiplier)**:
  - القيمة الافتراضية في قاعدة البيانات ونموذج الإعدادات تصبح **1.0x** (ساعة بساعة).
  - قائمة الخيارات المتاحة للمستخدم في شاشة الإعدادات المالية: `[1.0x, 1.25x, 1.5x, 1.75x, 2.0x, 2.5x]`.

---

## 1. نظرة عامة والمفهوم الأساسي (Overview & Core Concept)

- **ماذا يقدم الحل**:
  - معالجة جذرية لخطأ `FileProvider` الناجم عن تكرار أسماء المسارات في `res/xml/file_paths.xml` وغياب `ClipData` المانحة لتفويض القراءة للتطبيقات الخارجية.
  - تجربة تصدير متكاملة تقدم للمستخدم نافذة منبثقة أنيقة (BottomSheet أو Modal Dialog) عند الضغط على تصدير PDF أو Excel تمنحه حرية الاختيار بين: (مشاركة، حفظ بالتنزيلات، فتح فوري).
  - ضبط معامل الإضافي الثاني ليبدأ من 1.0x كافتراضي ليتماشى مع متطلبات العمل وساعات التعويض المباشرة.
- **الفئة المستهدفة**: الموظفون والعمال والمهنيون الذين يعتمدون على التطبيق لتوثيق أوقات عملهم وتقديم تقارير مالية رسمية إلى الإدارة أو الاحتفاظ بنسخ محلية في ملفات الجهاز.
- **القيمة المضافة**: موثوقية كاملة في إنشاء وتصدير ومشاركة المستندات دون أعطال أو حجب صلاحيات، وتنوع مرن في حساب الساعات الإضافية الثانية.

---

## 2. تجربة المستخدم والتصميم البصري (User Experience & Visual Design)

### مسارات الاستخدام الرئيسية (Key User Flows)

```
[شاشة التقارير] ──▶ [الضغط على "تصدير كملف PDF" أو "تصدير كملف Excel"]
      │
      ▼
[توليد الملف بنجاح وتجهيز URI الآمن]
      │
      ▼
[ظهور نافذة خيارات التصدير (Export Options Dialog / Sheet)]
      ├──▶ [خيار 1: مشاركة الملف (Share)] ──▶ فتح قائمة التطبيقات (WhatsApp, Gmail, Drive...)
      ├──▶ [خيار 2: حفظ في التنزيلات (Save)] ──▶ نسخ الملف إلى Downloads وإظهار إشعار تأكيد
      └──▶ [خيار 3: فتح الملف (Open)] ──▶ تشغيل عارض PDF أو جدول البيانات مباشرة
```

### التصميم البصري وعناصر الواجهة (Visual Identity & Theme)
- **ألوان وهوية التصميم**:
  - بطاقة تقرير PDF: لون مميز بالوردي الأنيق (`SickRose` / تدرج أحمر المستندات).
  - بطاقة تقرير Excel: لون مميز بالأخضر المالي (`WorkDayGreen` / تدرج الجداول).
  - خيارات الإضافي الثاني: إبراز الزر النشط بلون العنبر/البرتقالي الدافئ (`#D97706`).
- **نافذة خيارات الملف المصدّر (Export Action Sheet)**:
  - تظهر في أسفل الشاشة أو كبطاقة حوارية مركزية تضم أيقونات واضحة لكل إجراء:
    - أيقونة مشاركة (`Icons.Default.Share`)
    - أيقونة تنزيل (`Icons.Default.FileDownload` / `DownloadForOffline`)
    - أيقونة فتح المستند (`Icons.Default.OpenInNew`)
  - رسائل تنبيهية لطيفة ومحسوسة (Snackbar / Status Surface) تؤكد حفظ الملف في مسار التنزيلات وتتيح فتحه مباشرة.

---

## 3. القرارات التقنية والمفاضلات (Key Product Decisions & Trade-Offs)

1. **إصلاح FileProvider ومسارات الملفات**:
   - *القرار*: تحديث `res/xml/file_paths.xml` بأسماء مسارات فريدة لا تتعارض (مثل `name="cache_reports"` و `name="files_reports"` و `name="cache_all"`).
   - *السبب*: في أندرويد، تكرار السمة `name="reports"` داخل `file_paths.xml` يؤدي إلى مسح مسار الكاش من خريطة الـ FileProvider واستبداله، مما ينتج عنه استثناء `IllegalArgumentException: Failed to find configured root`.
   - *التفويض*: تضمين `clipData` وإضافة راية `FLAG_GRANT_READ_URI_PERMISSION` في كل من الـ Intent الأساسي ومُنشئ الـ Chooser لمنع استثناء `SecurityException: Permission Denial`.

2. **دعم الحفظ في مجلد التنزيلات المعتمد (MediaStore Downloads)**:
   - *القرار*: إضافة دالة `saveFileToDownloads(context, file, mimeType, displayName)` في `ExportHelper` تستخدم `MediaStore.Downloads` على أندرويد 10+ (API 29+) والنسخ المباشر على الإصدارات الأقدم، دون طلب صلاحيات تخزين زائدة ومحظورة من سياسات جوجل بلاي.
   - *السبب*: ضمان امتثال التطبيق الصارم لسياسات تخزين أندرويد الحديثة (Scoped Storage) وعدم طلب `WRITE_EXTERNAL_STORAGE`.

3. **قائمة معاملات الإضافي الثاني**:
   - *القرار*: تحديث `FinancialSettings.kt` ليجعل القيمة الافتراضية `overtimeMultiplier2 = 1.0`، وتحديث القائمة في `FinancesScreen.kt` لتضمين `1.0, 1.25, 1.5, 1.75, 2.0, 2.5`.

---

## 4. البنية التقنية وهيكل المكونات (Technical Architecture & Data Strategy)

```
┌────────────────────────────────────────────────────────┐
│                   Data & Settings Layer                │
│  - FinancialSettings (overtimeMultiplier2: 1.0 default)│
│  - WorkTrackerViewModel (state & settings updates)     │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│                   Export Engine                        │
│  ExportHelper:                                         │
│   ├── exportToPdf(...) -> File (UTF-8, styled table)  │
│   ├── exportToCsv(...) -> File (UTF-8 with BOM)       │
│   ├── getShareUri(...) -> Content URI via FileProvider │
│   ├── createShareIntent(...) with ClipData & Flags     │
│   ├── createViewIntent(...) for direct document opening│
│   └── saveToDownloads(...) Scoped Storage MediaStore   │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│                   UI Presentation                      │
│  ReportsScreen:                                        │
│   ├── ExportActionCard (PDF / Excel)                   │
│   ├── ExportOptionsDialog / BottomSheet                │
│   └── Status Feedback (Snackbar / Banner)              │
│  FinancesScreen:                                       │
│   └── OT2 Multipliers Row: [1.0x, 1.25x, 1.5x, 2.0x..] │
└────────────────────────────────────────────────────────┘
```

### الملفات المستهدفة للتعديل عند التنفيذ:
1. `app/src/main/res/xml/file_paths.xml`: تصحيح أسماء المسارات وتفادي أي تكرار.
2. `app/src/main/java/com/example/util/ExportHelper.kt`:
   - إضافة دوال الحفظ في مجلد التنزيلات (`saveToDownloads`).
   - إضافة دالة فتح الملف المباشر (`createViewIntent`).
   - ضبط تفويض قراءة الـ URI بواسطة `ClipData` و `FLAG_GRANT_READ_URI_PERMISSION`.
   - معالجة أي أخطاء وتقديم تسجيل دقيق.
3. `app/src/main/java/com/example/ui/screens/ReportsScreen.kt`:
   - إضافة نافذة حوارية / أزرار إجراءات التصدير (مشاركة، حفظ، فتح).
   - عرض رسائل تأكيد فورية واضحة باللغة المختارة.
4. `app/src/main/java/com/example/data/model/FinancialSettings.kt`:
   - ضبط `overtimeMultiplier2` لتكون قيمته الافتراضية `1.0`.
5. `app/src/main/java/com/example/ui/screens/FinancesScreen.kt`:
   - إضافة `1.0` إلى قائمة مضاعفات الإضافي الثاني وجعل `1.0x` خياراً متاحاً وواضحاً ومحدد كافتراضي.
6. `app/src/main/java/com/example/util/AppStrings.kt`:
   - إضافة نصوص الخيارات الجديدة: "فتح الملف"، "حفظ في التنزيلات"، "تم الحفظ في التنزيلات بنجاح".
