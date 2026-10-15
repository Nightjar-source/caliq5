package com.Nightjar.caliq5.ui.result

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.Nightjar.caliq5.ui.components.GradeDetailRow
import com.Nightjar.caliq5.ui.components.MainGradeBox
import com.Nightjar.caliq5.ui.components.ResultCard
import com.Nightjar.caliq5.viewmodel.ResultDisplayState
import kotlin.math.floor

@Composable
fun MonthlyResultContent(state: ResultDisplayState.MonthlyResult) {
    val data = state.data

    val hasFraction = kotlin.math.abs(data.realAverage - kotlin.math.floor(data.realAverage)) >= 0.01
    val showRounded = hasFraction && (data.finalAverage.toDouble() != data.realAverage)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MainGradeBox(
            title = "المعدل الفصلي${if (data.subjectName.isNotEmpty()) " لمادة ${data.subjectName}" else ""}",
            grade = "${com.Nightjar.caliq5.ui.components.formatDecimalGrade(data.realAverage)}%",
            subtitle = if (showRounded) "المعدل المقرب: ${data.finalAverage}%" else "",
        )

        ResultCard(title = "النتيجة النهائية بعد الجبر") {
            val wasRounded = data.finalAverage > kotlin.math.floor(data.realAverage)
            val message = when {
                !hasFraction -> "المعدل تام وبدون كسور عشرية وهو ${data.finalAverage}%."
                wasRounded -> "🎉 تهانينا! تم جبر المعدل للأعلى إلى ${data.finalAverage}% لأن الكسر العشري كان 0.6 أو أكثر."
                else -> "المعدل النهائي هو ${data.finalAverage}%. لم يتم جبره للأعلى لأن الكسر العشري أقل من 0.6."
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )

            var isExpanded by rememberSaveable { mutableStateOf(state.isExpanded) }
            val arrowAngle by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
                label = "monthly_arrow_angle"
            )
            val arrowScale by animateFloatAsState(
                targetValue = if (isExpanded) 1.15f else 1.0f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
                label = "monthly_arrow_scale"
            )

            if (data.grades.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                isExpanded = !isExpanded
                                state.isExpanded = isExpanded
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تفاصيل المعادلة وطريقة الحساب",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "تفاصيل",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.graphicsLayer {
                                rotationZ = arrowAngle
                                scaleX = arrowScale
                                scaleY = arrowScale
                            }
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        modifier = Modifier.fillMaxWidth().clipToBounds(),
                        enter = expandVertically(
                            animationSpec = tween(
                                durationMillis = 260,
                                easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
                            )
                        ) + fadeIn(animationSpec = tween(180)),
                        exit = shrinkVertically(
                            animationSpec = tween(
                                durationMillis = 240,
                                easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
                            ),
                            shrinkTowards = Alignment.Top
                        ) + fadeOut(
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = LinearEasing
                            )
                        )
                    ) {
                        val gradesJoined = data.grades.joinToString(" + ") { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it.grade, maxDecimals = 6) }
                        val avgStr = com.Nightjar.caliq5.ui.components.formatDecimalGrade(data.realAverage, maxDecimals = 4)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "طريقة الحساب: مجموع الأشهر مقسوماً على عددها (${data.grades.size}).",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = "المعادلة: ($gradesJoined) ÷ ${data.grades.size} = $avgStr%",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }
                    }
                }
            }
        }

        ResultCard(title = "تفاصيل الدرجات المدخلة") {
            data.grades.forEach { grade ->
                GradeDetailRow(
                    subjectName = grade.name,
                    grade = grade.grade,
                )
            }
        }
    }
}
