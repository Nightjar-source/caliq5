# دليل معايير الأداء وهندسة العتاد - Caliq5 Performance & Architecture Guide (2026 / 2027)

هذا الدليل يمثل المرجع الهندسي الشامل لمعايير الأداء، وإدارة الذاكرة، والتوافق العتادي الصارم لتطبيق **Caliq5** لضمان أعلى سرعة (120 FPS) مع تأثير صفري على البطارية والمعالج.

---

## 1. خوارزمية فحص وتصنيف أداء الأجهزة (`rememberIsHighPerformanceDevice`)

تعتمد الدالة على **منظومة فلترة عتادية متتالية (Strict Multi-Stage Pipeline)** تفحص عتاد الهاتف وإصدار النظام بشكل مباشر ودقيق:

```kotlin
@Composable
actual fun rememberIsHighPerformanceDevice(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        if (activityManager == null) return@remember true
        
        // 1. فحص تصنيف أندرويد الرسمي (Android Go Edition)
        if (activityManager.isLowRamDevice) return@remember false
        
        // 2. الفحص العتادي الفعلي للذاكرة العشوائية (RAM Measurement)
        // يعمل على جميع إصدارات أندرويد (بما في ذلك Android 15, 16, 17+)
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        if (totalRamGb <= 3.2) return@remember false // الأجهزة الاقتصادية ذات 2GB أو 3GB رام
        
        // 3. فحص عدد أنوية المعالج (CPU Cores)
        val cores = Runtime.getRuntime().availableProcessors()
        if (cores <= 4 && totalRamGb < 4.0) return@remember false

        // 4. فحص إصدار نظام التشغيل (OS Version Filter)
        // أندرويد 9 (API 28) وأقدم تفتقر لتعريفات Skia GPU الحديثة
        Build.VERSION.SDK_INT >= 29
    }
}
```

### 📊 جدول حالات الفحص وقرار النظام:
| حالة الهاتف والعتاد | قرار الدالة | السلوك الهندسي المطبق |
| :--- | :--- | :--- |
| **أندرويد 9 (أو أقدم)** + رام 8GB | ⛔ `false` (خفيف) | تعطيل الحسابات الثقيلة لحماية كرت الشاشة القديم من الانهيار |
| **أندرويد 17 (حديث)** + رام 2GB / 3GB | ⛔ `false` (خفيف) | التبديل للواجهة المسطحة الخفيفة لحفظ الذاكرة المحدودة |
| **أندرويد 10 إلى 17+** + رام 4GB فأعلى | 🚀 `true` (قوي) | تفعيل كامل مؤثرات الـ 3D الزجاجية والأنيميشن السائل |

---

## 2. توافق معدل التحديث والهرتز التلقائي (60Hz / 90Hz / 120Hz / LTPO)

* التطبيق متكامل مع **Android Choreographer & HardwareRenderer**:
  * **شاشات 60Hz:** استقرار كامل عند 60 إطار بالثانية بدون هبوط بالفريمات.
  * **شاشات 90Hz / 120Hz / 144Hz:** سلاسة وانسيابية سائلة مع المؤثرات الزنبركية (`Fluid Springs`).
  * **شاشات LTPO التكيفية:** يرتفع التردد تلقائياً عند اللمس إلى 120Hz، ويهبط إلى 1Hz/10Hz عند السكون لتوفير الطاقة.

---

## 3. هندسة الرسم المخبأ وانعدام استهلاك المعالج (`Zero Idle CPU`)

1. **الرسم المخبأ في كرت الشاشة (`drawWithCache`):**
   * كافة الإطارات ثلاثية الأبعاد (3D Glassmorphism) واللمعات تُحسب وتُخزن في ذاكرة الـ GPU مباشرة عبر `CacheDrawScope` ولا يعاد رسمها إلا عند تغير حجم العنصر.
2. **التحريك عبر العتاد الرسومي (`graphicsLayer`):**
   * التحجيم (`scaleX`/`scaleY`) والانزياح (`translationY`/`offset`) يتم داخل معالج الرسوميات (Compositor Thread) بدون أي إعادة قياس لأبعاد الواجهة (`No Re-layouts`).
3. **انعدام الفحص الدوري في الخلفية (No Polling Loops):**
   * لا توجد أي حلقات `while(true)` في التطبيق. استهلاك المعالج عند سكون الشاشة هو **0.0%**.

---

## 4. دورة حياة إدارة الذاكرة والتنظيف (`onTrimMemory`)

يتعامل التطبيق بصرامة مع إشارات الذاكرة الصادرة من النظام:
* **`TRIM_MEMORY_UI_HIDDEN`:** عند خروج المستخدم للواجهة الرئيسية، يتم فوراً إيقاف التحديثات وإلغاء أي رسومات غير مرئية.
* **`TRIM_MEMORY_BACKGROUND`:** عند بقاء التطبيق في الخلفية، يتم تفريغ المخازن المؤقتة لملفات الـ PDF والصور، مع بقاء بيانات المستخدم بأمان في قاعدة بيانات Room.

---

## 5. الامتثال لمعايير جوجل لعام 2026/2027 (Google Play Vitals)

* **وضع السكون التام (Doze Mode):** لا توجد أي `WakeLocks` تمنع الهاتف من النوم العميق.
* **ملء الشاشة الحقيقي (Edge-to-Edge):** توافق كامل مع `WindowInsets` بدون حواف سوداء.
* **الرجوع التنبئي (Predictive Back):** مفعل رسمياً عبر `enableOnBackInvokedCallback="true"`.
* **الحفظ الآمن (Scoped Storage):** تصدير عبر `MediaStore` بدون طلب أذونات خطيرة.

