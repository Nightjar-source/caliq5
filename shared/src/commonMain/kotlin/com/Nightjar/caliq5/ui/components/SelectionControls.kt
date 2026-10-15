package com.Nightjar.caliq5.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.zIndex
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.Nightjar.caliq5.theme.contrastingTextColor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Ultra-fluid class card selection button with seamless background gradient transition,
 * constant 3D glass border with zero delayed render artifacts, and floating spring badge.
 */
@Composable
fun ClassCardButton(
    title: String = "",
    subtitle: String = "",
    text: String = "",
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val displayTitle = if (title.isNotEmpty()) title else text
    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    val interactionSource = remember { MutableInteractionSource() }
    val isInteractionPressed by rememberExpressivePressState(interactionSource)
    val isInstantPressed = remember { mutableStateOf(false) }
    val isPressed = isInteractionPressed || isInstantPressed.value

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 10.dp else 18.dp,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 450f
        ),
        label = "class_card_corner"
    )
    val shape = RoundedCornerShape(cornerRadius)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 450f
        ),
        label = "class_card_scale"
    )

    val borderBrush = if (selected) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                primaryColor.copy(alpha = 0.90f),
                secondaryColor.copy(alpha = 0.85f),
                primaryColor.copy(alpha = 0.90f),
                Color.White.copy(alpha = 0.95f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.65f else 0.85f),
                primaryColor.copy(alpha = if (isDark) 0.70f else 0.50f),
                primaryContainer.copy(alpha = if (isDark) 0.45f else 0.35f),
                primaryColor.copy(alpha = if (isDark) 0.70f else 0.50f),
                Color.White.copy(alpha = if (isDark) 0.65f else 0.85f)
            )
        )
    }

    val backgroundBrush = if (selected) {
        Brush.linearGradient(
            colors = listOf(
                primaryColor,
                primaryColor.copy(alpha = 0.92f),
                secondaryColor.copy(alpha = 0.90f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                primaryContainer.copy(alpha = if (isDark) 0.75f else 0.90f),
                primaryColor.copy(alpha = if (isDark) 0.45f else 0.65f)
            )
        )
    }

    val titleColor = if (selected || isDark) {
        Color.White
    } else {
        primaryColor.contrastingTextColor(
            darkText = Color(0xFF0F172A),
            lightText = Color.White
        )
    }

    val subtitleColor = if (selected) {
        Color.White.copy(alpha = 0.90f)
    } else if (isDark) {
        Color.White.copy(alpha = 0.82f)
    } else {
        titleColor.copy(alpha = 0.78f)
    }

    Box(
        modifier = modifier
            .instantExpressivePress { isInstantPressed.value = it }
            .scale(scale)
            .clip(shape)
            .background(backgroundBrush, shape)
            .border(
                width = if (selected) 1.6.dp else 1.2.dp,
                brush = borderBrush,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 14.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                val textShadow = androidx.compose.ui.graphics.Shadow(
                    color = (if (titleColor == Color.White) Color.Black else Color.White).copy(alpha = 0.40f),
                    offset = androidx.compose.ui.geometry.Offset(0f, 1.5f),
                    blurRadius = 3f
                )
                Text(
                    text = displayTitle,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = titleColor,
                        fontSize = 14.sp,
                        shadow = textShadow
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = subtitleColor,
                            fontSize = 11.5.sp,
                            shadow = textShadow
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(18.dp)
                        .background(Color.White.copy(alpha = 0.28f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.75f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "تم الاختيار",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

/**
 * Interactive Primary Button with M3 Expressive 2026 shape-morphing spring physics.
 * Morphs dynamically from full rounded pill (24.dp) to soft squircle (8.dp) with scale = 0.92f.
 */
@Composable
fun CaliqPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isInteractionPressed by rememberExpressivePressState(interactionSource)
    val isInstantPressed = remember { mutableStateOf(false) }
    val isPressed = (isInteractionPressed || isInstantPressed.value) && enabled

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 450f
        ),
        label = "btn_primary_corner_morph"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 450f
        ),
        label = "btn_primary_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = spring(
            dampingRatio = 0.50f,
            stiffness = 380f
        ),
        label = "btn_primary_elevation"
    )

    val animatedShape = RoundedCornerShape(cornerRadius)

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .instantExpressivePress(enabled = enabled) { isInstantPressed.value = it }
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = animatedShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.50f)
            ),
        shape = animatedShape,
        contentPadding = PaddingValues(vertical = 15.dp, horizontal = 24.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

/**
 * Modifier to ensure M3 Expressive instant pointer response at Frame 0 (0ms)
 * without waiting for Compose's scroll-container tapTimeout (~100ms).
 * Keeps the pressed state active continuously as long as the pointer is held down,
 * and enforces a minimum visual feedback duration [minDurationMillis] for rapid taps.
 */
fun Modifier.instantExpressivePress(
    minDurationMillis: Long = 260L,
    enabled: Boolean = true,
    onPressedChange: (Boolean) -> Unit
): Modifier = if (!enabled) this else this.pointerInput(minDurationMillis) {
    kotlinx.coroutines.coroutineScope {
        val outerScope = this
        awaitPointerEventScope {
            var pressMark: kotlin.time.TimeMark? = null
            var releaseJob: kotlinx.coroutines.Job? = null
            val timeSource = kotlin.time.TimeSource.Monotonic

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val anyPressed = event.changes.any { it.pressed }
                if (anyPressed) {
                    releaseJob?.cancel()
                    if (pressMark == null) {
                        pressMark = timeSource.markNow()
                        onPressedChange(true)
                    }
                } else {
                    if (pressMark != null) {
                        val mark = pressMark
                        pressMark = null
                        val elapsed = mark.elapsedNow().inWholeMilliseconds
                        val remaining = if (elapsed < minDurationMillis) minDurationMillis - elapsed else 0L
                        releaseJob = outerScope.launch {
                            if (remaining > 0L) {
                                kotlinx.coroutines.delay(remaining)
                            }
                            onPressedChange(false)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper to ensure M3 Expressive 2026 shape-morphing spring physics plays visibly
 * even on an instant 10ms single tap, while also supporting hold-down.
 */
@Composable
fun rememberExpressivePressState(
    interactionSource: MutableInteractionSource,
    minDurationMillis: Long = 320L
): State<Boolean> {
    val isPressedState = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(interactionSource, minDurationMillis) {
        val timeSource = kotlin.time.TimeSource.Monotonic
        var pressMark: kotlin.time.TimeMark? = null
        var releaseJob: kotlinx.coroutines.Job? = null
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is androidx.compose.foundation.interaction.PressInteraction.Press -> {
                    releaseJob?.cancel()
                    pressMark = timeSource.markNow()
                    isPressedState.value = true
                }
                is androidx.compose.foundation.interaction.PressInteraction.Release -> {
                    releaseJob?.cancel()
                    val elapsedMillis = pressMark?.elapsedNow()?.inWholeMilliseconds ?: 0L
                    val remainingMillis = if (elapsedMillis < minDurationMillis) minDurationMillis - elapsedMillis else 0L
                    releaseJob = scope.launch {
                        if (remainingMillis > 0L) {
                            kotlinx.coroutines.delay(remainingMillis)
                        }
                        isPressedState.value = false
                    }
                }
                is androidx.compose.foundation.interaction.PressInteraction.Cancel -> {
                    releaseJob?.cancel()
                    isPressedState.value = false
                }
            }
        }
    }
    return isPressedState
}

/**
 * Expressive Floating Toggle Chip (used for "غير مشمولة / مشمولة")
 * Fluid spring physics, instant 0ms touch response, and stable floating pill shape that never clips.
 */
@Composable
fun CaliqToggleChip(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checkedColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedColor: Color = com.Nightjar.caliq5.theme.errorColor,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isInteractionPressed by rememberExpressivePressState(interactionSource)
    val isInstantPressed = remember { mutableStateOf(false) }
    val isPressed = (isInteractionPressed || isInstantPressed.value) && enabled

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 20.dp,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 450f
        ),
        label = "chip_corner_morph"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 450f
        ),
        label = "chip_scale"
    )

    val activeColor by animateColorAsState(
        targetValue = if (checked) checkedColor else uncheckedColor,
        animationSpec = spring(dampingRatio = 0.70f, stiffness = 400f),
        label = "chip_color_spring"
    )
    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val animatedShape = RoundedCornerShape(cornerRadius)

    // Solid floating background so it remains a pure clean floating button without any dimming or color bleeding
    val chipBgColor = if (isDark) {
        MaterialTheme.colorScheme.surface
    } else {
        Color.White
    }

    Surface(
        onClick = onCheckedChange,
        enabled = enabled,
        shape = animatedShape,
        color = chipBgColor,
        border = BorderStroke(
            width = 1.4.dp,
            color = activeColor
        ),
        shadowElevation = if (isPressed) 1.dp else 3.5.dp,
        interactionSource = interactionSource,
        modifier = modifier
            .zIndex(10f)
            .instantExpressivePress(enabled = enabled) { isInstantPressed.value = it }
            .scale(scale)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

/**
 * Universal Button with M3 Expressive 2026 shape-morphing spring physics (24.dp -> 6.dp, scale = 0.90f).
 */
@Composable
fun CaliqButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isInteractionPressed by rememberExpressivePressState(interactionSource)
    val isInstantPressed = remember { mutableStateOf(false) }
    val isPressed = (isInteractionPressed || isInstantPressed.value) && enabled

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 450f
        ),
        label = "caliq_btn_corner_morph"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 450f
        ),
        label = "caliq_btn_scale"
    )

    val effectiveShape = if (isPressed) RoundedCornerShape(cornerRadius) else (shape ?: RoundedCornerShape(cornerRadius))

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .instantExpressivePress(enabled = enabled) { isInstantPressed.value = it }
            .scale(scale),
        shape = effectiveShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Universal OutlinedButton with M3 Expressive 2026 shape-morphing spring physics (24.dp -> 6.dp, scale = 0.90f).
 */
@Composable
fun CaliqOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape? = null,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isInteractionPressed by rememberExpressivePressState(interactionSource)
    val isInstantPressed = remember { mutableStateOf(false) }
    val isPressed = (isInteractionPressed || isInstantPressed.value) && enabled

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 450f
        ),
        label = "caliq_outlined_btn_corner_morph"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 450f
        ),
        label = "caliq_outlined_btn_scale"
    )

    val effectiveShape = if (isPressed) RoundedCornerShape(cornerRadius) else (shape ?: RoundedCornerShape(cornerRadius))

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .instantExpressivePress(enabled = enabled) { isInstantPressed.value = it }
            .scale(scale),
        shape = effectiveShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Universal TextButton with M3 Expressive 2026 shape-morphing spring physics (24.dp -> 8.dp, scale = 0.92f).
 */
@Composable
fun CaliqTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape? = null,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isInteractionPressed by rememberExpressivePressState(interactionSource)
    val isInstantPressed = remember { mutableStateOf(false) }
    val isPressed = (isInteractionPressed || isInstantPressed.value) && enabled

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = 0.50f,
            stiffness = 380f
        ),
        label = "caliq_text_btn_corner_morph"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.50f,
            stiffness = 380f
        ),
        label = "caliq_text_btn_scale"
    )

    val effectiveShape = if (isPressed) {
        RoundedCornerShape(8.dp)
    } else {
        shape ?: RoundedCornerShape(cornerRadius)
    }

    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .instantExpressivePress(enabled = enabled) { isInstantPressed.value = it }
            .scale(scale),
        shape = effectiveShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun CaliqIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isInteractionPressed by rememberExpressivePressState(interactionSource)
    val isInstantPressed = remember { mutableStateOf(false) }
    val isPressed = (isInteractionPressed || isInstantPressed.value) && enabled

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.50f,
            stiffness = 380f
        ),
        label = "icon_btn_scale"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 20.dp,
        animationSpec = spring(
            dampingRatio = 0.50f,
            stiffness = 380f
        ),
        label = "icon_btn_corner"
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier
            .instantExpressivePress(enabled = enabled) { isInstantPressed.value = it }
            .size(40.dp)
            .scale(scale)
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
fun CaliqSnackbar(
    message: String,
    isError: Boolean,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (visible) {
        val containerColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        val contentColor = Color.White

        Surface(
            modifier = modifier
                .widthIn(min = 200.dp, max = 560.dp)
                .wrapContentWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.80f,
                        stiffness = 380f
                    ),
                    alignment = Alignment.Center
                )
                .neuralAmbientGlow(cornerRadius = 24.dp, elevation = 4.dp, tintColor = containerColor, hasSaltMotes = false),
            shape = RoundedCornerShape(24.dp),
            color = containerColor,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedContent(
                    targetState = isError,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(160))
                    },
                    label = "snackbar_icon_anim"
                ) { errorState ->
                    Icon(
                        imageVector = if (errorState) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                AnimatedContent(
                    targetState = message,
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInVertically(
                            animationSpec = tween(220),
                            initialOffsetY = { it / 3 }
                        )) togetherWith
                        (fadeOut(tween(180)) + slideOutVertically(
                            animationSpec = tween(180),
                            targetOffsetY = { -it / 3 }
                        ))
                    },
                    label = "snackbar_text_anim"
                ) { targetMsg ->
                    Text(
                        text = targetMsg,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        ),
                        textAlign = TextAlign.Justify,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String = "تأكيد الحذف",
    message: String = "هل أنت متأكد من الحذف؟",
    text: String = ""
) {
    val displayMessage = if (text.isNotEmpty()) text else message
    var isVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val dismissWithAnim: (action: () -> Unit) -> Unit = { action ->
        coroutineScope.launch {
            isVisible = false
            delay(170)
            action()
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { dismissWithAnim(onDismiss) },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { dismissWithAnim(onDismiss) }
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(170)) + 
                        scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.68f, stiffness = 380f)),
                exit = fadeOut(animationSpec = tween(150)) + 
                       scaleOut(targetScale = 0.85f, animationSpec = tween(150))
            ) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 400.dp)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Intercept clicks inside card
                        )
                        .neuralAmbientGlow(cornerRadius = 28.dp, elevation = 8.dp, tintColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            displayMessage,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 24.sp
                            ),
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CaliqOutlinedButton(
                                onClick = { dismissWithAnim(onDismiss) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text("إلغاء", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            }
                            CaliqButton(
                                onClick = { dismissWithAnim(onConfirm) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Text("حذف", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Universal Material 3 Expressive Confirmation Dialog:
 * - Fluid scale & spring bounce entrance/exit.
 * - Always keeps two buttons side-by-side (Row with equal weight).
 * - Justified text alignment for elegant readability.
 * - Neural ambient glow matching primary theme color.
 */
@Composable
fun CaliqConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "تأكيد",
    dismissText: String = "إلغاء",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val dismissWithAnim: (action: () -> Unit) -> Unit = { action ->
        coroutineScope.launch {
            isVisible = false
            delay(170)
            action()
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { dismissWithAnim(onDismiss) },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { dismissWithAnim(onDismiss) }
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(170)) + 
                        scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.68f, stiffness = 380f)),
                exit = fadeOut(animationSpec = tween(150)) + 
                       scaleOut(targetScale = 0.85f, animationSpec = tween(150))
            ) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .neuralAmbientGlow(
                            cornerRadius = 28.dp,
                            elevation = 8.dp,
                            tintColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            message,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 24.sp
                            ),
                            textAlign = TextAlign.Justify,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CaliqOutlinedButton(
                                onClick = { dismissWithAnim(onDismiss) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    dismissText,
                                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                            CaliqButton(
                                onClick = { dismissWithAnim(onConfirm) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    contentColor = if (isDestructive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Text(
                                    confirmText,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
