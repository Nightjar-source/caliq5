package com.Nightjar.caliq5.export

import com.Nightjar.caliq5.viewmodel.ResultDisplayState

private fun triggerBrowserPrint(): Unit = js("window.print()")

actual object PdfExporter {
    actual suspend fun export(result: ResultDisplayState) {
        try {
            triggerBrowserPrint()
        } catch (_: Throwable) {
        }
    }
}
