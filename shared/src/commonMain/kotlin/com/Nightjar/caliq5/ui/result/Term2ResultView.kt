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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import com.Nightjar.caliq5.ui.components.*
import com.Nightjar.caliq5.viewmodel.ResultDisplayState
import com.Nightjar.caliq5.theme.successColor

@Composable
fun Term2ResultContent(state: ResultDisplayState.Term2Result) {
    val data = state.data
    val term1 = data.grades.find { it.name == "الفصل الأول" }?.grade ?: 0.0
    val mid = data.grades.find { it.name == "نصف السنة" }?.grade ?: 0.0

    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.widthIn(min = 280.dp, max = 500.dp).weight(1f)) {
            Term2RequirementCard(
                title = "لتحقيق معدل سعي سنوي 75% (الإعفاء العام)",
                required = data.requiredFor75,
                target = 75,
                totalRequired = 225,
                term1 = term1,
                midYear = mid,
                subjectName = data.subjName,
                targetName = "الإعفاء العام",
                initialExpanded = state.is75Expanded,
                onExpandedChange = { state.is75Expanded = it }
            )
        }
        
        Box(modifier = Modifier.widthIn(min = 280.dp, max = 500.dp).weight(1f)) {
            Term2RequirementCard(
                title = "لتحقيق معدل سعي سنوي 90% (الإعفاء الفردي)",
                required = data.requiredFor90,
                target = 90,
                totalRequired = 270,
                term1 = term1,
                midYear = mid,
                subjectName = data.subjName,
                targetName = "الإعفاء الفردي",
                initialExpanded = state.is90Expanded,
                onExpandedChange = { state.is90Expanded = it }
            )
        }
    }
}

@Composable
private fun Term2RequirementCard(
    title: String,
    required: Double,
    target: Int,
    totalRequired: Int,
    term1: Double,
    midYear: Double,
    subjectName: String,
    targetName: String,
    initialExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {}
) {
    var isExpanded by rememberSaveable { mutableStateOf(initialExpanded) }
    val arrowAngle by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "t2_arrow_angle"
    )
    val arrowScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExpanded) 1.15f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "t2_arrow_scale"
    )
    val isPossible = required <= 100
    val statusMsg = when {
        required < 0 -> "محسوم! لقد حققت المعدل بالفعل وتحتاج إلى 0 لتحقيق سعي $target%."
        isPossible -> "نعم، يمكنك تحقيق $targetName وهذا هو المعدل الحد الأدنى. شد حيلك✨🌠😍 في الفصل الثاني!"
        else -> "لا يمكنك تحقيق هذه الدرجة والمعدل ($target%) لأن الدرجة المطلوبة أعلى من 100. بالتوفيق بامتحانات نهاية العام 🤝."
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Goal Header Title at the top
        Text(
            text = "$title ${if (subjectName.isNotEmpty()) "لمادة $subjectName" else ""}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )

        // 2. 3D Rectangular Grade Box matching MainGradeBox
        Term2GradeBox(
            requiredGrade = required,
            isPossible = isPossible
        )

        // 3. Result Detail Card containing the status message & expandable equation explanation
        ResultCard(title = "") {
            Text(
                text = statusMsg,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isPossible) successColor else Color(0xFFDC2626)
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Expandable Equation Explanation matching <details> in web
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            isExpanded = !isExpanded
                            onExpandedChange(isExpanded)
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
                    val t1Str = com.Nightjar.caliq5.ui.components.formatDecimalGrade(term1)
                    val midStr = com.Nightjar.caliq5.ui.components.formatDecimalGrade(midYear)
                    val reqStr = com.Nightjar.caliq5.ui.components.formatDecimalGrade(required)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clipToBounds()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "لتحقيق سعي $target%، يجب أن يكون مجموع درجاتك هو $totalRequired.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = "المعادلة: $totalRequired - ($t1Str + $midStr) = $reqStr",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                    }
                }
            }
        }
    }
}
