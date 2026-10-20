package com.Nightjar.caliq5.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.launch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.Nightjar.caliq5.ui.components.neuralAmbientGlow
import com.Nightjar.caliq5.ui.components.longitudinalSheen
import com.Nightjar.caliq5.ui.components.expressive3DCurvature
import com.Nightjar.caliq5.ui.components.frostedOrganicCapsule
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.input.pointer.pointerInput
import com.Nightjar.caliq5.viewmodel.ResultDisplayState
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import com.Nightjar.caliq5.data.*
import com.Nightjar.caliq5.ui.screens.*
import com.Nightjar.caliq5.ui.components.*
import caliq5.shared.generated.resources.*
import com.Nightjar.caliq5.ui.background.MathBackground
import com.Nightjar.caliq5.viewmodel.AppViewModel

/**
 * Main navigation host with official Material 3 Adaptive layout support.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppNavigation(
    viewModel: AppViewModel,
    isTablet: Boolean = false,
    adBanner: @Composable () -> Unit = {},
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val showSideMenu by viewModel.showSideMenu.collectAsState()
    val resultState by viewModel.resultState.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()

    val editingStudentMode by viewModel.editingStudentMode.collectAsState()

    // Back handler (unified across Android 8-17, iOS, Desktop, and Web)
    com.Nightjar.caliq5.utils.BackHandler(enabled = showSideMenu || resultState != null || canGoBack || editingStudentMode != null || currentScreen != Screen.EXEMPTION_SELECT) {
        viewModel.handleTopBarBack()
    }

    // Official Material 3 Adaptive Window & Posture Calculation
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val adaptiveWindowSizeClass = adaptiveInfo.windowSizeClass
    val isTabletop = adaptiveInfo.windowPosture.isTabletop

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val windowInfo = LocalWindowInfo.current
        val density = LocalDensity.current
        val containerWidth = if (maxWidth > 0.dp) maxWidth else with(density) { windowInfo.containerSize.width.toDp() }.coerceAtLeast(1.dp)
        val containerHeight = if (maxHeight > 0.dp) maxHeight else with(density) { windowInfo.containerSize.height.toDp() }.coerceAtLeast(1.dp)
        val isWideScreen = containerWidth >= 600.dp && adaptiveWindowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT
        val isLandscape = containerWidth > containerHeight && containerHeight < 500.dp && isWideScreen
        val targetRailWidth = when (adaptiveWindowSizeClass.windowWidthSizeClass) {
            WindowWidthSizeClass.EXPANDED -> 72.dp
            WindowWidthSizeClass.MEDIUM -> 64.dp
            else -> if (isLandscape) 56.dp else 64.dp
        }
        val railWidth by animateDpAsState(
            targetValue = targetRailWidth,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
            label = "rail_width_anim"
        )

        MathBackground(modifier = Modifier.fillMaxSize())

        val layoutDirection = LocalLayoutDirection.current
        val safeDrawing = WindowInsets.safeDrawing.asPaddingValues()
        val safeStart = safeDrawing.calculateStartPadding(layoutDirection)
        val safeEnd = safeDrawing.calculateEndPadding(layoutDirection)
        val isNavHidden by viewModel.isNavHidden.collectAsState()

        val topSafeInset = WindowInsets.statusBars
            .union(WindowInsets.captionBar)
            .union(WindowInsets.displayCutout)
            .asPaddingValues()
            .calculateTopPadding()

        val contentStartPad by animateDpAsState(
            targetValue = if (isWideScreen) (safeStart + 16.dp).coerceAtLeast(0.dp) else 0.dp,
            animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
            label = "content_start_pad"
        )
        val contentEndPad by animateDpAsState(
            targetValue = if (isWideScreen) (safeEnd + railWidth + 24.dp).coerceAtLeast(0.dp) else 0.dp,
            animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
            label = "content_end_pad"
        )

        Box(modifier = Modifier.fillMaxSize()) {
            MainContent(
                currentScreen = currentScreen,
                viewModel = viewModel,
                contentPadding = PaddingValues(
                    start = contentStartPad.coerceAtLeast(0.dp),
                    end = contentEndPad.coerceAtLeast(0.dp)
                ),
                isWideScreen = isWideScreen
            )

            // Dynamic Adaptive Navigation Rail (Wide / Landscape / Foldable / Chromebook / DeX)
            val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl
            val railSlideOffset: (Int) -> Int = { width -> if (isRtl) -width else width }
            AnimatedVisibility(
                visible = isWideScreen,
                enter = slideInHorizontally(
                    initialOffsetX = railSlideOffset,
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutHorizontally(
                    targetOffsetX = railSlideOffset,
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow)
                ) + fadeOut(animationSpec = tween(250)),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                val verticalPad = if (containerHeight < 500.dp) 8.dp else 24.dp
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = topSafeInset + verticalPad, bottom = verticalPad)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CaliqNavigationRail(
                        currentScreen = currentScreen,
                        isMenuOpen = showSideMenu,
                        onScreenSelected = { screen -> viewModel.navigateTo(screen) },
                        onMenuClick = { viewModel.openMenu() },
                        railWidth = railWidth
                    )
                }
            }

            // Adaptive AdMob Banner in Wide Screens / Landscape (Docked non-intrusively at bottom center)
            val isImeVisible = WindowInsets.ime.getBottom(density) > 0
            if (isWideScreen && !isImeVisible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 4.dp)
                ) {
                    adBanner()
                }
            }

            // Dynamic Adaptive Bottom Navigation (Phone Portrait)
            val isMainTab = currentScreen in listOf(
                Screen.EXEMPTION_SELECT,
                Screen.EXEMPTION_INPUT,
                Screen.ANNUAL_AVERAGE,
                Screen.MONTHLY_AVERAGE,
                Screen.TERM2_CALCULATION
            )
            // Persistent Bottom Container: AdMob Banner + Animated Bottom Navigation
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = 0.78f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Persistent AdMob Adaptive Banner across all main & sub-screens
                // Sits above CaliqBottomNav in phone portrait. When bottom nav slides down, banner smoothly glides down to bottom!
                AnimatedVisibility(
                    visible = !isImeVisible && resultState == null,
                    enter = fadeIn(tween(350)) + expandVertically(
                        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
                        expandFrom = Alignment.Bottom
                    ),
                    exit = fadeOut(tween(200)) + shrinkVertically(
                        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
                        shrinkTowards = Alignment.Bottom
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        adBanner()
                    }
                }

                // Dynamic Adaptive Bottom Navigation (Phone Portrait)
                AnimatedVisibility(
                    visible = isMainTab && !isNavHidden && !isWideScreen,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow)
                    ) + fadeOut(animationSpec = tween(250)),
                ) {
                    CaliqBottomNav(
                        currentScreen = currentScreen,
                        isMenuOpen = showSideMenu,
                        onScreenSelected = { viewModel.navigateTo(it) },
                        onMenuClick = { viewModel.openMenu() },
                        modifier = Modifier
                            .padding(top = 4.dp, bottom = 6.dp, start = 8.dp, end = 8.dp),
                    )
                }
            }
        }

// Global Header was removed because CaliqScreen manages it directly

        // Result overlay
        var lastResult by remember { mutableStateOf<com.Nightjar.caliq5.viewmodel.ResultDisplayState?>(null) }
        LaunchedEffect(resultState) {
            if (resultState != null) lastResult = resultState
        }

        AnimatedVisibility(
            visible = resultState != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow)
            ) + fadeIn(animationSpec = tween(500)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow)
            ) + fadeOut(animationSpec = tween(400)),
        ) {
            (resultState ?: lastResult)?.let { result ->
                ResultOverlay(
                    title = result.title,
                    content = result,
                    onClose = { viewModel.hideResult() },
                    onExportPdf = { viewModel.exportToPdf(viewModel.resultState.value ?: result) },
                    adBanner = adBanner,
                )
            }
        }

        // Side menu background dimming
        AnimatedVisibility(
            visible = showSideMenu,
            enter = fadeIn(tween(350)),
            exit = fadeOut(tween(350)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.closeMenu() }
                    )
            )
        }

        // Side Menu Panel
        AnimatedVisibility(
            visible = showSideMenu,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(durationMillis = 320, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(durationMillis = 260, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
            ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                CaliqSideMenu(
                    viewModel = viewModel,
                    onClose = { viewModel.closeMenu() },
                    onNavigate = { screen ->
                        viewModel.closeMenu()
                        viewModel.navigateTo(screen)
                    },
                )
            }
        }

        val showNotificationDialog by viewModel.showNotificationDialog.collectAsState()
        if (showNotificationDialog) {
            CaliqConfirmationDialog(
                title = "تفعيل الإشعارات",
                message = "تم حفظ ملف الـ PDF بنجاح في جهازك. للحصول على تنبيهات عند اكتمال التنزيل مستقبلاً، يُرجى تفعيل الإشعارات للتطبيق.",
                confirmText = "تفعيل الآن",
                dismissText = "تجاهل",
                onConfirm = {
                    com.Nightjar.caliq5.utils.openAppSettings()
                    viewModel.dismissNotificationDialog()
                },
                onDismiss = {
                    viewModel.dismissNotificationDialog()
                }
            )
        }

        // Fluid Liquid Snackbar
        var currentSnackbarMsg by remember { mutableStateOf<com.Nightjar.caliq5.viewmodel.SnackbarMessage?>(null) }
        LaunchedEffect(snackbarMessage) {
            if (snackbarMessage != null) {
                currentSnackbarMsg = snackbarMessage
            }
        }

        val safeTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val snackbarVisibleState = remember { androidx.compose.animation.core.MutableTransitionState(false) }
        snackbarVisibleState.targetState = (snackbarMessage != null)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = safeTopPadding + 56.dp)
                .padding(
                    start = if (isWideScreen) safeStart + 16.dp else 16.dp,
                    end = if (isWideScreen) safeEnd + 90.dp else 16.dp
                ),
            contentAlignment = Alignment.TopCenter,
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visibleState = snackbarVisibleState,
                enter = androidx.compose.animation.slideInVertically(
                    initialOffsetY = { -it - 800 },
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.65f, stiffness = 200f)
                ) + androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) + androidx.compose.animation.scaleIn(
                    initialScale = 0.4f,
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.65f, stiffness = 200f)
                ),
                exit = androidx.compose.animation.slideOutVertically(
                    targetOffsetY = { -it - 800 },
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.7f, stiffness = 250f)
                ) + androidx.compose.animation.fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(250)
                ) + androidx.compose.animation.scaleOut(
                    targetScale = 0.6f,
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.7f, stiffness = 250f)
                )
            ) {
                currentSnackbarMsg?.let { msg ->
                    CaliqSnackbar(
                        message = msg.message,
                        isError = msg.isError,
                        visible = true,
                    )
                }
            }
        }

        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        MorphingStarLoadingIndicator()
                    }
                }
            }
        }
    }

}

@Composable
private fun MainContent(
    currentScreen: Screen,
    viewModel: AppViewModel,
    contentPadding: PaddingValues,
    isWideScreen: Boolean = false,
) {
    val canGoBack by viewModel.canGoBack.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideOrLandscape = isWideScreen
        val topBarHeight = if (isWideOrLandscape) 38.dp else 48.dp

        val isNavigatingBack by viewModel.isNavigatingBack.collectAsState()

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                val direction = if (isNavigatingBack) -1 else 1
                val fluidEasing = androidx.compose.animation.core.CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                val slideSpec = androidx.compose.animation.core.tween<androidx.compose.ui.unit.IntOffset>(
                    durationMillis = 380,
                    easing = fluidEasing
                )
                val fadeSpec = androidx.compose.animation.core.tween<Float>(
                    durationMillis = 280,
                    easing = androidx.compose.animation.core.LinearOutSlowInEasing
                )

                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth * direction },
                    animationSpec = slideSpec
                ) + fadeIn(animationSpec = fadeSpec)).togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth * direction },
                        animationSpec = slideSpec
                    ) + fadeOut(animationSpec = fadeSpec)
                )
            },
            label = "screen_transition",
            modifier = Modifier.fillMaxSize()
        ) { screen ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                when (screen) {
                    Screen.EXEMPTION_SELECT -> ExemptionSelectScreen(viewModel)
                    Screen.EXEMPTION_INPUT  -> ExemptionInputScreen(viewModel)
                    Screen.ANNUAL_AVERAGE   -> AnnualAverageScreen(viewModel)
                    Screen.MONTHLY_AVERAGE  -> MonthlyAverageScreen(viewModel)
                    Screen.TERM2_CALCULATION -> Term2Screen(viewModel)
                    Screen.STATISTICS       -> StatisticsScreen(viewModel)
                    Screen.MY_GRADES        -> MyGradesScreen(viewModel)
                    Screen.INSTRUCTIONS     -> InstructionsScreen(viewModel)
                    Screen.ABOUT            -> AboutScreen(viewModel)
                    Screen.NEWS             -> NewsScreen(viewModel)
                    Screen.UPDATES          -> UpdatesScreen(viewModel)
                    Screen.WIDGET           -> WidgetScreen(viewModel)
                    Screen.APPEARANCE       -> AppearanceScreen(viewModel)
                }
            }
        }

        val layoutDirection = LocalLayoutDirection.current
        val topBarStartPadding = if (isWideScreen) contentPadding.calculateStartPadding(layoutDirection) else 0.dp
        val topBarEndPadding = if (isWideScreen) contentPadding.calculateEndPadding(layoutDirection) else 0.dp
        val containerWidth = maxWidth
        val topSafeInset = WindowInsets.statusBars
            .union(WindowInsets.captionBar)
            .union(WindowInsets.displayCutout)
            .asPaddingValues()
            .calculateTopPadding()

        val resultState by viewModel.resultState.collectAsState()

        val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
        val editingStudentMode by viewModel.editingStudentMode.collectAsState()
        val isMainScreen = currentScreen == Screen.EXEMPTION_SELECT || 
                           currentScreen == Screen.ANNUAL_AVERAGE || 
                           currentScreen == Screen.MONTHLY_AVERAGE || 
                           currentScreen == Screen.TERM2_CALCULATION
        val showTopBack = !isMainScreen || editingStudentMode != null || resultState != null

        // Material 3 Expressive adaptive dimensions (ultra-compact in landscape/tablets, sleek in portrait)
        val isUltraCompact = containerWidth < 280.dp
        val topBarTopPad = if (isUltraCompact) 2.dp else if (isWideOrLandscape) 4.dp else 8.dp
        val topCapsuleHeight = if (isUltraCompact) 30.dp else if (isWideOrLandscape) 36.dp else 42.dp
        val iconBtnSize = if (isUltraCompact) 30.dp else if (isWideOrLandscape) 36.dp else 40.dp
        val iconInnerSize = if (isUltraCompact) 15.dp else if (isWideOrLandscape) 18.dp else 22.dp
        val titleSideClearance = if (isUltraCompact) 36.dp else if (containerWidth < 340.dp) 44.dp else if (isWideOrLandscape) 56.dp else 64.dp

                // Unified Floating Top Bar (Back Button + Centered Title Capsule) with Zero Collision Guarantee
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = topSafeInset + topBarTopPad)
                .padding(
                    start = topBarStartPadding + (if (isUltraCompact) 8.dp else 16.dp),
                    end = topBarEndPadding + (if (isUltraCompact) 8.dp else 16.dp)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Floating Circular Back Button (Start edge)
            Box(modifier = Modifier.size(iconBtnSize), contentAlignment = Alignment.Center) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showTopBack && resultState == null,
                    enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.75f, animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.75f, animationSpec = tween(180))
                ) {
                    CaliqIconButton(
                        onClick = { viewModel.handleTopBarBack() },
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.82f else 0.88f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.28f else 0.40f))
                        ),
                        modifier = Modifier.size(iconBtnSize)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            modifier = Modifier.size(iconInnerSize)
                        )
                    }
                }
            }

            // 2. Floating Oval Title Capsule in the Center (Weight 1f, mathematically never overlaps)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = if (isUltraCompact) 4.dp else 8.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = resultState == null,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(150))
                ) {
                    Surface(
                        modifier = Modifier
                            .height(topCapsuleHeight)
                            .widthIn(max = 460.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.82f else 0.88f),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.28f else 0.40f))
                        ),
                        shadowElevation = 0.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = if (isUltraCompact) 8.dp else if (isWideOrLandscape) 14.dp else 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = Pair<Screen, String?>(currentScreen, editingStudentMode),
                                transitionSpec = {
                                    val easeOut = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
                                    (fadeIn(animationSpec = tween(300, easing = easeOut)) + 
                                     slideInVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)) { it / 2 } +
                                     scaleIn(initialScale = 0.96f, animationSpec = tween(300, easing = easeOut))
                                    ).togetherWith(
                                        fadeOut(animationSpec = tween(220, easing = easeOut)) + 
                                        slideOutVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)) { -it / 2 } +
                                        scaleOut(targetScale = 1.04f, animationSpec = tween(220, easing = easeOut))
                                    ).using(SizeTransform(clip = false))
                                },
                                label = "title_transition"
                            ) { statePair ->
                                val screen = statePair.first
                                val editMode = statePair.second
                                val selectedClass by viewModel.selectedClass.collectAsState()
                                val title = when (screen) {
                                    Screen.EXEMPTION_SELECT -> "الاعفاء العام والفردي"
                                    Screen.EXEMPTION_INPUT -> if (selectedClass.isNotEmpty()) "إدخال درجات - $selectedClass" else "إدخال درجات الإعفاء"
                                    Screen.ANNUAL_AVERAGE -> "حاسبة السعي السنوي"
                                    Screen.MONTHLY_AVERAGE -> "حاسبة الفصلي"
                                    Screen.TERM2_CALCULATION -> "كم تحتاج في الفصل الثاني"
                                    Screen.STATISTICS -> "السجل"
                                    Screen.MY_GRADES -> when (editMode) {
                                        "add" -> "إضافة سجل درجاتي (السجل الشامل)"
                                        "edit" -> "تعديل سجل درجاتي (السجل الشامل)"
                                        else -> "درجاتي (السجل السنوي)"
                                    }
                                    Screen.INSTRUCTIONS -> "كيفية الاستخدام"
                                    Screen.ABOUT -> "حول التطبيق"
                                    Screen.NEWS -> "آخر الأخبار"
                                    Screen.UPDATES -> "التحديثات"
                                    Screen.WIDGET -> "إعداد التطبيق المصغر"
                                    Screen.APPEARANCE -> "المظهر والسمات"
                                }
                                val pColor = MaterialTheme.colorScheme.primary

                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = pColor,
                                        fontSize = if (isUltraCompact) 11.5.sp else if (isWideOrLandscape) 14.sp else 15.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 3. Balance space (End edge) to keep title centered
            Box(modifier = Modifier.size(iconBtnSize)) {}
        }
    }
}

@Composable
fun CaliqBottomNav(
    currentScreen: Screen,
    isMenuOpen: Boolean = false,
    onScreenSelected: (Screen) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = when {
        currentScreen == Screen.EXEMPTION_SELECT || currentScreen == Screen.EXEMPTION_INPUT -> 0
        currentScreen == Screen.ANNUAL_AVERAGE -> 1
        currentScreen == Screen.MONTHLY_AVERAGE -> 2
        currentScreen == Screen.TERM2_CALCULATION -> 3
        else -> -1
    }

    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val themePrimary = MaterialTheme.colorScheme.primary

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val navTotalWidth = maxWidth
        val isUltraCompact = navTotalWidth < 280.dp
        val isCompact = navTotalWidth < 340.dp
        val barHeight = if (isUltraCompact) 46.dp else if (isCompact) 50.dp else 54.dp
        val squircleSize = if (isUltraCompact) 38.dp else if (isCompact) 42.dp else 48.dp

        // Neutral flat liquid indicator color (Material 3 Expressive flat pill that never clashes with icons or bar)
        val indicatorColor = if (isDark) {
            Color(0xFF334155).copy(alpha = 0.90f)
        } else {
            Color(0xFFF1F5F9).copy(alpha = 0.95f)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .padding(horizontal = if (isUltraCompact) 4.dp else 8.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isUltraCompact) 4.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. الكبسولة العضوية الحبيبية الرئيسية الشفافة (Original 25% frosted glass from backup)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                val totalWidth = maxWidth
                val tabWidth = totalWidth / 4f
                val tabWidthPx = with(LocalDensity.current) { tabWidth.toPx() }
                val safeTabWidthPx = tabWidthPx.coerceAtLeast(1f)
                val pillWidth = (tabWidth - 4.dp).coerceIn(18.dp, 62.dp)
                val innerPillHeight = if (isUltraCompact) 32.dp else if (isCompact) 36.dp else 42.dp

                var dragOffset by remember { mutableStateOf<Float?>(null) }
                var lastValidIndex by remember { mutableIntStateOf(if (selectedIndex in 0..3) selectedIndex else 0) }
                if (selectedIndex in 0..3) {
                    lastValidIndex = selectedIndex
                }

                val displayIndex = if (dragOffset != null) {
                    ((dragOffset!! / safeTabWidthPx).toInt()).coerceIn(0, 3)
                } else {
                    lastValidIndex
                }

                val layoutDirection = LocalLayoutDirection.current
                val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl

                val targetX = if (dragOffset != null) {
                    val offsetDp = with(LocalDensity.current) { (dragOffset!! - (pillWidth.toPx() / 2f)).toDp() }
                    val minX = (tabWidth - pillWidth) / 2f
                    val maxX = (tabWidth * 3) + (tabWidth - pillWidth) / 2f
                    offsetDp.coerceIn(minOf(minX, maxX), maxOf(minX, maxX))
                } else {
                    (tabWidth * displayIndex) + (tabWidth - pillWidth) / 2f
                }

                // Authentic M3 Expressive liquid fluid spring motion (zero vertical squash, purely horizontal)
                val animX by animateDpAsState(
                    targetValue = targetX,
                    animationSpec = spring(
                        dampingRatio = 0.65f, // Liquid fluid elasticity
                        stiffness = 300f      // Responsive fluid movement
                    ),
                    label = "bottom_nav_pill_x"
                )

                val animScale by animateFloatAsState(
                    targetValue = if (selectedIndex in 0..3 || dragOffset != null) 1f else 0f,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 320f),
                    label = "bottom_nav_pill_scale"
                )

                // Dynamic liquid fluid stretch and squash (تمدد وانكماش سائل ديناميكي طبيعي)
                val moveDeltaX = kotlin.math.abs(animX.value - targetX.value).dp
                val stretchX = (moveDeltaX * 0.25f).coerceAtMost(10.dp)
                val dynamicPillWidth = (pillWidth + stretchX).coerceAtLeast(16.dp)
                val dynamicPillHeight = (innerPillHeight - (stretchX * 0.20f)).coerceAtLeast(24.dp)
                val dynamicPillX = animX - (stretchX / 2f)

                val navShape = CircleShape

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .frostedOrganicCapsule(
                            shape = navShape,
                            cornerRadius = 32.dp,
                            tintColor = themePrimary,
                            alpha = 0.25f
                        )
                ) {
                    if (animScale > 0.001f) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = dynamicPillX)
                                .size(width = dynamicPillWidth, height = dynamicPillHeight)
                                .graphicsLayer {
                                    alpha = animScale.coerceIn(0f, 1f)
                                    scaleX = 0.9f + 0.1f * animScale
                                    scaleY = 0.9f + 0.1f * animScale
                                    clip = false
                                }
                                .background(
                                    color = indicatorColor,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(layoutDirection) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull()
                                    if (change != null) {
                                        if (change.pressed) {
                                            val rawX = change.position.x
                                            val directionalX = if (isRtl) size.width.toFloat() - rawX else rawX
                                            dragOffset = directionalX.coerceIn(0f, size.width.toFloat())
                                        } else {
                                            if (dragOffset != null) {
                                                val idx = (dragOffset!! / safeTabWidthPx).toInt().coerceIn(0, 3)
                                                onScreenSelected(bottomNavItems[idx].screen)
                                                dragOffset = null
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    bottomNavItems.take(4).forEachIndexed { _, item ->
                        val isSelected = (!isMenuOpen && currentScreen == item.screen) ||
                                (!isMenuOpen && currentScreen == Screen.EXEMPTION_INPUT && item.screen == Screen.EXEMPTION_SELECT)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            CaliqNavItem(
                                item = item,
                                isSelected = isSelected,
                                isUltraCompact = isUltraCompact,
                                isCompact = isCompact
                            )
                        }
                    }
                }
            }

            // 2. زر القائمة المربع المنفصل الزجاجي العضوي (Companion Floating Action Squircle)
            val fabShape = RoundedCornerShape(if (isUltraCompact) 14.dp else 18.dp)
            val isFabActive = isMenuOpen || showSideMenuPill(currentScreen)
            val fabIndicatorScale by animateFloatAsState(
                targetValue = if (isFabActive) 1f else 0f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 320f),
                label = "fab_indicator_scale"
            )
            val fabIconColor by animateColorAsState(
                targetValue = if (isFabActive) {
                    themePrimary
                } else {
                    if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF1E293B).copy(alpha = 0.85f)
                },
                animationSpec = tween(200),
                label = "fab_icon_color"
            )

            Box(
                modifier = Modifier
                    .size(squircleSize)
                    .frostedOrganicCapsule(
                        shape = fabShape,
                        cornerRadius = if (isUltraCompact) 14.dp else 18.dp,
                        tintColor = themePrimary,
                        alpha = 0.25f
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onMenuClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (fabIndicatorScale > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp)
                            .graphicsLayer {
                                alpha = fabIndicatorScale
                                scaleX = 0.85f + 0.15f * fabIndicatorScale
                                scaleY = 0.85f + 0.15f * fabIndicatorScale
                            }
                            .background(
                                color = indicatorColor,
                                shape = RoundedCornerShape(if (isUltraCompact) 11.dp else 14.dp)
                            )
                    )
                }
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "القائمة",
                    tint = fabIconColor,
                    modifier = Modifier.size(if (isUltraCompact) 18.dp else if (isCompact) 20.dp else 22.dp)
                )
            }
        }
    }
}

@Composable
fun CaliqNavItem(
    item: NavItem,
    isSelected: Boolean,
    isUltraCompact: Boolean = false,
    isCompact: Boolean = false
) {
    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = if (isDark) {
        Color(0xFFCBD5E1).copy(alpha = 0.85f)
    } else {
        Color(0xFF475569).copy(alpha = 0.85f)
    }

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(220),
        label = "nav_item_icon"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(220),
        label = "nav_item_label"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        val iconSize = if (isUltraCompact) 18.dp else if (isCompact) 20.dp else 22.dp
        Box(
            modifier = Modifier
                .size(width = if (isCompact) 36.dp else 50.dp, height = if (isCompact) 26.dp else 30.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(iconSize)
            )
        }
        if (!isUltraCompact) {
            Spacer(Modifier.height(if (isCompact) 1.dp else 2.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = labelColor,
                    fontSize = if (isCompact) 8.5.sp else 10.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun CaliqNavigationRail(
    currentScreen: Screen,
    isMenuOpen: Boolean = false,
    onScreenSelected: (Screen) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    railWidth: Dp = 72.dp,
) {
    val selectedIndex = when {
        currentScreen == Screen.EXEMPTION_SELECT || currentScreen == Screen.EXEMPTION_INPUT -> 0
        currentScreen == Screen.ANNUAL_AVERAGE -> 1
        currentScreen == Screen.MONTHLY_AVERAGE -> 2
        currentScreen == Screen.TERM2_CALCULATION -> 3
        else -> -1
    }

    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val themePrimary = MaterialTheme.colorScheme.primary

    // Neutral flat liquid indicator color (Material 3 Expressive flat pill that never clashes with icons or bar)
    val indicatorColor = if (isDark) {
        Color(0xFF334155).copy(alpha = 0.90f)
    } else {
        Color(0xFFF1F5F9).copy(alpha = 0.95f)
    }

    Column(
        modifier = modifier
            .heightIn(max = 460.dp)
            .fillMaxHeight()
            .width(railWidth)
            .padding(vertical = 10.dp, horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. الكبسولة العضوية الحبيبية العمودية الرئيسية (تحتوي على التبويبات الـ 4 مع مؤشر زجاجي سائل)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val totalHeight = maxHeight
            val tabHeight = totalHeight / 4f
            val tabHeightPx = with(LocalDensity.current) { tabHeight.toPx() }
            val safeTabHeightPx = tabHeightPx.coerceAtLeast(1f)
            val pillWidth = (railWidth - 12.dp).coerceIn(18.dp, 56.dp)
            val pillHeight = (tabHeight - 6.dp).coerceIn(18.dp, 56.dp)

            var dragOffset by remember { mutableStateOf<Float?>(null) }
            var lastValidIndex by remember { mutableIntStateOf(if (selectedIndex in 0..3) selectedIndex else 0) }
            if (selectedIndex in 0..3) {
                lastValidIndex = selectedIndex
            }

            val displayIndex = if (dragOffset != null) {
                ((dragOffset!! / safeTabHeightPx).toInt()).coerceIn(0, 3)
            } else {
                lastValidIndex
            }

            val targetY = if (dragOffset != null) {
                val offsetDp = with(LocalDensity.current) { (dragOffset!! - (pillHeight.toPx() / 2f)).toDp() }
                val minY = (tabHeight - pillHeight) / 2f
                val maxY = (tabHeight * 3) + (tabHeight - pillHeight) / 2f
                offsetDp.coerceIn(minOf(minY, maxY), maxOf(minY, maxY))
            } else {
                (tabHeight * displayIndex) + (tabHeight - pillHeight) / 2f
            }

            val animY by animateDpAsState(
                targetValue = targetY,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = 300f
                ),
                label = "rail_pill_y"
            )

            val animScale by animateFloatAsState(
                targetValue = if (selectedIndex in 0..3 || dragOffset != null) 1f else 0f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 320f),
                label = "rail_pill_scale"
            )

            // Dynamic liquid fluid stretch and squash (تمدد وانكماش سائل ديناميكي طبيعي)
            val moveDeltaY = kotlin.math.abs(animY.value - targetY.value).dp
            val stretchY = (moveDeltaY * 0.25f).coerceAtMost(10.dp)
            val dynamicPillHeight = (pillHeight + stretchY).coerceAtLeast(16.dp)
            val dynamicPillWidth = (pillWidth - (stretchY * 0.20f)).coerceAtLeast(16.dp)
            val dynamicPillY = animY - (stretchY / 2f)

            val railShape = RoundedCornerShape(32.dp)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .frostedOrganicCapsule(
                        shape = railShape,
                        cornerRadius = 32.dp,
                        tintColor = themePrimary,
                        alpha = 0.25f
                    )
            ) {
                if (animScale > 0.001f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = dynamicPillY)
                            .size(width = dynamicPillWidth, height = dynamicPillHeight)
                            .graphicsLayer {
                                alpha = animScale.coerceIn(0f, 1f)
                                scaleX = 0.9f + 0.1f * animScale
                                scaleY = 0.9f + 0.1f * animScale
                                clip = false
                            }
                            .background(
                                color = indicatorColor,
                                shape = CircleShape
                            )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull()
                                if (change != null) {
                                    if (change.pressed) {
                                        val rawY = change.position.y
                                        dragOffset = rawY.coerceIn(0f, size.height.toFloat())
                                    } else {
                                        if (dragOffset != null) {
                                            val idx = (dragOffset!! / safeTabHeightPx).toInt().coerceIn(0, 3)
                                            onScreenSelected(bottomNavItems[idx].screen)
                                            dragOffset = null
                                        }
                                    }
                                }
                            }
                        }
                    },
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                bottomNavItems.take(4).forEachIndexed { _, item ->
                    val isSelected = (!isMenuOpen && currentScreen == item.screen) ||
                            (!isMenuOpen && currentScreen == Screen.EXEMPTION_INPUT && item.screen == Screen.EXEMPTION_SELECT)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onScreenSelected(item.screen) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CaliqNavItem(
                            item = item,
                            isSelected = isSelected
                        )
                    }
                }
            }
        }

        // 2. زر القائمة المربع المنفصل الزجاجي العضوي المطابق لشريط التنقل (Floating Action Squircle)
        val menuBtnSize = (railWidth - 8.dp)
        val menuCornerRadius = (menuBtnSize * 0.42f).coerceIn(24.dp, 30.dp)
        val fabShape = RoundedCornerShape(menuCornerRadius)
        val isFabActive = isMenuOpen || showSideMenuPill(currentScreen)
        val fabIndicatorScale by animateFloatAsState(
            targetValue = if (isFabActive) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 320f),
            label = "rail_fab_indicator_scale"
        )
        val fabIconColor by animateColorAsState(
            targetValue = if (isFabActive) {
                themePrimary
            } else {
                if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF1E293B).copy(alpha = 0.85f)
            },
            animationSpec = tween(200),
            label = "rail_fab_icon_color"
        )

        Box(
            modifier = Modifier
                .size(menuBtnSize)
                .frostedOrganicCapsule(
                    shape = fabShape,
                    cornerRadius = menuCornerRadius,
                    tintColor = themePrimary,
                    alpha = 0.25f
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMenuClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (fabIndicatorScale > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .graphicsLayer {
                            alpha = fabIndicatorScale
                            scaleX = 0.85f + 0.15f * fabIndicatorScale
                            scaleY = 0.85f + 0.15f * fabIndicatorScale
                        }
                        .background(
                            color = indicatorColor,
                            shape = RoundedCornerShape(menuCornerRadius - 4.dp)
                        )
                )
            }
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "القائمة",
                tint = fabIconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

fun showSideMenuPill(screen: Screen): Boolean {
    return screen in listOf(
        Screen.MY_GRADES,
        Screen.INSTRUCTIONS,
        Screen.ABOUT,
        Screen.NEWS,
        Screen.UPDATES,
        Screen.WIDGET,
        Screen.APPEARANCE
    )
}

