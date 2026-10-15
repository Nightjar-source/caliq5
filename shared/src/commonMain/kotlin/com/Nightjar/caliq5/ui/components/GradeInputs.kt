package com.Nightjar.caliq5.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.runtime.*

@Composable
fun GradeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    showPercent: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    enabled: Boolean = true,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    var tfv by remember(value) {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val shape = RoundedCornerShape(16.dp)
    OutlinedTextField(
        value = tfv,
        onValueChange = { newVal ->
            if (keyboardType == KeyboardType.Decimal || keyboardType == KeyboardType.Number) {
                val cleaned = cleanGradeInput(newVal.text)
                if (cleaned != null) {
                    val justAddedCommaOrDot = (newVal.text.endsWith(".") || newVal.text.endsWith(",") || newVal.text.endsWith("،") || newVal.text.endsWith("٫"))
                    val targetSelection = if (cleaned == "0." && justAddedCommaOrDot) {
                        TextRange(2)
                    } else if (cleaned.contains(".") && justAddedCommaOrDot) {
                        TextRange(cleaned.indexOf('.') + 1)
                    } else {
                        val sel = newVal.selection.end.coerceIn(0, cleaned.length)
                        TextRange(sel)
                    }
                    tfv = TextFieldValue(text = cleaned, selection = targetSelection)
                    onValueChange(cleaned)
                }
            } else {
                tfv = newVal
                onValueChange(newVal.text)
            }
        },
        label = if (label.isNotEmpty()) { { Text(label) } } else null,
        placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder) } } else null,
        isError = isError,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        trailingIcon = if (showPercent) { { Text("%") } } else null,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.40f else 0.50f),
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.40f else 0.50f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.25f else 0.35f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.20f else 0.25f),
            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.25f else 0.35f),
            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            disabledPlaceholderColor = com.Nightjar.caliq5.theme.successColor.copy(alpha = 0.85f)
        )
    )
}

@Composable
fun SmallGradeInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isReadOnly: Boolean = false,
    isHighlighted: Boolean = false
) {
    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val shape = RoundedCornerShape(16.dp)

    val containerColor = if (isHighlighted) {
        primaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.25f else 0.35f)
    }
    
    val borderColor = if (isHighlighted) {
        primaryColor
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.45f)
    }
    
    val dynamicFontSize = when {
        value.length > 8 -> 14.sp
        value.length > 5 -> 15.sp
        else -> 16.sp
    }
    
    var tfv by remember(value) {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    Box(
        modifier = modifier
            .background(containerColor, shape)
            .border(1.dp, borderColor, shape)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = tfv,
            onValueChange = { newVal ->
                val cleaned = cleanGradeInput(newVal.text)
                if (cleaned != null) {
                    val justAddedCommaOrDot = (newVal.text.endsWith(".") || newVal.text.endsWith(",") || newVal.text.endsWith("،") || newVal.text.endsWith("٫"))
                    val targetSelection = if (cleaned == "0." && justAddedCommaOrDot) {
                        TextRange(2)
                    } else if (cleaned.contains(".") && justAddedCommaOrDot) {
                        TextRange(cleaned.indexOf('.') + 1)
                    } else {
                        val sel = newVal.selection.end.coerceIn(0, cleaned.length)
                        TextRange(sel)
                    }
                    tfv = TextFieldValue(text = cleaned, selection = targetSelection)
                    onValueChange(cleaned)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                textAlign = TextAlign.Center, 
                color = if (isHighlighted) primaryColor else onSurface, 
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = dynamicFontSize
            ),
            cursorBrush = SolidColor(primaryColor),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            enabled = enabled,
            readOnly = isReadOnly,
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.Center) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = MaterialTheme.typography.bodyMedium.copy(color = onSurfaceVariant.copy(alpha = 0.55f), fontSize = 14.sp, fontWeight = FontWeight.Medium), maxLines = 1)
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun CaliqOutlinedInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "أدخل الدرجة",
    isReadOnly: Boolean = false,
    enabled: Boolean = true,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = com.Nightjar.caliq5.theme.isAppInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val shape = RoundedCornerShape(14.dp)

    val containerColor = if (isHighlighted) {
        primaryContainer.copy(alpha = 0.35f)
    } else if (isDark) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val borderColor = if (isHighlighted) {
        primaryColor
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.45f else 0.38f)
    }
    
    var tfv by remember(value) {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    Box(
        modifier = modifier
            .background(containerColor, shape)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = tfv,
            onValueChange = { newVal ->
                val cleaned = cleanGradeInput(newVal.text)
                if (cleaned != null) {
                    val justAddedCommaOrDot = (newVal.text.endsWith(".") || newVal.text.endsWith(",") || newVal.text.endsWith("،") || newVal.text.endsWith("٫"))
                    val targetSelection = if (cleaned == "0." && justAddedCommaOrDot) {
                        TextRange(2)
                    } else if (cleaned.contains(".") && justAddedCommaOrDot) {
                        TextRange(cleaned.indexOf('.') + 1)
                    } else {
                        val sel = newVal.selection.end.coerceIn(0, cleaned.length)
                        TextRange(sel)
                    }
                    tfv = TextFieldValue(text = cleaned, selection = targetSelection)
                    onValueChange(cleaned)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                textAlign = TextAlign.Center, 
                color = if (isHighlighted) primaryColor else onSurface, 
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(primaryColor),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            enabled = enabled,
            readOnly = isReadOnly,
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.Center) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = MaterialTheme.typography.bodyMedium.copy(color = onSurfaceVariant.copy(alpha = 0.55f), fontSize = 14.sp, fontWeight = FontWeight.Medium), maxLines = 1)
                    }
                    innerTextField()
                }
            }
        )
    }
}