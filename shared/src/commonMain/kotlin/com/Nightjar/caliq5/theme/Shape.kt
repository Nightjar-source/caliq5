package com.Nightjar.caliq5.theme

import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Matching the CSS border-radius values:
// --md-sys-border-radius: 20px
// --md-sys-border-radius-large: 32px
val CaliqShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(20.dp),     // --md-sys-border-radius
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp), // --md-sys-border-radius-large (pill/FAB)
)
