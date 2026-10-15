package com.Nightjar.caliq5.ui.result

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Nightjar.caliq5.data.TermMode
import com.Nightjar.caliq5.ui.components.ResultCard
import com.Nightjar.caliq5.ui.components.formatDecimalGrade
import com.Nightjar.caliq5.ui.components.parseGradeToDouble
import com.Nightjar.caliq5.viewmodel.ResultDisplayState
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun StudentProfileResultContent(state: ResultDisplayState.StudentProfileResult) {
    val s = state.student
    val isDark = isSystemInDarkTheme()
    
    val predefinedList = if (s.className == "صف آخر (مخصص)") emptyList() else com.Nightjar.caliq5.data.ClassData.getSubjects(s.className)
    val nonSkippedSubjects = s.subjects.filter { (name, r) ->
        val isOpt = predefinedList.find { it.name == name }?.optional == true
        !(isOpt && r.t1Val == "SKIPPED")
    }

    var totalSaei = 0.0
    var countSaei = 0
    var hasBelow75 = false
    nonSkippedSubjects.forEach { (_, r) ->
        val t1Val = if (r.t1Mode == TermMode.TERM) r.t1Val.parseGradeToDouble() else {
            val gr = r.t1Months.mapNotNull { it.parseGradeToDouble() }
            if (gr.isNotEmpty()) gr.average() else null
        }
        val midVal = r.midVal.parseGradeToDouble()
        val t2Val = if (r.t2Mode == TermMode.TERM) r.t2Val.parseGradeToDouble() else {
            val gr = r.t2Months.mapNotNull { it.parseGradeToDouble() }
            if (gr.isNotEmpty()) gr.average() else null
        }
        if (t1Val != null && midVal != null && t2Val != null) {
            val num = (t1Val + midVal + t2Val) / 3.0
            val fl = floor(num)
            val dec = num - fl
            val saei = when {
                dec >= 0.6 -> ceil(num)
                num >= 49.6 && num < 50.0 -> 50.0
                num >= 74.6 && num < 75.0 -> 75.0
                num >= 89.6 && num < 90.0 -> 90.0
                else -> num
            }
            totalSaei += saei
            countSaei++
            if (saei < 75.0) hasBelow75 = true
        }
    }
    val overallAverage = if (countSaei > 0) totalSaei / countSaei else 0.0
    val isGenExempt = countSaei > 0 && countSaei == nonSkippedSubjects.size && overallAverage >= 85.0 && !hasBelow75

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 300,
                    easing = androidx.compose.animation.core.CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
                )
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (countSaei > 0) {
            val overallRoundingNote = when {
                overallAverage >= 84.6 && overallAverage < 85.0 -> "المعدل قبل الجبر (${formatDecimalGrade(overallAverage)}%) - تم جبر الكسر إلى 85% للإعفاء العام"
                overallAverage >= 74.6 && overallAverage < 75.0 -> "المعدل قبل الجبر (${formatDecimalGrade(overallAverage)}%) - تم جبر الكسر إلى 75%"
                (overallAverage - floor(overallAverage)) >= 0.6 -> "المعدل قبل الجبر (${formatDecimalGrade(overallAverage)}%) - تم جبر الكسر إلى ${ceil(overallAverage).toInt()}%"
                else -> null
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                com.Nightjar.caliq5.ui.components.MainGradeBox(
                    title = "معدل السعي السنوي العام (${s.name})",
                    grade = "${formatDecimalGrade(overallAverage)}%",
                    subtitle = "الصف: ${s.className}"
                )
                if (overallRoundingNote != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 ملاحظة المعدل العام: $overallRoundingNote",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        if (isGenExempt) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.14f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = com.Nightjar.caliq5.theme.successColor, modifier = Modifier.size(26.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "مبروك ✨ 🎉 الطالب معفى إعفاء عام في جميع الدروس لهذا العام ونجحت الى صفك التالي..",
                            fontWeight = FontWeight.Bold,
                            color = com.Nightjar.caliq5.theme.successColor,
                            fontSize = 14.5.sp,
                            lineHeight = 21.sp
                        )
                        Text(
                            "تم تحقيق شروط الإعفاء العام لجميع المواد بمعدل سعي ${formatDecimalGrade(overallAverage)}%.",
                            color = com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        ResultCard(title = "معلومات السجل") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "اسم الطالب",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Normal
                            )
                        )
                        Text(
                            text = s.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "المرحلة والصف الدراسي",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Normal
                            )
                        )
                        Text(
                            text = s.className,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.40f else 0.50f),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.30f else 0.40f)
            ),
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "تفاصيل السعي والدرجات الشاملة",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                s.subjects.entries.forEachIndexed { index, (subName, r) ->
                    val isOptional = predefinedList.find { it.name == subName }?.optional == true
                    val isSkipped = isOptional && r.t1Val == "SKIPPED"

                    val t1Val = if (isSkipped) null else if (r.t1Mode == TermMode.TERM) r.t1Val.parseGradeToDouble() else {
                        val gr = r.t1Months.mapNotNull { it.parseGradeToDouble() }
                        if (gr.isNotEmpty()) gr.average() else null
                    }
                    val midVal = if (isSkipped) null else r.midVal.parseGradeToDouble()
                    val t2Val = if (isSkipped) null else if (r.t2Mode == TermMode.TERM) r.t2Val.parseGradeToDouble() else {
                        val gr = r.t2Months.mapNotNull { it.parseGradeToDouble() }
                        if (gr.isNotEmpty()) gr.average() else null
                    }
                    
                    val saeiRaw = if (t1Val != null && midVal != null && t2Val != null) (t1Val + midVal + t2Val) / 3.0 else null
                    val saeiRounded = saeiRaw?.let { num ->
                        val fl = floor(num)
                        val dec = num - fl
                        when {
                            dec >= 0.6 -> ceil(num)
                            num >= 49.6 && num < 50.0 -> 50.0
                            num >= 74.6 && num < 75.0 -> 75.0
                            num >= 89.6 && num < 90.0 -> 90.0
                            else -> num
                        }
                    }
                    val isIndivExempt = saeiRounded != null && saeiRounded >= 90.0
                    val isExempt = isGenExempt || isIndivExempt

                    val finalExamVal = r.finalVal.parseGradeToDouble()
                    val finalGrade = if (isExempt) {
                        saeiRounded
                    } else if (finalExamVal != null && saeiRounded != null) {
                        (saeiRounded + finalExamVal) / 2.0
                    } else null

                    val roundingNote = when {
                        saeiRaw == null -> null
                        saeiRaw >= 74.6 && saeiRaw < 75.0 -> "المعدل قبل الجبر (${formatDecimalGrade(saeiRaw)}%) - تم جبر الكسر إلى 75% للإعفاء"
                        saeiRaw >= 89.6 && saeiRaw < 90.0 -> "المعدل قبل الجبر (${formatDecimalGrade(saeiRaw)}%) - تم جبر الكسر إلى 90% للإعفاء الفردي"
                        saeiRaw >= 49.6 && saeiRaw < 50.0 -> "المعدل قبل الجبر (${formatDecimalGrade(saeiRaw)}%) - تم جبر الكسر إلى 50% للنجاح"
                        (saeiRaw - floor(saeiRaw)) >= 0.6 -> "المعدل قبل الجبر (${formatDecimalGrade(saeiRaw)}%) - تم جبر الكسر إلى ${ceil(saeiRaw).toInt()}%"
                        else -> null
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    subName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExempt) com.Nightjar.caliq5.theme.successColor else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSkipped) {
                                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                                        Text("لا تشملني", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isExempt) {
                                    Surface(color = com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
                                        Text(
                                            if (isGenExempt) "معفى عام" else "إعفاء فردي",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = com.Nightjar.caliq5.theme.successColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = if (isSkipped) "اختيارية" else saeiRounded?.let { "${formatDecimalGrade(it)}%" } ?: "غير مكتمل",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isExempt) com.Nightjar.caliq5.theme.successColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (!isSkipped) {
                            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.88f else 0.94f))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.90f else 0.95f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("معدل الفصل الأول", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                            Spacer(Modifier.height(2.dp))
                                            Text(t1Val?.let { formatDecimalGrade(it) } ?: "-", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("معدل نصف السنة", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                            Spacer(Modifier.height(2.dp))
                                            Text(midVal?.let { formatDecimalGrade(it) } ?: r.midVal.ifEmpty { "-" }, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("معدل الفصل الثاني", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                            Spacer(Modifier.height(2.dp))
                                            Text(t2Val?.let { formatDecimalGrade(it) } ?: "-", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("السعي", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(2.dp))
                                            Text(saeiRounded?.let { formatDecimalGrade(it) } ?: "-", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isExempt) com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("الامتحان النهائي", fontSize = 11.sp, color = if (isExempt) com.Nightjar.caliq5.theme.successColor else MaterialTheme.colorScheme.secondary, fontWeight = if (isExempt) FontWeight.Bold else FontWeight.Normal)
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                if (isExempt) "معفى" else if (r.finalVal.isNotEmpty()) r.finalVal else "-",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isExempt) com.Nightjar.caliq5.theme.successColor else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isExempt) com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("الدرجة النهائية", fontSize = 11.sp, color = if (isExempt) com.Nightjar.caliq5.theme.successColor else MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                finalGrade?.let { formatDecimalGrade(it) } ?: "-",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = if (isExempt) com.Nightjar.caliq5.theme.successColor else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            if (roundingNote != null) {
                                Text(
                                    text = roundingNote,
                                    fontSize = 10.5.sp,
                                    color = com.Nightjar.caliq5.theme.successColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            if ((r.t1Mode == TermMode.MONTHLY && r.t1Months.isNotEmpty()) || (r.t2Mode == TermMode.MONTHLY && r.t2Months.isNotEmpty())) {
                                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    if (r.t1Mode == TermMode.MONTHLY && r.t1Months.isNotEmpty()) {
                                        Text("أشهر فصل 1: ${r.t1Months.joinToString("، ")}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Start)
                                    }
                                    if (r.t2Mode == TermMode.MONTHLY && r.t2Months.isNotEmpty()) {
                                        Text("أشهر فصل 2: ${r.t2Months.joinToString("، ")}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Start)
                                    }
                                }
                            }
                        }
                    }
                    
                    if (index < s.subjects.size - 1) {
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}
