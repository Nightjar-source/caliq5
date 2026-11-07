package com.Nightjar.caliq5.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Nightjar.caliq5.data.ClassData
import com.Nightjar.caliq5.ui.components.*
import com.Nightjar.caliq5.viewmodel.AppViewModel

@Composable
fun ExemptionSelectScreen(viewModel: AppViewModel) {
    val selectedClass by viewModel.selectedClass.collectAsState()
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()

    CaliqScreen(title = "الاعفاء العام والفردي", scrollable = false, oneUiReachabilityEnabled = true) {
        val bottomNavPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val topStatusPadding = WindowInsets.statusBars
            .union(WindowInsets.captionBar)
            .union(WindowInsets.displayCutout)
            .asPaddingValues()
            .calculateTopPadding()

        BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            val screenWidth = maxWidth
            val containerMaxWidth = when {
                maxWidth >= 1400.dp -> 1340.dp
                maxWidth >= 1100.dp -> 1180.dp
                maxWidth >= 840.dp -> 960.dp
                else -> maxWidth
            }
            val isLandscape = maxWidth > maxHeight
            val isPhoneLandscape = isLandscape && (maxHeight < 540.dp || maxWidth < 840.dp)
            val gridColumns = when {
                isPhoneLandscape -> GridCells.Fixed(2)
                maxWidth >= 840.dp -> GridCells.Adaptive(minSize = 200.dp)
                maxWidth >= 600.dp -> GridCells.Adaptive(minSize = 180.dp)
                else -> GridCells.Adaptive(minSize = 160.dp)
            }
            LazyVerticalGrid(
                state = gridState,
                columns = gridColumns,
                modifier = Modifier
                    .widthIn(max = containerMaxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(
                    top = topStatusPadding + 65.dp,
                    bottom = 160.dp + bottomNavPadding,
                    start = 2.dp,
                    end = 2.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "select_explanation") {
                Box(modifier = Modifier.fillMaxWidth().popInOnInitialLoad(0), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth()) {
                        FeatureExplanationCard(
                            icon = "school",
                            text = "لمعرفة ما إذا كنت مؤهلاً للإعفاء العام أو الفردي، قم باختيار صفك الدراسي أولاً لتهيئة المواد المخصصة لك.",
                        )
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }, key = "select_title") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .popInOnInitialLoad(1),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth()) {
                        Text(
                            text = "اختر صفك الدراسي",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 2.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                        )
                    }
                }
            }

            @OptIn(ExperimentalLayoutApi::class)
            item(span = { GridItemSpan(maxLineSpan) }, key = "classes_flow_grid") {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val cardWidth = if (screenWidth < 360.dp) 145.dp else 165.dp
                        ClassData.classNames.forEachIndexed { index, className ->
                            ClassCardButton(
                                text = className,
                                selected = selectedClass == className,
                                onClick = { viewModel.selectClass(className) },
                                modifier = Modifier
                                    .width(cardWidth)
                                    .popInOnInitialLoad(index + 2)
                            )
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }, key = "start_button") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
                        .popInOnInitialLoad(minOf(ClassData.classNames.size, 6) + 2),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = selectedClass,
                        transitionSpec = {
                            (fadeIn(animationSpec = androidx.compose.animation.core.tween(200)) + 
                             scaleIn(initialScale = 0.94f, animationSpec = spring(dampingRatio = 0.55f, stiffness = 320f)))
                                .togetherWith(fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) + scaleOut(targetScale = 1.04f))
                                .using(SizeTransform(clip = false))
                        },
                        contentAlignment = Alignment.Center,
                        label = "class_start_btn_anim"
                    ) { cls ->
                        CaliqPrimaryButton(
                            onClick = { viewModel.startGradeInput() },
                            text = if (cls.isNotEmpty()) "بدء إدخال الدرجات ($cls)" else "بدء إدخال الدرجات",
                            icon = Icons.AutoMirrored.Filled.ArrowForward,
                            modifier = Modifier
                                .actionButtonAdaptive()
                                .zIndex(5f)
                        )
                    }
                }
            }
        }
    }
    }
}

