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
            style.textContent = '@media screen { #caliq5-print-report { display: none !important; } } @media print { html, body { width: auto !important; height: auto !important; overflow: visible !important; background: #ffffff !important; color: #111827 !important; } #ComposeTarget, #pwa-loader { display: none !important; } body > *:not(#caliq5-print-report) { display: none !important; } #caliq5-print-report { display: block !important; position: static !important; width: 100% !important; background: #ffffff !important; color: #111827 !important; padding: 24px; font-family: system-ui, -apple-system, sans-serif; direction: rtl; } }';
            document.head.appendChild(style);
        }
        var div = document.createElement('div');
        div.id = 'caliq5-print-report';
        div.innerHTML = reportHtml;
        document.body.appendChild(div);
        window.onafterprint = function() {
            var el = document.getElementById('caliq5-print-report');
            if (el) el.remove();
        };
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
            sb.append("<div style='max-width:760px;margin:0 auto;direction:rtl;text-align:right;color:#111827;'>")
            sb.append("<h1 style='color:#0F766E;margin-bottom:6px;'>حاسبة الإعفاء والسعي لطلاب العراق (Caliq 5)</h1>")
            sb.append("<h2 style='margin-top:0;'>${escapeHtml(result.title)}</h2>")
            when (result) {
                is ResultDisplayState.ExemptionResult -> {
                    val d = result.data
                    sb.append("<p><strong>الصف:</strong> ${escapeHtml(d.className)}</p>")
                    sb.append("<p><strong>المعدل العام:</strong> ${d.finalAverage}%</p>")
                    sb.append("<p><strong>النتيجة:</strong> ${if (d.isGeneralExempt) "إعفاء عام" else if (d.isExempt) "إعفاء فردي" else "غير معفى"}</p>")
                    sb.append("<table border='1' cellspacing='0' cellpadding='8' style='width:100%;border-collapse:collapse;margin-top:12px;'>")
                    sb.append("<tr><th>المادة</th><th>الدرجة</th></tr>")
                    d.grades.forEach { g ->
                        sb.append("<tr><td>${escapeHtml(g.name)}</td><td>${g.grade ?: "-"}</td></tr>")
                    }
                    sb.append("</table>")
                }
                is ResultDisplayState.AnnualResult -> {
                    val d = result.data
                    sb.append("<p><strong>المادة:</strong> ${escapeHtml(d.subjectName)}</p>")
                    sb.append("<p><strong>السعي السنوي النهائي:</strong> ${d.finalAverage}% (الحقيقي: ${d.realAverage}%)</p>")
                    sb.append("<table border='1' cellspacing='0' cellpadding='8' style='width:100%;border-collapse:collapse;margin-top:12px;'>")
                    sb.append("<tr><th>الفصل</th><th>الدرجة</th></tr>")
                    d.grades.forEach { g ->
                        sb.append("<tr><td>${escapeHtml(g.name)}</td><td>${g.grade}</td></tr>")
                    }
                    sb.append("</table>")
                }
                is ResultDisplayState.MonthlyResult -> {
                    val d = result.data
                    sb.append("<p><strong>المادة:</strong> ${escapeHtml(d.subjectName)}</p>")
                    sb.append("<p><strong>المعدل الفصلي النهائي:</strong> ${d.finalAverage}% (الحقيقي: ${d.realAverage}%)</p>")
                    sb.append("<table border='1' cellspacing='0' cellpadding='8' style='width:100%;border-collapse:collapse;margin-top:12px;'>")
                    sb.append("<tr><th>الشهر</th><th>الدرجة</th></tr>")
                    d.grades.forEach { g ->
                        sb.append("<tr><td>${escapeHtml(g.name)}</td><td>${g.grade}</td></tr>")
                    }
                    sb.append("</table>")
                }
                is ResultDisplayState.Term2Result -> {
                    val d = result.data
                    sb.append("<p><strong>المادة:</strong> ${escapeHtml(d.subjName)}</p>")
                    sb.append("<p><strong>الدرجة المطلوبة في الفصل الثاني للإعفاء العام (75%):</strong> ${d.requiredFor75}</p>")
                    sb.append("<p><strong>الدرجة المطلوبة في الفصل الثاني للإعفاء الفردي (90%):</strong> ${d.requiredFor90}</p>")
                }
                is ResultDisplayState.StudentProfileResult -> {
                    val s = result.student
                    sb.append("<p><strong>الطالب:</strong> ${escapeHtml(s.name)} — <strong>الصف:</strong> ${escapeHtml(s.className)}</p>")
                    sb.append("<table border='1' cellspacing='0' cellpadding='8' style='width:100%;border-collapse:collapse;margin-top:12px;'>")
                    sb.append("<tr><th>المادة</th><th>ف1</th><th>نصف السنة</th><th>ف2</th></tr>")
                    s.subjects.forEach { (subName, rec) ->
                        sb.append("<tr><td>${escapeHtml(subName)}</td><td>${rec.t1Val}</td><td>${rec.midVal}</td><td>${rec.t2Val}</td></tr>")
                    }
                    sb.append("</table>")
                }
            }
            sb.append("</div>")
            printHtmlReport(sb.toString())
        } catch (_: Throwable) {
        }
    }
}
