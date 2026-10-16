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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import com.Nightjar.caliq5.data.*
import com.Nightjar.caliq5.ui.screens.*
import com.Nightjar.caliq5.ui.components.*
import caliq5.shared.generated.resources.*
import com.Nightjar.caliq5.ui.background.MathBackground
import com.Nightjar.caliq5.viewmodel.AppViewModel

/**
 * Main navigation host.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppNavigation(
    viewModel: AppViewModel,
    isTablet: Boolean = false,
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val windowInfo = LocalWindowInfo.current
        val density = LocalDensity.current
        val size = with(density) {
            DpSize(windowInfo.containerSize.width.toDp(), windowInfo.containerSize.height.toDp())
        }
        val windowSizeClass = WindowSizeClass.calculateFromSize(size)
        val isLandscape = size.height < 500.dp && size.width > size.height
        val isWideScreen = (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact) || isLandscape || size.width >= 600.dp

        MathBackground(modifier = Modifier.fillMaxSize())

        val layoutDirection = LocalLayoutDirection.current
        val safeDrawing = WindowInsets.safeDrawing.asPaddingValues()
        val safeStart = safeDrawing.calculateStartPadding(layoutDirection)
        val safeEnd = safeDrawing.calculateEndPadding(layoutDirection)
        val isNavHidden by viewModel.isNavHidden.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
            MainContent(
                currentScreen = currentScreen,
                viewModel = viewModel,
                contentPadding = PaddingValues(
                    start = if (isWideScreen) safeStart + 16.dp else 0.dp,
                    end = if (isWideScreen) safeEnd + 90.dp else 0.dp
                ),
                isWideScreen = isWideScreen
            )

            if (isWideScreen) {
                val verticalPad = if (size.height < 500.dp) 8.dp else 24.dp
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .padding(end = 12.dp, top = verticalPad, bottom = verticalPad),
                    contentAlignment = Alignment.Center
                ) {
                    CaliqNavigationRail(
                        currentScreen = currentScreen,
                        isMenuOpen = showSideMenu,
                        onScreenSelected = { screen -> viewModel.navigateTo(screen) },
                        onMenuClick = { viewModel.openMenu() },
                    )
                }
            } else {
                val isMainTab = currentScreen in listOf(
                    Screen.EXEMPTION_SELECT,
                    Screen.EXEMPTION_INPUT,
                    Screen.ANNUAL_AVERAGE,
                    Screen.MONTHLY_AVERAGE,
                    Screen.TERM2_CALCULATION
                )
                AnimatedVisibility(
                    visible = isMainTab && !isNavHidden && !isWideScreen,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(400)),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(400)),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    CaliqBottomNav(
                        currentScreen = currentScreen,
                        isMenuOpen = showSideMenu,
                        onScreenSelected = { viewModel.navigateTo(it) },
                        onMenuClick = { viewModel.openMenu() },
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(bottom = 20.dp, start = 12.dp, end = 12.dp),
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
        val isWideOrLandscape = maxWidth > 600.dp || maxHeight < 500.dp || isWideScreen
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

        val resultState by viewModel.resultState.collectAsState()

        // Static Glassy Top App Bar (Compact: < 56dp in portrait, even lower in landscape: 38dp)
        // Strictly padded on end in wide/landscape screens so it never extends behind the side rail!
        // Hidden when result overlay is visible to prevent duplicate headers or layer bleed-through
        AnimatedVisibility(
            visible = resultState == null,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = topBarStartPadding, end = topBarEndPadding),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.statusBars
                            .union(WindowInsets.captionBar)
                            .union(WindowInsets.displayCutout)
                    )
                    .defaultMinSize(minHeight = if (isWideOrLandscape) 48.dp else 54.dp)
                    .padding(horizontal = if (isWideOrLandscape) 12.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val editingStudentMode by viewModel.editingStudentMode.collectAsState()
                val showTopBack = currentScreen != Screen.EXEMPTION_SELECT || editingStudentMode != null || resultState != null

                AnimatedVisibility(
                    visible = showTopBack,
                    enter = fadeIn(tween(220)) + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)) + expandHorizontally(),
                    exit = fadeOut(tween(160)) + scaleOut(targetScale = 0.8f, animationSpec = tween(160)) + shrinkHorizontally()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.handleTopBarBack() },
                            modifier = Modifier.size(44.dp).clip(CircleShape)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                }
                
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
                    modifier = Modifier.weight(1f),
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
                    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
                    val pColor = MaterialTheme.colorScheme.primary

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = pColor,
                            fontSize = if (isWideOrLandscape) 15.sp else 17.sp
                        ),
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
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
    val barHeight = 58.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. الكبسولة العضوية الحبيبية الرئيسية (تحتوي على التبويبات الـ 4 مع مؤشر زجاجي سائل)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            val totalWidth = maxWidth
            val tabWidth = totalWidth / 4f
            val tabWidthPx = with(LocalDensity.current) { tabWidth.toPx() }
            val pillWidth = (tabWidth - 6.dp).coerceAtMost(62.dp)
            val innerPillHeight = 46.dp

            var dragOffset by remember { mutableStateOf<Float?>(null) }
            var localSelectedIndex by remember { mutableStateOf(selectedIndex) }
            var lastValidIndex by remember { mutableStateOf(if (selectedIndex in 0..3) selectedIndex else 0) }
            LaunchedEffect(selectedIndex) {
                localSelectedIndex = selectedIndex
                if (selectedIndex in 0..3) lastValidIndex = selectedIndex
            }
            val layoutDirection = LocalLayoutDirection.current
            val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl

            val targetX = if (dragOffset != null) {
                val offsetDp = with(LocalDensity.current) { (dragOffset!! - (pillWidth.toPx() / 2f)).toDp() }
                val minX = (tabWidth - pillWidth) / 2f
                val maxX = (tabWidth * 3) + (tabWidth - pillWidth) / 2f
                offsetDp.coerceIn(minX, maxX)
            } else {
                (tabWidth * lastValidIndex) + (tabWidth - pillWidth) / 2f
            }

            // Authentic liquid spring physics: soft, organic, elastic fluidity like mercury/water
            val animX by animateDpAsState(
                targetValue = targetX,
                animationSpec = spring(
                    dampingRatio = 0.60f, // Liquid fluid elasticity
                    stiffness = 220f      // Soft fluid movement following finger seamlessly
                ),
                label = "bottom_nav_pill_x"
            )

            val animScale by animateFloatAsState(
                targetValue = if ((localSelectedIndex in 0..3) || dragOffset != null) 1f else 0f,
                animationSpec = spring(dampingRatio = 0.60f, stiffness = 260f),
                label = "bottom_nav_pill_scale"
            )

            // Volume-conserving liquid squash and stretch during lateral velocity
            val moveDeltaX = kotlin.math.abs(animX.value - targetX.value)
            val maxNormX = tabWidth.value.coerceAtLeast(1f)
            val velocityRatio = (moveDeltaX / maxNormX).coerceIn(0f, 1f)
            val stretchFactorX = 1f + (velocityRatio * 0.22f)
            val squashFactorY = 1f - (velocityRatio * 0.08f)

            val navShape = CircleShape

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .frostedOrganicCapsule(
                        shape = navShape,
                        cornerRadius = 32.dp,
                        tintColor = MaterialTheme.colorScheme.primary,
                        alpha = 0.50f
                    )
            ) {
                if ((localSelectedIndex in 0..3) || dragOffset != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = animX)
                            .size(width = pillWidth, height = innerPillHeight)
                            .graphicsLayer {
                                scaleX = stretchFactorX.coerceIn(1f, 1.42f)
                                scaleY = (1f - ((stretchFactorX - 1f) * 0.65f)).coerceIn(0.72f, 1f)
                                alpha = animScale.coerceIn(0f, 1f)
                                clip = false
                            }
                            .drawBehind {
                                val rPx = size.height / 2f
                                // 1. Liquid droplet body with subtle theme-tinted organic liquid surface
                                val liquidBody = if (isDark) {
                                    Brush.verticalGradient(
                                        0.0f to Color.White.copy(alpha = 0.20f),
                                        0.40f to themePrimary.copy(alpha = 0.35f),
                                        1.0f to themePrimary.copy(alpha = 0.52f)
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        0.0f to Color.White.copy(alpha = 0.72f),
                                        0.45f to themePrimary.copy(alpha = 0.26f),
                                        1.0f to themePrimary.copy(alpha = 0.42f)
                                    )
                                }
                                drawRoundRect(
                                    brush = liquidBody,
                                    size = size,
                                    cornerRadius = CornerRadius(rPx, rPx)
                                )
                            }
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
                                            val idx = (dragOffset!! / tabWidthPx).toInt().coerceIn(0, 3)
                                            localSelectedIndex = idx
                                            lastValidIndex = idx
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
                            isSelected = isSelected
                        )
                    }
                }
            }
        }

        // 2. زر القائمة المربع المنفصل الزجاجي العضوي المطابق للصورة تماماً (Companion Floating Action Squircle)
        val fabShape = RoundedCornerShape(20.dp)
        Box(
            modifier = Modifier
                .size(barHeight)
                .frostedOrganicCapsule(
                    shape = fabShape,
                    cornerRadius = 20.dp,
                    tintColor = MaterialTheme.colorScheme.primary,
                    alpha = 0.50f
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMenuClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isMenuOpen || showSideMenuPill(currentScreen)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                        .drawBehind {
                            val rPx = 15.dp.toPx()
                            val liquidBody = if (isDark) {
                                Brush.verticalGradient(
                                    0.0f to Color.White.copy(alpha = 0.20f),
                                    0.40f to themePrimary.copy(alpha = 0.35f),
                                    1.0f to themePrimary.copy(alpha = 0.52f)
                                )
                            } else {
                                Brush.verticalGradient(
                                    0.0f to Color.White.copy(alpha = 0.72f),
                                    0.45f to themePrimary.copy(alpha = 0.26f),
                                    1.0f to themePrimary.copy(alpha = 0.42f)
                                )
                            }
                            drawRoundRect(
                                brush = liquidBody,
                                size = size,
                                cornerRadius = CornerRadius(rPx, rPx)
                            )
                        }
                )
            }
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "القائمة",
                tint = if (isMenuOpen || showSideMenuPill(currentScreen)) {
                    MaterialTheme.colorScheme.primary
                } else {
                    if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CaliqNavItem(
    item: NavItem,
    isSelected: Boolean
) {
    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = if (isDark) Color.White.copy(alpha = 0.88f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)

    val iconColor by animateColorAsState(
        if (isSelected) activeColor else inactiveColor
    )
    val labelColor by animateColorAsState(
        if (isSelected) activeColor else inactiveColor
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(width = 50.dp, height = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = labelColor,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )
    }
}

@Composable
fun CaliqNavigationRail(
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
    val railWidth = 64.dp

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
            val pillWidth = 54.dp
            val pillHeight = (tabHeight - 6.dp).coerceAtMost(56.dp)

            var dragOffset by remember { mutableStateOf<Float?>(null) }
            var localSelectedIndex by remember { mutableStateOf(selectedIndex) }
            var lastValidIndex by remember { mutableStateOf(if (selectedIndex in 0..3) selectedIndex else 0) }
            LaunchedEffect(selectedIndex) {
                localSelectedIndex = selectedIndex
                if (selectedIndex in 0..3) lastValidIndex = selectedIndex
            }

            val targetY = if (dragOffset != null) {
                with(LocalDensity.current) { (dragOffset!! - (pillHeight.toPx() / 2f)).toDp() }
            } else {
                (tabHeight * lastValidIndex) + (tabHeight - pillHeight) / 2f
            }

            val animY by animateDpAsState(
                targetValue = targetY,
                animationSpec = spring(
                    dampingRatio = 0.60f, // Liquid fluid elasticity
                    stiffness = 220f      // Soft fluid movement following finger seamlessly
                ),
                label = "rail_pill_y"
            )

            val animScale by animateFloatAsState(
                targetValue = if ((localSelectedIndex in 0..3) || dragOffset != null) 1f else 0f,
                animationSpec = spring(dampingRatio = 0.60f, stiffness = 260f),
                label = "rail_pill_scale"
            )

            // Volume-conserving liquid squash and stretch during vertical velocity
            val moveDeltaY = kotlin.math.abs(animY.value - targetY.value)
            val maxNormY = tabHeight.value.coerceAtLeast(1f)
            val velocityRatioY = (moveDeltaY / maxNormY).coerceIn(0f, 1f)
            val stretchFactorY = 1f + (velocityRatioY * 0.22f)
            val squashFactorX = 1f - (velocityRatioY * 0.08f)

            val railShape = RoundedCornerShape(32.dp)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .frostedOrganicCapsule(
                        shape = railShape,
                        cornerRadius = 32.dp,
                        tintColor = MaterialTheme.colorScheme.primary,
                        alpha = 0.50f
                    )
            ) {
                if ((localSelectedIndex in 0..3) || dragOffset != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = animY)
                            .size(width = pillWidth, height = pillHeight)
                            .graphicsLayer {
                                scaleX = (1f - ((stretchFactorY - 1f) * 0.65f)).coerceIn(0.72f, 1f)
                                scaleY = stretchFactorY.coerceIn(1f, 1.42f)
                                alpha = animScale.coerceIn(0f, 1f)
                                clip = false
                            }
                            .drawBehind {
                                val rPx = size.width / 2f
                                // 1. Liquid droplet body with subtle theme-tinted organic liquid surface
                                val liquidBody = if (isDark) {
                                    Brush.verticalGradient(
                                        0.0f to Color.White.copy(alpha = 0.20f),
                                        0.40f to themePrimary.copy(alpha = 0.35f),
                                        1.0f to themePrimary.copy(alpha = 0.52f)
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        0.0f to Color.White.copy(alpha = 0.72f),
                                        0.45f to themePrimary.copy(alpha = 0.26f),
                                        1.0f to themePrimary.copy(alpha = 0.42f)
                                    )
                                }
                                drawRoundRect(
                                    brush = liquidBody,
                                    size = size,
                                    cornerRadius = CornerRadius(rPx, rPx)
                                )
                            }
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
                                            val idx = (dragOffset!! / tabHeightPx).toInt().coerceIn(0, 3)
                                            localSelectedIndex = idx
                                            lastValidIndex = idx
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
                            .weight(1f),
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

        // 2. زر القائمة المربع المنفصل الزجاجي العضوي المطابق للصورة تماماً (Floating Action Squircle)
        val fabShape = RoundedCornerShape(20.dp)
        Box(
            modifier = Modifier
                .size(railWidth)
                .frostedOrganicCapsule(
                    shape = fabShape,
                    cornerRadius = 20.dp,
                    tintColor = MaterialTheme.colorScheme.primary,
                    alpha = 0.50f
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMenuClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isMenuOpen || showSideMenuPill(currentScreen)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                        .drawBehind {
                            val rPx = 15.dp.toPx()
                            val liquidBody = if (isDark) {
                                Brush.verticalGradient(
                                    0.0f to Color.White.copy(alpha = 0.20f),
                                    0.40f to themePrimary.copy(alpha = 0.35f),
                                    1.0f to themePrimary.copy(alpha = 0.52f)
                                )
                            } else {
                                Brush.verticalGradient(
                                    0.0f to Color.White.copy(alpha = 0.72f),
                                    0.45f to themePrimary.copy(alpha = 0.26f),
                                    1.0f to themePrimary.copy(alpha = 0.42f)
                                )
                            }
                            drawRoundRect(
                                brush = liquidBody,
                                size = size,
                                cornerRadius = CornerRadius(rPx, rPx)
                            )
                        }
                )
            }
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "القائمة",
                tint = if (isMenuOpen || showSideMenuPill(currentScreen)) {
                    MaterialTheme.colorScheme.primary
                } else {
                    if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                },
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

