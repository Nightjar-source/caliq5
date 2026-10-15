package com.Nightjar.caliq5.ui.result

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import com.Nightjar.caliq5.ui.components.GradeDetailRow
import com.Nightjar.caliq5.ui.components.MainGradeBox
import com.Nightjar.caliq5.ui.components.ResultCard
import com.Nightjar.caliq5.viewmodel.ResultDisplayState
import com.Nightjar.caliq5.theme.successColor

@Composable
fun ExemptionResultContent(state: ResultDisplayState.ExemptionResult) {
    val data = state.data

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Main grade box (Fixed: matches web .main-grade-box)
        MainGradeBox(
            title = "المعدل العام للسعي السنوي",
            grade = "${com.Nightjar.caliq5.ui.components.formatDecimalGrade(data.finalAverage)}%",
            subtitle = "المعدل العام لا يخضع لقاعدة جبر الكسور (85% هو الحد الأدنى)",
        )

        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.widthIn(min = 280.dp).weight(1f)) {
                // Exemption status card
                ResultCard(title = "حالة الإعفاء${if (data.className.isNotEmpty()) " (${data.className})" else ""}") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = data.exemptionStatus.primary,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (data.exemptionStatus.isGeneralExempt) successColor
                                        else com.Nightjar.caliq5.theme.errorColor,
                            ),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (data.exemptionStatus.secondary.isNotEmpty()) {
                            Text(
                                text = data.exemptionStatus.secondary,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = successColor,
                                ),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    // Exemption Rules Explanation
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "ضوابط الإعفاء:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "• الإعفاء العام: معدل >= 85% وجميع المواد بسعي >= 75%.",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "• الإعفاء الفردي: درجة المادة >= 90%.",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Individual exemptions card (90%+)
            if (data.individualExemptions.isNotEmpty()) {
                Box(modifier = Modifier.widthIn(min = 280.dp).weight(1f)) {
                    ResultCard(
                        title = "مواد الإعفاء الفردي (90% فأكثر)"
                    ) {
                        data.individualExemptions.forEach { grade ->
                            GradeDetailRow(
                                subjectName = grade.name,
                                grade = grade.grade,
                                originalGrade = grade.original,
                                isRounded = grade.isRounded,
                                isSkipped = grade.isSkipped,
                                showExemptionLabels = true
                            )
                        }
                    }
                }
            }
        }

        // All grades detail card with smooth expansion animation
        var expanded by rememberSaveable { mutableStateOf(true) }
        val arrowAngle by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (expanded) 180f else 0f,
            animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.65f, stiffness = 450f),
            label = "ex_details_arrow"
        )
        val arrowScale by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (expanded) 1.15f else 1.0f,
            animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.65f, stiffness = 450f),
            label = "ex_details_arrow_scale"
        )
        ResultCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { expanded = !expanded }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = arrowAngle
                        scaleX = arrowScale
                        scaleY = arrowScale
                    }
                )
                Text(
                    text = "تفاصيل درجات السعي السنوي",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    ),
                    textAlign = TextAlign.End
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                modifier = Modifier.fillMaxWidth().clipToBounds(),
                enter = expandVertically(
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 260,
                        easing = androidx.compose.animation.core.CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
                    )
                ) + fadeIn(animationSpec = androidx.compose.animation.core.tween(180)),
                exit = shrinkVertically(
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 240,
                        easing = androidx.compose.animation.core.CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
                    ),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 200,
                        easing = androidx.compose.animation.core.LinearEasing
                    )
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth().clipToBounds()) {
                    data.grades.forEach { grade ->
                        GradeDetailRow(
                            subjectName = grade.name,
                            grade = grade.grade,
                            originalGrade = grade.original,
                            isRounded = grade.isRounded,
                            isSkipped = grade.isSkipped,
                            showExemptionLabels = true
                        )
                    }
                }
            }
        }
    }
}
