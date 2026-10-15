package com.Nightjar.caliq5

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Nightjar.caliq5.theme.CaliqTheme
import com.Nightjar.caliq5.ui.navigation.AppNavigation
import com.Nightjar.caliq5.ui.splash.SplashScreen
import com.Nightjar.caliq5.viewmodel.AppViewModel

/**
 * Root composable - entry point for all platforms.
 * Sets up:
 * 1. RTL layout direction (Arabic UI)
 * 2. CaliqTheme (Material 3 with dark/light support)
 * 3. Splash screen (2.5s animated)
 * 4. Main app navigation
 */
@Composable
fun App(
    deepLinkUri: String? = null,
    isRestoredFromProcessDeath: Boolean = false,
    onSplashFinishedAction: (() -> Unit)? = null
) {
    val viewModel: AppViewModel = viewModel()
    
    LaunchedEffect(isRestoredFromProcessDeath) {
        viewModel.initLaunchState(isRestoredFromProcessDeath)
    }
    
    var lastHandledUri by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
    
    LaunchedEffect(deepLinkUri) {
        if (deepLinkUri != null && deepLinkUri != lastHandledUri) {
            lastHandledUri = deepLinkUri
            val cleanUri = deepLinkUri.substringBefore("?")
            when {
                cleanUri.equals("caliq://exemption", ignoreCase = true) -> viewModel.resetToDeepLink(com.Nightjar.caliq5.data.Screen.EXEMPTION_SELECT)
                cleanUri.equals("caliq://annual", ignoreCase = true) -> viewModel.resetToDeepLink(com.Nightjar.caliq5.data.Screen.ANNUAL_AVERAGE)
                cleanUri.equals("caliq://monthly", ignoreCase = true) -> viewModel.resetToDeepLink(com.Nightjar.caliq5.data.Screen.MONTHLY_AVERAGE)
                cleanUri.equals("caliq://term2", ignoreCase = true) -> viewModel.resetToDeepLink(com.Nightjar.caliq5.data.Screen.TERM2_CALCULATION)
                cleanUri.equals("caliq://grades", ignoreCase = true) -> viewModel.resetToDeepLink(com.Nightjar.caliq5.data.Screen.MY_GRADES)
                cleanUri.equals("caliq://statistics", ignoreCase = true) -> viewModel.resetToDeepLink(com.Nightjar.caliq5.data.Screen.STATISTICS)
                cleanUri.equals("caliq://appearance", ignoreCase = true) -> viewModel.resetToDeepLink(com.Nightjar.caliq5.data.Screen.APPEARANCE)
                cleanUri.equals("caliq://about", ignoreCase = true) -> viewModel.resetToDeepLink(com.Nightjar.caliq5.data.Screen.ABOUT)
            }
        }
    }

    val settings by viewModel.settings.collectAsState()

    // Force RTL layout direction for Arabic UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        CaliqTheme(settings = settings) {
            var showSplash by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }

            AnimatedContent(
                targetState = showSplash,
                transitionSpec = {
                    (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(800)) +
                     androidx.compose.animation.scaleIn(initialScale = 0.95f, animationSpec = androidx.compose.animation.core.spring(0.8f, 100f))) togetherWith
                    (androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500)) +
                     androidx.compose.animation.scaleOut(targetScale = 1.05f, animationSpec = androidx.compose.animation.core.tween(500)))
                },
                label = "SplashTransition"
            ) { isSplash ->
                if (isSplash) {
                    SplashScreen(
                        onSplashFinished = { 
                            showSplash = false
                            onSplashFinishedAction?.invoke()
                        }
                    )
                } else {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
