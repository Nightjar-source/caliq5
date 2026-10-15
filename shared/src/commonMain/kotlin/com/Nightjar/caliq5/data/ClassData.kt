package com.Nightjar.caliq5.data

import kotlinx.serialization.Serializable

/**
 * Represents a subject/course.
 * optional = true means student can skip it (like Computer, PE, French, etc.)
 */
@Serializable
data class Subject(
    val name: String,
    val optional: Boolean = false,
)

/**
 * All Iraqi school classes and their subjects.
 * Matches the CLASSES object in caliq5.html exactly.
 */
object ClassData {

    private fun opt(name: String) = Subject(name, optional = true)
    private fun req(name: String) = Subject(name, optional = false)

    val CLASSES: Map<String, List<Subject>> = mapOf(
        "الأول المتوسط" to listOf(
            req("التربية الإسلامية"),
            req("اللغة العربية"),
            req("اللغة الإنكليزية"),
            req("الرياضيات"),
            req("الاجتماعيات"),
            req("الاحياء"),
            req("الكيمياء"),
            req("الفيزياء"),
            opt("الحاسوب"),
            opt("اللغة الفرنسية"),
            opt("التربية الأخلاقية"),
            opt("الفنية"),
            opt("الرياضة"),
        ),
        "الثاني المتوسط" to listOf(
            req("التربية الإسلامية"),
            req("اللغة العربية"),
            req("اللغة الإنكليزية"),
            req("الرياضيات"),
            req("الاجتماعيات"),
            req("الاحياء"),
            req("الكيمياء"),
            req("الفيزياء"),
            opt("الحاسوب"),
            opt("اللغة الفرنسية"),
            opt("التربية الأخلاقية"),
            opt("الفنية"),
            opt("الرياضة"),
        ),
        "الرابع الأدبي" to listOf(
            req("التربية الإسلامية"),
            req("اللغة العربية"),
            req("اللغة الإنكليزية"),
            req("الرياضيات"),
            req("التاريخ"),
            req("الجغرافية"),
            req("علم الاجتماع"),
            opt("الحاسوب"),
            opt("اللغة الفرنسية"),
            opt("اللغة الكردية"),
            opt("جرائم البعث"),
            opt("الفنية"),
            opt("الرياضة"),
        ),
        "الرابع العلمي" to listOf(
            req("التربية الإسلامية"),
            req("اللغة العربية"),
            req("اللغة الإنكليزية"),
            req("الرياضيات"),
            req("الكيمياء"),
            req("الفيزياء"),
            req("الأحياء"),
            opt("الحاسوب"),
            opt("اللغة الفرنسية"),
            opt("اللغة الكردية"),
            opt("جرائم البعث"),
            opt("الفنية"),
            opt("الرياضة"),
        ),
        "الخامس الأدبي" to listOf(
            req("التربية الإسلامية"),
            req("اللغة العربية"),
            req("اللغة الإنكليزية"),
            req("الرياضيات"),
            req("التاريخ"),
            req("الجغرافية"),
            req("الاقتصاد"),
            req("الفلسفة"),
            opt("الحاسوب"),
            opt("اللغة الفرنسية"),
            opt("اللغة الكردية"),
            opt("الفنية"),
            opt("الرياضة"),
        ),
        "الخامس العلمي" to listOf(
            req("التربية الإسلامية"),
            req("اللغة العربية"),
            req("اللغة الإنكليزية"),
            req("الرياضيات"),
            req("الاحياء"),
            req("الكيمياء"),
            req("الفيزياء"),
            opt("الحاسوب"),
            opt("اللغة الفرنسية"),
            opt("اللغة الكردية"),
            opt("الفنية"),
            opt("الرياضة"),
        ),
    )

    val classNames: List<String> get() = CLASSES.keys.toList()

    fun getSubjects(className: String): List<Subject> =
        CLASSES[className] ?: emptyList()
}
