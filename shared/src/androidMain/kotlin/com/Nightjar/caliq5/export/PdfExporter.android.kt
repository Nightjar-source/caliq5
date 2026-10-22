package com.Nightjar.caliq5.export

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.Nightjar.caliq5.CaliqApplication
import com.Nightjar.caliq5.viewmodel.ResultDisplayState
import com.Nightjar.caliq5.data.TermMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat

actual object PdfExporter {
    actual suspend fun export(result: ResultDisplayState) {
        withContext(Dispatchers.IO) {
            val context = CaliqApplication.appContext
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try { context.startActivity(fallbackIntent) } catch (e: Exception) {}
                    }
                }
            }

            val docTitle = result.title
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val fileName = when (result) {
                is ResultDisplayState.StudentProfileResult -> {
                    "تطبيق الاعفاء والسعي - درجات ${result.student.name} - $timeStamp.pdf"
                }
                else -> {
                    "تطبيق الاعفاء والسعي - ${result.title} - $timeStamp.pdf"
                }
            }

            val fontBytes = try {
                caliq5.shared.generated.resources.Res.readBytes("font/reemkufi_bold.ttf")
            } catch (e: Exception) { null }
            
            val customTypeface = fontBytes?.let { bytes ->
                try {
                    val tempFile = File.createTempFile("reemkufi_bold", "ttf", context.cacheDir)
                    tempFile.deleteOnExit()
                    FileOutputStream(tempFile).use { it.write(bytes) }
                    Typeface.createFromFile(tempFile)
                } catch (e: Exception) { null }
            }
            
            val defaultTypeface = Typeface.DEFAULT
            val appSettings = com.Nightjar.caliq5.viewmodel.DataRepository.loadSettings()

            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            
            var totalPages = 1
            fun calculateTotalPages() {
                var tempY = 160f
                fun addY(height: Float) {
                    tempY += height
                    if (tempY > pageHeight - 100f) {
                        totalPages++
                        tempY = 80f + height
                    }
                }
                when (result) {
                    is ResultDisplayState.ExemptionResult -> {
                        addY(140f)
                        addY(40f + 2 * 25f + 25f)
                        if (result.data.individualExemptions.isNotEmpty()) addY(40f + result.data.individualExemptions.size * 25f + 25f)
                        addY(35f)
                        for (g in result.data.grades) addY(28f)
                    }
                    is ResultDisplayState.AnnualResult, is ResultDisplayState.MonthlyResult -> {
                        addY(140f)
                        addY(40f + 25f + 25f)
                        addY(35f)
                        val gradesSize = if (result is ResultDisplayState.AnnualResult) result.data.grades.size else (result as ResultDisplayState.MonthlyResult).data.grades.size
                        for (i in 0 until gradesSize) addY(28f)
                    }
                    is ResultDisplayState.Term2Result -> {
                        addY(140f)
                        addY(140f)
                        addY(40f + 3 * 25f + 25f)
                        addY(35f)
                        for (g in result.data.grades) addY(28f)
                    }
                    is ResultDisplayState.StudentProfileResult -> {
                        addY(30f + 40f + 35f)
                        for (r in result.student.subjects.values) addY(35f)
                    }
                }
            }
            calculateTotalPages()

            var iconBitmap: android.graphics.Bitmap? = null
            try {
                val splashResId = context.resources.getIdentifier("splash_icon", "drawable", context.packageName)
                if (splashResId != 0) {
                    try {
                        val options = android.graphics.BitmapFactory.Options().apply {
                            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                        }
                        iconBitmap = android.graphics.BitmapFactory.decodeResource(context.resources, splashResId, options)
                    } catch (e: Exception) {}
                }
                if (iconBitmap == null) {
                    var drawable: android.graphics.drawable.Drawable? = null
                    try {
                        drawable = context.packageManager.getApplicationIcon(context.packageName)
                    } catch (e: Exception) {}
                    if (drawable == null) {
                        val appIconId = context.resources.getIdentifier("ic_launcher", "mipmap", context.packageName)
                        if (appIconId != 0) {
                            drawable = androidx.core.content.ContextCompat.getDrawable(context, appIconId)
                        }
                    }
                    if (drawable != null) {
                        val iconSizePx = 512
                        val bmp = android.graphics.Bitmap.createBitmap(iconSizePx, iconSizePx, android.graphics.Bitmap.Config.ARGB_8888)
                        val iconCanvas = android.graphics.Canvas(bmp)
                        drawable.setBounds(0, 0, iconSizePx, iconSizePx)
                        drawable.draw(iconCanvas)
                        iconBitmap = bmp
                    }
                }
            } catch (e: Exception) {}

            var currentPage = document.startPage(pageInfo)
            var canvas = currentPage.canvas
            var pageNumber = 1

            fun drawWatermark(c: Canvas) {
                if (iconBitmap != null) {
                    val wmSize = minOf(pageWidth.toFloat(), pageHeight.toFloat()) * 0.48f
                    val wmLeft = (pageWidth - wmSize) / 2f
                    val wmTop = (pageHeight - wmSize) / 2f
                    val wmRect = android.graphics.RectF(wmLeft, wmTop, wmLeft + wmSize, wmTop + wmSize)
                    val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                        alpha = 18 // <= 10% opacity (~7%), subtle and elegant watermark
                    }
                    c.drawBitmap(iconBitmap, null, wmRect, wmPaint)
                }
            }

            fun drawPageFooter(c: Canvas, num: Int) {
                val footerY = pageHeight - 60f
                val p = Paint().apply { textSize = 9f; color = Color.BLACK; typeface = defaultTypeface; textAlign = Paint.Align.RIGHT }
                val dashedPaint = Paint().apply {
                    color = Color.parseColor("#79747E")
                    strokeWidth = 1f
                    pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
                }
                val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.US)
                val rawDate = sdf.format(Date())
                val dateStr = rawDate.replace("AM", "صباحاً").replace("PM", "مساءً")
                c.drawLine(30f, footerY, pageWidth - 30f, footerY, dashedPaint)
                c.drawText("الإنشاء بواسطة: تطبيق حاسبة الاعفاء والسعي 2026", pageWidth - 30f, footerY + 25f, p)
                val pageStr = if (totalPages <= 1) "" else " | صفحة $num / $totalPages"
                drawText(c, "تاريخ الطباعة: $dateStr$pageStr", 30f, footerY + 25f, 9f, Color.BLACK, false, Paint.Align.LEFT, defaultTypeface)
            }

            fun startNewPage() {
                drawPageFooter(canvas, pageNumber)
                document.finishPage(currentPage)
                pageNumber++
                currentPage = document.startPage(pageInfo)
                canvas = currentPage.canvas
                canvas.drawColor(Color.WHITE)
                drawWatermark(canvas)
                drawText(canvas, "تطبيق الاعفاء والسعي - تابع لـ $docTitle", pageWidth - 30f, 40f, 10f, Color.GRAY, false, Paint.Align.RIGHT, defaultTypeface)
                val settings = appSettings
                
                val primaryColor = try {
                    if (settings.useMaterialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        androidx.compose.material3.dynamicLightColorScheme(context).primary.let {
                            Color.argb((it.alpha * 255).toInt(), (it.red * 255).toInt(), (it.green * 255).toInt(), (it.blue * 255).toInt())
                        }
                    } else {
                        val themeHex = settings.primaryColor ?: "#ec4899"
                        val parsed = Color.parseColor(themeHex)
                        if (themeHex.equals("#800020", ignoreCase = true) || themeHex.equals("#9E1B3E", ignoreCase = true)) Color.parseColor("#d41145") else parsed
                    }
                } catch(e:Exception) { Color.parseColor("#ec4899") }
                
                val containerColor = try {
                    val hsv = FloatArray(3)
                    Color.colorToHSV(primaryColor, hsv)
                    hsv[1] *= 0.2f
                    hsv[2] = 0.95f
                    Color.HSVToColor(hsv)
                } catch(e:Exception) { Color.parseColor("#FCE4EC") }
                val tableRowColor = Color.parseColor("#F4EFF4")

                canvas.drawLine(30f, 55f, pageWidth - 30f, 55f, Paint().apply { color = containerColor; strokeWidth = 1f })
            }

            val settings = com.Nightjar.caliq5.viewmodel.DataRepository.loadSettings()
            
                val primaryColor = try {
                    if (settings.useMaterialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        androidx.compose.material3.dynamicLightColorScheme(context).primary.let {
                            Color.argb((it.alpha * 255).toInt(), (it.red * 255).toInt(), (it.green * 255).toInt(), (it.blue * 255).toInt())
                        }
                    } else {
                        val themeHex = settings.primaryColor ?: "#ec4899"
                        val effectiveHex = if (themeHex.equals("#800020", ignoreCase = true) || themeHex.equals("#9E1B3E", ignoreCase = true)) "#d41145" else themeHex
                        Color.parseColor(effectiveHex)
                    }
                } catch(e:Exception) { Color.parseColor("#ec4899") }
            
            val containerColor = try {
                val hsv = FloatArray(3)
                Color.colorToHSV(primaryColor, hsv)
                hsv[1] = 0.05f
                hsv[2] = 0.99f
                Color.HSVToColor(hsv)
            } catch(e:Exception) { Color.parseColor("#FAFAFA") }
            val tableRowColor = Color.WHITE
            
            canvas.drawColor(Color.WHITE)
            drawWatermark(canvas)
            
            if (iconBitmap != null) {
                // Draw pure original app icon at highest sharpness without artificial frames
                val destRect = android.graphics.RectF(pageWidth - 80f, 32f, pageWidth - 80f + 54f, 32f + 54f)
                val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
                canvas.drawBitmap(iconBitmap, null, destRect, drawPaint)
            }

            
            // Header
            drawText(canvas, "حاسبة الاعفاء والسعي لطلاب العراق", pageWidth / 2f, 65f, 22f, primaryColor, true, Paint.Align.CENTER, customTypeface)
            drawText(canvas, "م/ $docTitle", pageWidth / 2f, 95f, 14f, primaryColor, false, Paint.Align.CENTER, defaultTypeface)
            
            // Clean separator with pure theme color (zero black/white shading)
            val gradPaint = Paint().apply {
                color = primaryColor
                strokeWidth = 2f
            }
            canvas.drawLine(30f, 120f, pageWidth - 30f, 120f, gradPaint)

            var y = 160f

            fun drawBeautifulResultBox(c: Canvas, y: Float, label: String, grade: String, subLabel: String): Float {
                val tempPaint = Paint().apply { textSize = 12f; typeface = defaultTypeface }
                val labelPaint = Paint().apply { textSize = 16f; typeface = defaultTypeface; isFakeBoldText = true }
                val gradePaint = Paint().apply { textSize = 32f; typeface = defaultTypeface; isFakeBoldText = true }
                
                // Dynamic grade text sizing for long fractions like 99.5656656%
                var gradeTextSize = 32f
                if (grade.length > 9) {
                    gradeTextSize = 22f
                } else if (grade.length > 7) {
                    gradeTextSize = 26f
                }
                gradePaint.textSize = gradeTextSize

                val textWidth = maxOf(
                    tempPaint.measureText(subLabel),
                    maxOf(labelPaint.measureText(label), gradePaint.measureText(grade))
                ) + 60f
                val boxWidth = 340f
                val boxHeight = 115f
                val boxX = (pageWidth - boxWidth) / 2f

                // Pure primary theme color fill with zero artificial shading
                val gradientPaint = Paint().apply {
                    color = primaryColor
                    isAntiAlias = true
                }
                c.drawRoundRect(boxX, y, boxX + boxWidth, y + boxHeight, 28f, 28f, gradientPaint)
                
                // Subtle clean border for sharp definition
                val boxBorderPaint = Paint().apply {
                    color = Color.argb(55, 255, 255, 255)
                    style = Paint.Style.STROKE
                    strokeWidth = 1.2f
                    isAntiAlias = true
                }
                c.drawRoundRect(boxX, y, boxX + boxWidth, y + boxHeight, 28f, 28f, boxBorderPaint)
                
                drawText(c, label, pageWidth / 2f, y + 35f, 16f, Color.WHITE, true, Paint.Align.CENTER, defaultTypeface)
                drawText(c, grade, pageWidth / 2f, y + 75f, gradeTextSize, Color.WHITE, true, Paint.Align.CENTER, defaultTypeface)
                if (subLabel.isNotEmpty()) {
                    val subTextWidth = tempPaint.measureText(subLabel)
                    val pillWidth = subTextWidth + 28f
                    val pillHeight = 22f
                    val pillX = (pageWidth - pillWidth) / 2f
                    val pillY = y + 84f
                    val pillPaint = Paint().apply {
                        color = Color.argb(45, 0, 0, 0)
                        isAntiAlias = true
                    }
                    c.drawRoundRect(pillX, pillY, pillX + pillWidth, pillY + pillHeight, 11f, 11f, pillPaint)
                    drawText(c, subLabel, pageWidth / 2f, pillY + 15.5f, 11f, Color.WHITE, false, Paint.Align.CENTER, defaultTypeface)
                }
                
                return y + boxHeight + 30f
            }

            when (result) {
                is ResultDisplayState.ExemptionResult -> {
                    val d = result.data
                    y = drawBeautifulResultBox(
                        canvas,
                        y,
                        "المعدل العام للسعي السنوي",
                        "${com.Nightjar.caliq5.ui.components.formatDecimalGrade(d.finalAverage)}%",
                        "المعدل العام لا يخضع لقاعدة جبر الكسور (85% هو الحد الأدنى)"
                    )

                    // Draw status card matching HTML structure
                    if (y > pageHeight - 180f) { startNewPage(); y = 80f }
                    val statusTitle = "حالة الإعفاء" + (if (d.className.isNotEmpty()) " (${d.className})" else "")
                    val statusLines = mutableListOf<String>()
                    statusLines.add(d.exemptionStatus.primary)
                    if (d.exemptionStatus.secondary.isNotEmpty()) {
                        statusLines.add(d.exemptionStatus.secondary)
                    }
                    y = drawCard(canvas, pageWidth, y, statusTitle, statusLines, primaryColor, defaultTypeface)

                    // Draw individual exemptions card matching HTML detail structure
                    if (d.individualExemptions.isNotEmpty()) {
                        if (y > pageHeight - 180f) { startNewPage(); y = 80f }
                        val indLines = d.individualExemptions.map { g ->
                            val gradeStr = g.original?.let { original ->
                                if (g.isRounded) {
                                    "${g.grade?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""}% (تم جبره من ${com.Nightjar.caliq5.ui.components.formatDecimalGrade(original)}%)"
                                } else {
                                    "${com.Nightjar.caliq5.ui.components.formatDecimalGrade(original)}%"
                                }
                            } ?: "لا تشملني"
                            "• ${g.name}: $gradeStr"
                        }
                        y = drawCard(canvas, pageWidth, y, "مواد الإعفاء الفردي (90% فأكثر)", indLines, primaryColor, defaultTypeface)
                    }

                    // Draw detail list with rounded details if any
                    y = drawSectionHeader(canvas, pageWidth, y, "تفاصيل درجات السعي السنوي", defaultTypeface)
                    for (g in d.grades) {
                        if (y > pageHeight - 100f) { startNewPage(); y = 80f }
                        val gradeStr = g.original?.let { original ->
                            if (g.isRounded) {
                                "${g.grade?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""}% (تم جبره من ${com.Nightjar.caliq5.ui.components.formatDecimalGrade(original)}%)"
                            } else {
                                "${com.Nightjar.caliq5.ui.components.formatDecimalGrade(original)}%"
                            }
                        } ?: "لا تشملني"
                        y = drawRow(canvas, pageWidth, y, g.name, gradeStr, defaultTypeface)
                    }
                }
                is ResultDisplayState.AnnualResult -> {
                    val d = result.data
                    val hasFrac = kotlin.math.abs(d.realAverage - kotlin.math.floor(d.realAverage)) >= 0.01
                    val subText = if (hasFrac && d.finalAverage.toDouble() != d.realAverage) "السعي المقرب: ${d.finalAverage}%" else ""
                    y = drawBeautifulResultBox(canvas, y, "السعي السنوي", "${com.Nightjar.caliq5.ui.components.formatDecimalGrade(d.realAverage)}%", subText)

                    val floorVal = kotlin.math.floor(d.realAverage)
                    val roundingMsg = when {
                        !hasFrac -> "المعدل تام وبدون كسور عشرية وهو ${d.finalAverage}%."
                        d.finalAverage > floorVal -> "🎉 تهانينا! تم جبر السعي السنوي للأعلى إلى ${d.finalAverage}% لأن الكسر العشري كان 0.6 أو أكثر."
                        else -> "السعي السنوي النهائي هو ${d.finalAverage}%. لم يتم جبره للأعلى لأن الكسر العشري أقل من 0.6."
                    }
                    val t1Str = if (d.grades.isNotEmpty()) com.Nightjar.caliq5.ui.components.formatDecimalGrade(d.grades[0].grade, maxDecimals = 6) else ""
                    val midStr = if (d.grades.size > 1) com.Nightjar.caliq5.ui.components.formatDecimalGrade(d.grades[1].grade, maxDecimals = 6) else ""
                    val t2Str = if (d.grades.size > 2) com.Nightjar.caliq5.ui.components.formatDecimalGrade(d.grades[2].grade, maxDecimals = 6) else ""
                    val avgStr = com.Nightjar.caliq5.ui.components.formatDecimalGrade(d.realAverage, maxDecimals = 4)
                    val eqMsg = "المعادلة: ($t1Str + $midStr + $t2Str) ÷ 3 = $avgStr%"
                    val cardLines = if (result.isExpanded) listOf(roundingMsg, eqMsg) else listOf(roundingMsg)
                    y = drawCard(canvas, pageWidth, y, "النتيجة النهائية بعد الجبر", cardLines, primaryColor, defaultTypeface)

                    y = drawSectionHeader(canvas, pageWidth, y, "تفاصيل الفصول والامتحانات", defaultTypeface)
                    d.grades.forEach { g ->
                        if (y > pageHeight - 100f) { startNewPage(); y = 80f }
                        val gradeDisplay = g.grade?.let { "${com.Nightjar.caliq5.ui.components.formatDecimalGrade(it, maxDecimals = 6)}%" } ?: "-"
                        y = drawRow(canvas, pageWidth, y, g.name, gradeDisplay, defaultTypeface)
                    }
                }
                is ResultDisplayState.MonthlyResult -> {
                    val d = result.data
                    val hasFrac = kotlin.math.abs(d.realAverage - kotlin.math.floor(d.realAverage)) >= 0.01
                    val subText = if (hasFrac && d.finalAverage.toDouble() != d.realAverage) "المعدل المقرب: ${d.finalAverage}%" else ""
                    y = drawBeautifulResultBox(canvas, y, "المعدل الفصلي", "${com.Nightjar.caliq5.ui.components.formatDecimalGrade(d.realAverage, maxDecimals = 4)}%", subText)

                    val floorVal = kotlin.math.floor(d.realAverage)
                    val roundingMsg = when {
                        !hasFrac -> "المعدل تام وبدون كسور عشرية وهو ${d.finalAverage}%."
                        d.finalAverage > floorVal -> "🎉 تهانينا! تم جبر المعدل الفصلي للأعلى إلى ${d.finalAverage}% لأن الكسر العشري كان 0.6 أو أكثر."
                        else -> "المعدل النهائي هو ${d.finalAverage}%. لم يتم جبره للأعلى لأن الكسر العشري أقل من 0.6."
                    }
                    val gradeStrs = d.grades.map { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it.grade, maxDecimals = 6) }
                    val avgStr = com.Nightjar.caliq5.ui.components.formatDecimalGrade(d.realAverage, maxDecimals = 4)
                    val eqMsg = "المعادلة: (${gradeStrs.joinToString(" + ")}) ÷ ${d.grades.size} = $avgStr%"
                    val cardLines = if (result.isExpanded) listOf(roundingMsg, eqMsg) else listOf(roundingMsg)
                    y = drawCard(canvas, pageWidth, y, "النتيجة النهائية بعد الجبر", cardLines, primaryColor, defaultTypeface)

                    y = drawSectionHeader(canvas, pageWidth, y, "درجات الأشهر", defaultTypeface)
                    d.grades.forEach { g ->
                        if (y > pageHeight - 100f) { startNewPage(); y = 80f }
                        val gradeDisplay = g.grade?.let { "${com.Nightjar.caliq5.ui.components.formatDecimalGrade(it, maxDecimals = 6)}%" } ?: "-"
                        y = drawRow(canvas, pageWidth, y, g.name, gradeDisplay, defaultTypeface)
                    }
                }
                is ResultDisplayState.Term2Result -> {
                    val d = result.data
                    val term1 = d.grades.find { it.name == "الفصل الأول" }?.grade ?: 0.0
                    val mid = d.grades.find { it.name == "نصف السنة" }?.grade ?: 0.0

                    fun drawTerm2Card(c: Canvas, yPos: Float, target: Int, required: Double, totalRequired: Int, isExpanded: Boolean): Float {
                        var currY = yPos
                        val isPossible = required <= 100
                        val reqValue = if (required < 0) 0.0 else required
                        val targetName = if (target == 75) "الإعفاء العام" else "الإعفاء الفردي"
                        val msg = when {
                            required < 0 -> "محسوم! لقد حققت المعدل بالفعل وتحتاج إلى 0 لتحقيق سعي $target%."
                            isPossible -> "نعم، يمكنك تحقيق $targetName وهذا هو المعدل الحد الأدنى. شد حيلك✨🌠😍 في الفصل الثاني!"
                            else -> "لا يمكنك تحقيق هذه الدرجة والمعدل ($target%) لأن الدرجة المطلوبة أعلى من 100. بالتوفيق بامتحانات نهاية العام 🤝."
                        }
                        
                        val title = "لتحقيق معدل سعي سنوي $target% ($targetName) ${if (d.subjName.isNotEmpty()) "لمادة ${d.subjName}" else ""}"
                        val msgColor = if (isPossible) Color.parseColor("#28A745") else Color.parseColor("#DC2626")
                        
                        val boxColor = if (isPossible) primaryColor else Color.parseColor("#DC2626")

                        val boxWidth = 340f
                        val boxHeight = 115f
                        val boxX = (pageWidth - boxWidth) / 2f

                        val gradientPaint = Paint().apply {
                            color = boxColor
                            isAntiAlias = true
                        }
                        c.drawRoundRect(boxX, currY, boxX + boxWidth, currY + boxHeight, 28f, 28f, gradientPaint)
                        
                        drawText(c, title, pageWidth / 2f, currY - 10f, 12f, boxColor, true, Paint.Align.CENTER, defaultTypeface)
                        drawText(c, "تحتاج إلى معدل", pageWidth / 2f, currY + 30f, 13.5f, Color.WHITE, true, Paint.Align.CENTER, defaultTypeface)
                        drawText(c, "${com.Nightjar.caliq5.ui.components.formatDecimalGrade(reqValue, maxDecimals = 4)}%", pageWidth / 2f, currY + 70f, 32f, Color.WHITE, true, Paint.Align.CENTER, defaultTypeface)
                        
                        val term2Sub = "درجة في الفصل الثاني"
                        val tempPaint = Paint().apply { textSize = 11f; typeface = defaultTypeface }
                        val term2SubWidth = tempPaint.measureText(term2Sub)
                        val pillWidth = term2SubWidth + 28f
                        val pillHeight = 22f
                        val pillX = (pageWidth - pillWidth) / 2f
                        val pillY = currY + 84f
                        val pillPaint = Paint().apply {
                            color = Color.argb(56, 0, 0, 0)
                            isAntiAlias = true
                        }
                        c.drawRoundRect(pillX, pillY, pillX + pillWidth, pillY + pillHeight, 11f, 11f, pillPaint)
                        drawText(c, term2Sub, pageWidth / 2f, pillY + 15.5f, 11f, Color.WHITE, false, Paint.Align.CENTER, defaultTypeface)
                        
                        currY += 130f
                        
                        drawText(c, msg, pageWidth / 2f, currY, 11.5f, msgColor, true, Paint.Align.CENTER, defaultTypeface)
                        currY += 25f
                        
                        val t1Str = com.Nightjar.caliq5.ui.components.formatDecimalGrade(term1, maxDecimals = 6)
                        val midStr = com.Nightjar.caliq5.ui.components.formatDecimalGrade(mid, maxDecimals = 6)
                        val reqStr = com.Nightjar.caliq5.ui.components.formatDecimalGrade(required, maxDecimals = 6)
                        val lines = listOf(
                            "لتحقيق سعي $target%، يجب أن يكون مجموع درجاتك هو $totalRequired.",
                            "المعادلة: $totalRequired - ($t1Str + $midStr) = $reqStr"
                        )
                        if (isExpanded) {
                            currY = drawCard(canvas, pageWidth, currY, "شرح المعادلة", lines, primaryColor, defaultTypeface)
                        }
                        return currY
                    }

                    y = drawTerm2Card(canvas, y, 75, d.requiredFor75, 225, result.is75Expanded)
                    y = drawTerm2Card(canvas, y, 90, d.requiredFor90, 270, result.is90Expanded)

                    y = drawSectionHeader(canvas, pageWidth, y, "درجات الفصول السابقة", defaultTypeface)
                    d.grades.forEach { g ->
                        if (y > pageHeight - 100f) { startNewPage(); y = 80f }
                        y = drawRow(canvas, pageWidth, y, g.name, "${com.Nightjar.caliq5.ui.components.formatDecimalGrade(g.grade, maxDecimals = 6)}%", defaultTypeface)
                    }
                }
                is ResultDisplayState.StudentProfileResult -> {
                    val s = result.student
                    drawText(canvas, "اسم الطالب: ${s.name}", pageWidth - 30f, y, 16f, primaryColor, true, Paint.Align.RIGHT, defaultTypeface)
                    y += 30f
                    drawText(canvas, "الصف الدراسي: ${s.className}", pageWidth - 30f, y, 14f, Color.GRAY, false, Paint.Align.RIGHT, defaultTypeface)
                    y += 40f
                    
                    val cols = listOf("المادة", "معدل ف1", "نصف السنة", "معدل ف2", "السعي", "النهائي", "الدرجة النهائية")
                    val colWidths = listOf(105f, 72f, 68f, 72f, 60f, 60f, 88f)
                    
                    val predefinedList = if (s.className == "صف آخر (مخصص)") emptyList() else com.Nightjar.caliq5.data.ClassData.getSubjects(s.className)
                    val nonSkippedSubjects = s.subjects.filter { (name, r) ->
                        val isOpt = predefinedList.find { it.name == name }?.optional == true
                        !(isOpt && r.t1Val == "SKIPPED")
                    }

                    var totalSaei = 0.0
                    var countSaei = 0
                    var hasBelow75 = false
                    for ((subName, r) in nonSkippedSubjects) {
                        val t1Val = if (r.t1Mode == TermMode.TERM) r.t1Val.toDoubleOrNull() else {
                            val gr = r.t1Months.mapNotNull { it.toDoubleOrNull() }
                            if (gr.isNotEmpty()) gr.average() else null
                        }
                        val midVal = r.midVal.toDoubleOrNull()
                        val t2Val = if (r.t2Mode == TermMode.TERM) r.t2Val.toDoubleOrNull() else {
                            val gr = r.t2Months.mapNotNull { it.toDoubleOrNull() }
                            if (gr.isNotEmpty()) gr.average() else null
                        }
                        
                        if (t1Val != null && midVal != null && t2Val != null) {
                            val saeiRaw = (t1Val + midVal + t2Val) / 3.0
                            val rounded = floor(saeiRaw).let { f -> 
                                val dec = saeiRaw - f
                                when {
                                    dec >= 0.6 -> ceil(saeiRaw)
                                    saeiRaw >= 49.6 && saeiRaw < 50.0 -> 50.0
                                    saeiRaw >= 74.6 && saeiRaw < 75.0 -> 75.0
                                    saeiRaw >= 89.6 && saeiRaw < 90.0 -> 90.0
                                    else -> f
                                }
                            }
                            totalSaei += rounded
                            countSaei++
                            if (rounded < 75.0) hasBelow75 = true
                        }
                    }
                    val isGenExempt = countSaei > 0 && countSaei == nonSkippedSubjects.size && (totalSaei / countSaei) >= 85.0 && !hasBelow75

                    val overallAverageStr = if (countSaei > 0) "${com.Nightjar.caliq5.ui.components.formatDecimalGrade(totalSaei / countSaei)}%" else "---"
                    
                    y = drawBeautifulResultBox(
                        canvas,
                        y,
                        "المعدل العام",
                        overallAverageStr,
                        if (isGenExempt) "مبروك ✨ 🎉 الطالب معفى إعفاء عام في جميع الدروس لهذا العام" else "حالة الإعفاء العام: غير معفى عام"
                    )


                    drawTableHeader(canvas, pageWidth, y, cols, colWidths, containerColor, defaultTypeface)
                    y += 30f

                    for (entry in s.subjects) {
                        val subName = entry.key
                        val subRecord = entry.value
                        val isOpt = predefinedList.find { it.name == subName }?.optional == true
                        val isSkipped = isOpt && subRecord.t1Val == "SKIPPED"

                        if (y > pageHeight - 100f) {
                            startNewPage()
                            y = 80f
                            drawTableHeader(canvas, pageWidth, y, cols, colWidths, containerColor, defaultTypeface)
                            y += 30f
                        }

                        if (isSkipped) {
                            val vals = listOf("لا تشملني", "-", "-", "لا تشملني", "-", "-")
                            val subVals = listOf("", "", "", "", "", "")
                            y = drawTableRow(canvas, pageWidth, y, subName, vals, subVals, colWidths, Color.parseColor("#F5F5F5"), defaultTypeface)
                        } else {
                            val t1Val = if (subRecord.t1Mode == TermMode.TERM) subRecord.t1Val.toDoubleOrNull() else {
                                val gr = subRecord.t1Months.mapNotNull { it.toDoubleOrNull() }
                                if (gr.isNotEmpty()) gr.average() else null
                            }
                            val midVal = subRecord.midVal.toDoubleOrNull()
                            val t2Val = if (subRecord.t2Mode == TermMode.TERM) subRecord.t2Val.toDoubleOrNull() else {
                                val gr = subRecord.t2Months.mapNotNull { it.toDoubleOrNull() }
                                if (gr.isNotEmpty()) gr.average() else null
                            }
                            val saeiRaw = if (t1Val != null && midVal != null && t2Val != null) (t1Val + midVal + t2Val) / 3.0 else null
                            val saeiRounded = saeiRaw?.let { num ->
                                val fl = floor(num)
                                val dec = num - fl
                                when {
                                    dec >= 0.6 -> ceil(num)
                                    num >= 49.6 && num < 50.0 -> 50.0
                                    num >= 74.6 && num < 75.0 -> 75.0
                                    num >= 89.6 && num < 90.0 -> 90.0
                                    else -> fl
                                }
                            }
                            val isIndivExempt = saeiRounded != null && saeiRounded >= 90.0
                            val isExempt = saeiRounded != null && (isIndivExempt || isGenExempt)
                            val t1Str = if (subRecord.t1Mode == TermMode.TERM) subRecord.t1Val else t1Val?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""
                            val t2Str = if (subRecord.t2Mode == TermMode.TERM) subRecord.t2Val else t2Val?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: ""
                            val saeiStr = saeiRounded?.let { com.Nightjar.caliq5.ui.components.formatDecimalGrade(it) } ?: "---"
                            val finalStr = if (isExempt) "معفى" else if (subRecord.finalVal.isNotEmpty()) subRecord.finalVal else "-"
                            val resStr = if (isExempt) saeiStr else {
                                val fin = subRecord.finalVal.toDoubleOrNull()
                                if (fin != null && saeiRounded != null) com.Nightjar.caliq5.ui.components.formatDecimalGrade((saeiRounded + fin) / 2.0) else "---"
                            }
                            val vals = listOf(t1Str, subRecord.midVal, t2Str, saeiStr, finalStr, resStr)
                            
                            val t1Sub = if (subRecord.t1Mode == TermMode.MONTHLY && subRecord.t1Months.isNotEmpty()) "أشهر ف1: ${subRecord.t1Months.joinToString("، ")}" else ""
                            val t2Sub = if (subRecord.t2Mode == TermMode.MONTHLY && subRecord.t2Months.isNotEmpty()) "أشهر ف2: ${subRecord.t2Months.joinToString("، ")}" else ""
                            val roundingNote = when {
                                saeiRaw == null -> ""
                                saeiRaw >= 74.6 && saeiRaw < 75.0 -> "(${com.Nightjar.caliq5.ui.components.formatDecimalGrade(saeiRaw)}) تم جبر الكسر"
                                saeiRaw >= 89.6 && saeiRaw < 90.0 -> "(${com.Nightjar.caliq5.ui.components.formatDecimalGrade(saeiRaw)}) تم جبر الكسر"
                                saeiRaw >= 49.6 && saeiRaw < 50.0 -> "(${com.Nightjar.caliq5.ui.components.formatDecimalGrade(saeiRaw)}) تم جبر الكسر"
                                (saeiRaw - floor(saeiRaw)) >= 0.6 -> "(${com.Nightjar.caliq5.ui.components.formatDecimalGrade(saeiRaw)}) تم جبر الكسر"
                                else -> ""
                            }
                            val subVals = listOf(t1Sub, "", t2Sub, roundingNote, "", "")
                            
                            y = drawTableRow(canvas, pageWidth, y, subName, vals, subVals, colWidths, if (isExempt) Color.parseColor("#E8F5E9") else tableRowColor, defaultTypeface)
                        }
                    }

                }
            }

            drawPageFooter(canvas, pageNumber)
            document.finishPage(currentPage)

            val fileNameFinal = fileName.replace("/", "-").replace(":", "-")
            val cacheFile = File(context.cacheDir, fileNameFinal)
            try {
                if (cacheFile.exists()) {
                    cacheFile.delete()
                }
                FileOutputStream(cacheFile).use { out ->
                    document.writeTo(out)
                    out.flush()
                }
                saveToPublicDownloads(context, cacheFile, fileNameFinal)
                showDownloadNotification(context, fileNameFinal, cacheFile)
            } catch (e: Exception) { e.printStackTrace() } finally { document.close() }
        }
    }

    private fun openExternalViewer(context: Context, file: File, fileName: String) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val chooserIntent = Intent.createChooser(viewIntent, "فتح أو مشاركة ملف PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            try {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    private fun saveToPublicDownloads(context: Context, sourceFile: File, fileName: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { out ->
                        sourceFile.inputStream().use { input -> input.copyTo(out) }
                        out.flush()
                    }
                }
            } else {
                val targetFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                sourceFile.inputStream().use { input ->
                    FileOutputStream(targetFile).use { out -> 
                        input.copyTo(out)
                        out.flush()
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun drawTableHeader(canvas: Canvas, pageWidth: Int, y: Float, cols: List<String>, widths: List<Float>, color: Int, typeface: Typeface?) {
        val paint = Paint().apply { this.color = color; style = Paint.Style.FILL; isAntiAlias = true }
        canvas.drawRoundRect(30f, y - 20f, pageWidth - 30f, y + 15f, 8f, 8f, paint)
        val headerBorderPaint = Paint().apply {
            this.color = Color.parseColor("#CBD5E1")
            this.style = Paint.Style.STROKE
            this.strokeWidth = 1f
            this.isAntiAlias = true
        }
        canvas.drawRoundRect(30f, y - 20f, pageWidth - 30f, y + 15f, 8f, 8f, headerBorderPaint)
        var x = pageWidth - 35f
        for(i in cols.indices) {
            drawText(canvas, cols[i], x - widths[i]/2, y + 2f, 11f, Color.parseColor("#21005D"), true, Paint.Align.CENTER, typeface)
            x -= widths[i]
        }
    }

    private fun drawTableRow(canvas: Canvas, pageWidth: Int, y: Float, name: String, vals: List<String>, subVals: List<String>? = null, widths: List<Float>, color: Int, typeface: Typeface?): Float {
        val hasSubVals = subVals != null && subVals.any { it.isNotEmpty() }
        val boxHeight = if (hasSubVals) 34f else 24f
        
        val paint = Paint().apply { this.color = color; style = Paint.Style.FILL; isAntiAlias = true }
        canvas.drawRoundRect(30f, y - 16f, pageWidth - 30f, y + boxHeight, 8f, 8f, paint)
        val rowBorderPaint = Paint().apply {
            this.color = Color.parseColor("#E2E8F0")
            this.style = Paint.Style.STROKE
            this.strokeWidth = 0.8f
            this.isAntiAlias = true
        }
        canvas.drawRoundRect(30f, y - 16f, pageWidth - 30f, y + boxHeight, 8f, 8f, rowBorderPaint)
        var x = pageWidth - 35f
        
        val textY = if (hasSubVals) y - 1f else y + 5f
        
        drawText(canvas, name, x - 5f, y + (if(hasSubVals) 8f else 5f), 10.5f, Color.BLACK, true, Paint.Align.RIGHT, typeface)
        x -= widths[0]
        for(i in vals.indices) {
            drawText(canvas, vals[i], x - widths[i+1]/2, textY, 10.5f, Color.BLACK, false, Paint.Align.CENTER, typeface)
            
            if (hasSubVals && i < subVals!!.size && subVals[i].isNotEmpty()) {
                drawText(canvas, subVals[i], x - widths[i+1]/2, textY + 17f, 8f, Color.DKGRAY, false, Paint.Align.CENTER, typeface)
            }
            x -= widths[i+1]
        }
        return y + boxHeight + 34f
    }

    private fun drawCard(
        canvas: Canvas, 
        pageWidth: Int, 
        y: Float, 
        title: String, 
        lines: List<String>, 
        color: Int, 
        typeface: Typeface?,
        bgColor: Int = Color.parseColor("#FDFDFD"),
        borderColor: Int = Color.argb(40, 0, 0, 0)
    ): Float {
        val paint = Paint().apply { textSize = 11.5f; if (typeface != null) this.typeface = typeface else this.typeface = Typeface.DEFAULT }
        val maxWidth = pageWidth - 90f
        
        val wrappedLines = mutableListOf<String>()
        for (line in lines) {
            var currentLine = ""
            val words = line.split(" ")
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) > maxWidth) {
                    wrappedLines.add(currentLine)
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) wrappedLines.add(currentLine)
        }

        val height = 48f + (wrappedLines.size * 28f)
        
        // Soft rounded card with radius 28f exactly matching result screen cards
        canvas.drawRoundRect(30f, y, pageWidth - 30f, y + height, 28f, 28f, Paint().apply { this.color = bgColor; isAntiAlias = true })
        val cardBorderPaint = Paint().apply {
            this.color = Color.parseColor("#E2E8F0")
            this.style = Paint.Style.STROKE
            this.strokeWidth = 1.2f
            this.isAntiAlias = true
        }
        canvas.drawRoundRect(30f, y, pageWidth - 30f, y + height, 28f, 28f, cardBorderPaint)
        drawText(canvas, title, pageWidth - 45f, y + 26f, 14f, color, true, Paint.Align.RIGHT, typeface)
        var ly = y + 58f
        for(line in wrappedLines) {
            drawText(canvas, line, pageWidth - 45f, ly, 11f, Color.DKGRAY, false, Paint.Align.RIGHT, typeface)
            ly += 26f
        }
        return y + height + 32f
    }

    private fun drawSectionHeader(canvas: Canvas, pageWidth: Int, y: Float, text: String, typeface: Typeface?): Float {
        drawText(canvas, text, pageWidth - 30f, y, 14f, Color.parseColor("#6750A4"), true, Paint.Align.RIGHT, typeface)
        canvas.drawLine(30f, y + 10f, pageWidth - 30f, y + 10f, Paint().apply { color = Color.parseColor("#EADDFF"); strokeWidth = 2f })
        return y + 38f
    }

    private fun drawRow(canvas: Canvas, pageWidth: Int, y: Float, label: String, value: String, typeface: Typeface?): Float {
        drawText(canvas, label, pageWidth - 40f, y, 11.5f, Color.BLACK, false, Paint.Align.RIGHT, typeface)
        drawText(canvas, value, 40f, y, 11.5f, Color.parseColor("#6750A4"), true, Paint.Align.LEFT, typeface)
        canvas.drawLine(30f, y + 14f, pageWidth - 30f, y + 14f, Paint().apply { color = Color.parseColor("#EEEEEE"); strokeWidth = 1f; pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f) })
        return y + 34f
    }

    private fun showDownloadNotification(context: Context, fileName: String, file: File) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        }
        val channelId = "caliq_pdf"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(channelId, "PDF Downloads", NotificationManager.IMPORTANCE_HIGH))
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val reqCode = (System.currentTimeMillis() % 100000).toInt()
        val pi = PendingIntent.getActivity(
            context, 
            reqCode, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val smallIconRes = context.resources.getIdentifier("splash_icon", "drawable", context.packageName).let {
            if (it != 0) it else android.R.drawable.stat_sys_download_done
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIconRes)
            .setContentTitle("تم حفظ ملف PDF بنجاح ✓")
            .setContentText("انقر لفتح: $fileName")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        nm.notify(reqCode, notification)
    }

    private fun drawText(canvas: Canvas, text: String, x: Float, y: Float, size: Float, color: Int, bold: Boolean = false, align: Paint.Align = Paint.Align.RIGHT, customTypeface: Typeface? = null) {
        val p = Paint().apply {
            this.color = color; this.textSize = size; this.textAlign = align; this.isAntiAlias = true
            if (customTypeface != null) { typeface = customTypeface } 
            else if (bold) { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        }
        canvas.drawText(text, x, y, p)
    }
}
