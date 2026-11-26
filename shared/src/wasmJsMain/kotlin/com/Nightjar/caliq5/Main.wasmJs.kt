package com.Nightjar.caliq5

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun hidePwaLoader(): Unit = js("document.getElementById('pwa-loader')?.remove()")

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    hidePwaLoader()
    ComposeViewport(viewportContainerId = "ComposeTarget") {
        App()
    }
}
