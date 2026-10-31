package com.Nightjar.caliq5.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.Nightjar.caliq5.ui.components.*
import com.Nightjar.caliq5.viewmodel.AppViewModel

@Composable
fun Term2Screen(viewModel: AppViewModel) {
    val subjectName by viewModel.term2SubjectName.collectAsState()
    val term1 by viewModel.term2Term1.collectAsState()
    val midYear by viewModel.term2MidYear.collectAsState()
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

    CaliqScreen(title = "كم تحتاج في الفصل الثاني", onBack = { viewModel.goBack() }, scrollable = false) {
        val topSafeInset = WindowInsets.statusBars
            .union(WindowInsets.captionBar)
            .union(WindowInsets.displayCutout)
            .asPaddingValues()
            .calculateTopPadding()
        val bottomNavPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val density = androidx.compose.ui.platform.LocalDensity.current
        val isImeVisible = WindowInsets.ime.getBottom(density) > 0
        val dynamicBottomPadding by androidx.compose.animation.core.animateDpAsState(
            targetValue = if (isImeVisible) 20.dp else (160.dp + bottomNavPadding),
            animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow),
            label = "term2BottomPadding"
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            val isLandscape = maxWidth > maxHeight
            val isPhoneLandscape = isLandscape && (maxHeight < 540.dp || maxWidth < 840.dp)
            val gridColumns = when {
                isPhoneLandscape -> androidx.compose.foundation.lazy.grid.GridCells.Fixed(2)
                maxWidth >= 1200.dp -> androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 290.dp)
                maxWidth >= 840.dp -> androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 310.dp)
                maxWidth >= 600.dp -> androidx.compose.foundation.lazy.grid.GridCells.Fixed(2)
                else -> androidx.compose.foundation.lazy.grid.GridCells.Fixed(1)
            }

            val containerMaxWidth = when {
                maxWidth >= 1400.dp -> 1340.dp
                maxWidth >= 1100.dp -> 1140.dp
                maxWidth >= 840.dp -> 960.dp
                else -> maxWidth
            }

            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                state = gridState,
                columns = gridColumns,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = dynamicBottomPadding, top = topSafeInset + 65.dp),
                modifier = Modifier.widthIn(min = 320.dp, max = containerMaxWidth).fillMaxWidth().imePadding().padding(horizontal = 16.dp)
            ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(0)) {
                    FeatureExplanationCard(
                        icon = "task_alt",
                        text = "أدخل درجتك في الفصل الأول ونصف السنة لتعرف بالضبط كم تحتاج في الفصل الثاني لتصل إلى سعي 75% (إعفاء عام) أو 90% (إعفاء فردي)."
                    )
                }
            }

            item(key = "t2_name", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().animateItem(placementSpec = androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow)).popInOnInitialLoad(1), contentAlignment = Alignment.Center) {
                    GradeInputField(
                        label = "اسم المادة:",
                        value = subjectName,
                        onValueChange = { viewModel.updateTerm2Field("name", it) },
                        placeholder = "مثال: الكيمياء",
                        showPercent = false,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
                    )
                }
            }

            item(key = "t2_term1") {
                Box(modifier = Modifier.fillMaxWidth().animateItem(placementSpec = androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow)).popInOnInitialLoad(2), contentAlignment = Alignment.Center) {
                    GradeInputField(
                        label = "الفصل الأول:",
                        value = term1,
                        onValueChange = { viewModel.updateTerm2Field("term1", it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item(key = "t2_midyear") {
                Box(modifier = Modifier.fillMaxWidth().animateItem(placementSpec = androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow)).popInOnInitialLoad(3), contentAlignment = Alignment.Center) {
                    GradeInputField(
                        label = "نصف السنة:",
                        value = midYear,
                        onValueChange = { viewModel.updateTerm2Field("midYear", it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item(key = "t2_btn", span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().animateItem(placementSpec = androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow)).padding(vertical = 16.dp).popInOnInitialLoad(4), contentAlignment = Alignment.Center) {
                    CaliqPrimaryButton(
                        onClick = { viewModel.calculateTerm2() },
                        text = "حساب الدرجة المطلوبة",
                        icon = Icons.Default.TrackChanges,
                        modifier = Modifier.actionButtonAdaptive()
                    )
                }
            }
        }
        }
    }
}
