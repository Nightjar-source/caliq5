package com.Nightjar.caliq5

/**
 * خدمة وظائف التطبيق لوكلاء الذكاء الاصطناعي (Android 16+ AppFunctions API - October 2026).
 * 
 * تحوّل هذه الخدمة تطبيق Caliq5 إلى خادم أدوات ذكي محلي (Local MCP / Agent Tool Service)،
 * مما يُمكّن وكلاء الذكاء الاصطناعي على الجهاز (مثل Google Gemini Nano ومساعدات النظام) 
 * من استدعاء الوظائف الحسابية لمنظومة التعليم العراقية مباشرة في الخلفية وبأمان تام.
 */
class CaliqAppFunctionService {

    /**
     * حساب الإعفاء العام والفردي لطلبة المدارس العراقية.
     * 
     * معايير وزارة التربية العراقية:
     * - الإعفاء الفردي: أن لا تقل درجة السعي السنوي للمادة عن 90.
     * - الإعفاء العام: أن لا تقل درجة أي مادة عن 75، وأن يكون المعدل العام لكافة المواد 85 فأكثر.
     *
     * @param grades قائمة درجات السعي السنوي لكافة المواد الدراسية.
     * @return نتيجة الإعفاء متضمنة نوع الإعفاء (عام، فردي، أو غير مشمول) مع المعدل العام والمواد المستثناة.
     */
    suspend fun calculateExemption(grades: List<Double>): ExemptionSummary {
        if (grades.isEmpty()) {
            return ExemptionSummary(isGeneralExempt = false, average = 0.0, individualExemptCount = 0, message = "لا توجد درجات مدخلة")
        }

        val average = grades.average()
        val individualExemptCount = grades.count { it >= 90.0 }
        val allAboveMinimum = grades.all { it >= 75.0 }
        val isGeneralExempt = (average >= 85.0) && allAboveMinimum

        val message = when {
            isGeneralExempt -> "مبارك! مشمول بالإعفاء العام لكافة المواد (المعدل: %.2f)".format(average)
            individualExemptCount > 0 -> "مشمول بالإعفاء الفردي في $individualExemptCount مواد (المعدل: %.2f)".format(average)
            else -> "غير مشمول بالإعفاء (المعدل: %.2f)".format(average)
        }

        return ExemptionSummary(
            isGeneralExempt = isGeneralExempt,
            average = average,
            individualExemptCount = individualExemptCount,
            message = message
        )
    }

    /**
     * حساب معدل السعي السنوي لطلبة المرحلة المتوسطة والإعدادية.
     *
     * المعادلة المعتمدة:
     * السعي السنوي = (معدل الفصل الأول + درجة نصف السنة + معدل الفصل الثاني) / 3
     *
     * @param term1Average معدل الفصل الدراسي الأول.
     * @param midYearGrade درجة امتحان نصف السنة.
     * @param term2Average معدل الفصل الدراسي الثاني.
     * @return معدل السعي السنوي النهائي مقرباً لأقرب منزلة عشرية.
     */
    suspend fun calculateAnnualAverage(
        term1Average: Double,
        midYearGrade: Double,
        term2Average: Double
    ): Double {
        val total = term1Average + midYearGrade + term2Average
        return Math.round((total / 3.0) * 10.0) / 10.0
    }

    /**
     * حساب المعدل الفصلي من درجات الامتحانات الشهرية ونشاط الطالب.
     *
     * @param month1Grade درجة الشهر الأول.
     * @param month2Grade درجة الشهر الثاني.
     * @param dailyPerformance درجة النشاط اليومي والشفهي (اختياري).
     * @return المعدل الفصلي للمادة.
     */
    suspend fun calculateMonthlyAverage(
        month1Grade: Double,
        month2Grade: Double,
        dailyPerformance: Double? = null
    ): Double {
        return if (dailyPerformance != null) {
            val total = month1Grade + month2Grade + dailyPerformance
            Math.round((total / 3.0) * 10.0) / 10.0
        } else {
            val total = month1Grade + month2Grade
            Math.round((total / 2.0) * 10.0) / 10.0
        }
    }

    /**
     * حساب الدرجة المطلوبة في الفصل الثاني لتحقيق درجة سعي سنوي محددة مسبقاً (مثل 90 للإعفاء أو 50 للنجاح).
     *
     * المعادلة العكسية:
     * الدرجة المطلوبة = (السعي المستهدف * 3) - معدل الفصل الأول - درجة نصف السنة.
     *
     * @param term1Average معدل الفصل الأول.
     * @param midYearGrade درجة امتحان نصف السنة.
     * @param targetAnnualGrade درجة السعي السنوي المطلوبة (افتراضياً 90 للإعفاء الفردي).
     * @return الدرجة الواجب تحصيلها في الفصل الثاني.
     */
    suspend fun calculateTerm2RequiredGrade(
        term1Average: Double,
        midYearGrade: Double,
        targetAnnualGrade: Double = 90.0
    ): Double {
        val required = (targetAnnualGrade * 3.0) - (term1Average + midYearGrade)
        return Math.max(0.0, Math.min(100.0, Math.round(required * 10.0) / 10.0))
    }
}

/**
 * كائن نتيجة حساب الإعفاء المعروض لوكيل الذكاء الاصطناعي (Schema Output).
 */
data class ExemptionSummary(
    val isGeneralExempt: Boolean,
    val average: Double,
    val individualExemptCount: Int,
    val message: String
)
