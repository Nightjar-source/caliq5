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
fun AnnualAverageScreen(viewModel: AppViewModel) {
    val subjectName by viewModel.annualSubjectName.collectAsState()
    val term1 by viewModel.annualTerm1.collectAsState()
    val midYear by viewModel.annualMidYear.collectAsState()
    val term2 by viewModel.annualTerm2.collectAsState()
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

    CaliqScreen(title = "حاسبة السعي السنوي", onBack = { viewModel.goBack() }, scrollable = false) {
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
            label = "annualBottomPadding"
        )

        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            state = gridState,
            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 340.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = dynamicBottomPadding, top = topSafeInset + 65.dp),
            modifier = Modifier.fillMaxSize().imePadding().padding(horizontal = 16.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(0)) {
                    FeatureExplanationCard(
                        icon = "calculate",
                        text = "تستخدم هذه الأداة لحساب السعي السنوي لمادة معينة (مجموع الفصل الأول ونصف السنة والفصل الثاني مقسوماً على 3) مع تطبيق قاعدة جبر الكسور تلقائياً."
                    )
                }
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(1), contentAlignment = Alignment.Center) {
                    GradeInputField(
                        label = "اسم المادة:",
                        value = subjectName,
                        onValueChange = { viewModel.updateAnnualField("name", it) },
                        placeholder = "مثال: الرياضيات",
                        showPercent = false,
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
                    )
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(2), contentAlignment = Alignment.Center) {
                    GradeInputField(
                        label = "الفصل الأول:",
                        value = term1,
                        onValueChange = { viewModel.updateAnnualField("term1", it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(3), contentAlignment = Alignment.Center) {
                    GradeInputField(
                        label = "نصف السنة:",
                        value = midYear,
                        onValueChange = { viewModel.updateAnnualField("midYear", it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(4), contentAlignment = Alignment.Center) {
                    GradeInputField(
                        label = "الفصل الثاني:",
                        value = term2,
                        onValueChange = { viewModel.updateAnnualField("term2", it) },
                        modifier = Modifier.widthIn(max = 380.dp).fillMaxWidth()
                    )
                }
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).popInOnInitialLoad(5), contentAlignment = Alignment.Center) {
                    CaliqPrimaryButton(
                        onClick = { viewModel.calculateAnnualAverage() },
                        text = "حساب السعي",
                        icon = Icons.Default.Functions,
                        modifier = Modifier.actionButtonAdaptive()
                    )
                }
            }
        }
    }
}
