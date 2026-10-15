package com.Nightjar.caliq5.export

import com.Nightjar.caliq5.viewmodel.ResultDisplayState

/**
 * Platform-agnostic PDF exporter.
 */
expect object PdfExporter {
    suspend fun export(result: ResultDisplayState)
}
