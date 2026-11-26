package com.Nightjar.caliq5

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(width = 1100.dp, height = 780.dp)
    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "حاسبة الإعفاء والسعي لطلاب العراق"
    ) {
        App()
    }
}
