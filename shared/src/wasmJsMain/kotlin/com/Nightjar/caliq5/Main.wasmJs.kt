package com.Nightjar.caliq5

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

private fun hidePwaLoader(): Unit = js(
    """
    var el = document.getElementById('pwa-loader');
    if (el && el.parentNode) { el.parentNode.removeChild(el); }
    """
)

@OptIn(ExperimentalComposeUiApi::class, kotlin.js.ExperimentalWasmJsInterop::class)
fun main() {
    hidePwaLoader()
    ComposeViewport(viewportContainerId = "ComposeTarget") {
        App()
    }
}
