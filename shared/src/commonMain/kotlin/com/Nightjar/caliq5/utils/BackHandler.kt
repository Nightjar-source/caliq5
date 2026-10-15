package com.Nightjar.caliq5.utils

import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
