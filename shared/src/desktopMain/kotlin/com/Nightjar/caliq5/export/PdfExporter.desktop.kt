package com.Nightjar.caliq5.export

import com.Nightjar.caliq5.viewmodel.ResultDisplayState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

actual object PdfExporter {
    actual suspend fun export(result: ResultDisplayState) {
        withContext(Dispatchers.IO) {
            try {
                val outDir = File(System.getProperty("user.home"), "Downloads").takeIf { it.exists() }
                    ?: File(System.getProperty("user.home"))
                val file = File(outDir, "caliq5_result_${System.currentTimeMillis()}.txt")
                file.writeText("Caliq 5 Result\n${result}", Charsets.UTF_8)
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file)
                }
            } catch (_: Throwable) {
            }
        }
    }
}
