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
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            state = gridState,
            columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 250.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 120.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(), top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 65.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(0)) {
                    FeatureExplanationCard(
                        icon = "task_alt",
                        text = "أدخل درجتك في الفصل الأول ونصف السنة لتعرف بالضبط كم تحتاج في الفصل الثاني لتصل إلى سعي 75% (إعفاء عام) أو 90% (إعفاء فردي)."
                    )
                }
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(1), contentAlignment = Alignment.Center) {
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

            item {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(2), contentAlignment = Alignment.Center) {
                    GradeInputField(
                        label = "الفصل الأول:",
                        value = term1,
                        onValueChange = { viewModel.updateTerm2Field("term1", it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(3), contentAlignment = Alignment.Center) {
                    GradeInputField(
                        label = "نصف السنة:",
                        value = midYear,
                        onValueChange = { viewModel.updateTerm2Field("midYear", it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).popInOnInitialLoad(4), contentAlignment = Alignment.Center) {
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
