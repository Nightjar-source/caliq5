package com.Nightjar.caliq5.export

import com.Nightjar.caliq5.viewmodel.ResultDisplayState

private fun printHtmlReport(html: String): Unit = js("""
    (function(reportHtml) {
        var existing = document.getElementById('caliq5-print-report');
        if (existing) existing.remove();
        var style = document.getElementById('caliq5-print-style');
        if (!style) {
            style = document.createElement('style');
            style.id = 'caliq5-print-style';
            style.textContent = '@media screen { #caliq5-print-report { display: none; } } @media print { body > *:not(#caliq5-print-report) { display: none !important; } #caliq5-print-report { display: block !important; position: static !important; background: #fff !important; color: #111 !important; padding: 24px; font-family: system-ui, -apple-system, sans-serif; direction: rtl; } }';
            document.head.appendChild(style);
        }
        var div = document.createElement('div');
        div.id = 'caliq5-print-report';
        div.innerHTML = reportHtml;
        document.body.appendChild(div);
        window.print();
    })(html)
""")

private fun escapeHtml(text: String): String =
    text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

actual object PdfExporter {
    actual suspend fun export(result: ResultDisplayState) {
        try {
            val sb = StringBuilder()
            sb.append("<div style='max-width:760px;margin:0 auto;direction:rtl;text-align:right;'>")
            sb.append("<h1 style='color:#0F766E;margin-bottom:4px;'>حاسبة الإعفاء والسعي لطلاب العراق (Caliq 5)</h1>")
            when (result) {
                is ResultDisplayState.Exemption -> {
                    sb.append("<h2>نتيجة الإعفاء — ${escapeHtml(result.stageName)}</h2>")
                    sb.append("<p><strong>الحالة:</strong> ${escapeHtml(result.overallStatus)}</p>")
                    sb.append("<p><strong>المعدل العام:</strong> ${result.overallAverage}%</p>")
                    sb.append("<table border='1' cellspacing='0' cellpadding='8' style='width:100%;border-collapse:collapse;margin-top:12px;'>")
                    sb.append("<tr><th>المادة</th><th>الدرجة</th><th>الإعفاء</th></tr>")
                    result.subjects.forEach { s ->
                        sb.append("<tr><td>${escapeHtml(s.subjectName)}</td><td>${s.score}</td><td>${if (s.isExempt) "معفى" else "غير معفى"}</td></tr>")
                    }
                    sb.append("</table>")
                }
                is ResultDisplayState.Annual -> {
                    sb.append("<h2>نتيجة السعي السنوي — ${escapeHtml(result.subjectName)}</h2>")
                    sb.append("<p><strong>السعي السنوي:</strong> ${result.annualAverage}%</p>")
                    sb.append("<p>${escapeHtml(result.statusText)}</p>")
                }
                is ResultDisplayState.Monthly -> {
                    sb.append("<h2>نتيجة المعدل الفصلي — ${escapeHtml(result.subjectName)}</h2>")
                    sb.append("<p><strong>المعدل الفصلي:</strong> ${result.average}%</p>")
                }
                is ResultDisplayState.Term2 -> {
                    sb.append("<h2>الدرجة المطلوبة في الفصل الثاني — ${escapeHtml(result.subjectName)}</h2>")
                    sb.append("<p><strong>للإعفاء العام (75%):</strong> ${result.neededFor75}</p>")
                    sb.append("<p><strong>للإعفاء الفردي (90%):</strong> ${result.neededFor90}</p>")
                }
                ResultDisplayState.None -> {
                    sb.append("<p>لا توجد نتيجة للطباعة.</p>")
                }
            }
            sb.append("</div>")
            printHtmlReport(sb.toString())
        } catch (_: Throwable) {
        }
    }
}
