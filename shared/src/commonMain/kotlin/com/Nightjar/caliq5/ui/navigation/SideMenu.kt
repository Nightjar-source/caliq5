package com.Nightjar.caliq5.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.Nightjar.caliq5.ui.components.neuralExpressiveShadow
import com.Nightjar.caliq5.ui.components.neuralAmbientGlow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import caliq5.shared.generated.resources.Res
import caliq5.shared.generated.resources.icon
import com.Nightjar.caliq5.data.Screen
import com.Nightjar.caliq5.data.sideMenuItems

/**
 * Side menu drawer in authentic Material 3.
 */
@Composable
fun CaliqSideMenu(
    viewModel: com.Nightjar.caliq5.viewmodel.AppViewModel,
    onClose: () -> Unit,
    onNavigate: (Screen) -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight(),
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        drawerTonalElevation = 0.dp,
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Start + WindowInsetsSides.End + WindowInsetsSides.Vertical)
    ) {
        SideMenuHeader()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.99f }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.05f to Color.Black,
                            0.95f to Color.Black,
                            1.0f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            sideMenuItems.forEach { item ->
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Start
                        )
                    },
                    selected = false,
                    onClick = { onNavigate(item.screen) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    shape = RoundedCornerShape(28.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
private fun SideMenuHeader() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val pColor = MaterialTheme.colorScheme.primary
    val sColor = MaterialTheme.colorScheme.secondary
    val pContainer = MaterialTheme.colorScheme.primaryContainer
    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        shadowElevation = 3.5.dp,
        border = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            pColor.copy(alpha = if (isDark) 0.14f else 0.09f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(95.dp)
            ) {
                // Floating shapes around the icon matching caliq5.html: '✦  ★  ∆  +'
                val shapes = listOf("✦", "★", "∆", "+")
                shapes.forEachIndexed { index, shape ->
                    val baseAngle = index * 90f
                    Text(
                        text = shape,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = pColor.copy(alpha = 0.50f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .graphicsLayer {
                                // Draw phase deferral: Reading floatAnim exclusively inside graphicsLayer lambda
                                // achieves Zero Recompositions and locked 120 FPS
                                val currentAnim = floatAnim
                                val angleDeg = baseAngle + (currentAnim * 12f)
                                val rad = angleDeg * (kotlin.math.PI / 180f).toFloat()
                                val distance = 46f + (currentAnim * 3f)
                                val transY = currentAnim * -5f
                                translationX = kotlin.math.cos(rad) * distance * density
                                translationY = (kotlin.math.sin(rad) * distance * density) + (transY * density)
                                rotationZ = (currentAnim * 15f) - 7.5f
                            }
                    )
                }

                // 80dp App Icon - Clean & Borderless
                val painter = org.jetbrains.compose.resources.painterResource(Res.drawable.icon)
                androidx.compose.foundation.Image(
                    painter = painter,
                    contentDescription = "أيقونة التطبيق",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .shadow(4.dp, CircleShape)
                )
            }

            com.Nightjar.caliq5.ui.components.ShimmeringAppTitle(
                text = "حاسبة الاعفاء والسعي",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    fontFamily = com.Nightjar.caliq5.theme.ReemKufiFontFamily
                ),
            )
        }
    }
}
}


