package com.example.util

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.FinancialSettings
import com.example.data.model.WorkDayEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    fun exportToCsv(
        context: Context,
        monthPrefix: String,
        entries: List<WorkDayEntry>,
        settings: FinancialSettings,
        language: AppLanguage = AppLanguage.AR
    ): File? {
        return try {
            val strings = getAppStrings(language)
            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val fileName = "work_report_${monthPrefix}.csv"
            val file = File(reportsDir, fileName)

            val summary = CalculationUtils.computeMonthSummary(entries, settings)
            val monthTitle = DateUtils.formatToDisplayMonth(monthPrefix, language)

            FileOutputStream(file).use { fos ->
                // Write UTF-8 BOM so Microsoft Excel correctly renders all languages (Arabic, French, English)
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

                val sb = StringBuilder()
                sb.append("${strings.reportSummaryTitle} - $monthTitle\n")
                sb.append("${if (language == AppLanguage.AR) "تاريخ التصدير" else "Export Date"},${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}\n")
                sb.append("${strings.calcModeTitle},${if (settings.calculationMode == "HOURLY") strings.modeHourlyTitle else strings.modeSalaryTitle}\n")
                sb.append("${strings.hourlyRateLabel},${CalculationUtils.formatAmount(summary.effectiveHourlyRate)} ${settings.currencySymbol}\n")
                sb.append("${strings.overtime1MultiplierTitle},${settings.overtimeMultiplier}x\n")
                sb.append("${strings.overtime2MultiplierTitle},${settings.overtimeMultiplier2}x\n\n")

                // CSV Headers
                sb.append("${strings.dateCol},${strings.dayCol},${strings.typeCol},${strings.regCol},${strings.ot1Col},${strings.ot2Col},${strings.totCol},${strings.earnCol} (${settings.currencySymbol}),${strings.notesCol}\n")

                for (entry in entries) {
                    val dayName = DateUtils.getDayOfWeekShort(entry.date, language)
                    val dayTypeTitle = when (entry.dayType) {
                        "WORK" -> strings.typeWork
                        "OFF" -> strings.typeOff
                        "VACATION" -> strings.typeVacation
                        "SICK" -> strings.typeSick
                        else -> strings.typeWork
                    }
                    val regHours = CalculationUtils.formatHours(entry.regularHours)
                    val ot1Hours = CalculationUtils.formatHours(entry.overtimeHours)
                    val ot2Hours = CalculationUtils.formatHours(entry.overtimeHours2)
                    val totHours = CalculationUtils.formatHours(entry.totalHours)
                    val earnings = CalculationUtils.formatAmount(CalculationUtils.calculateDayEarnings(entry, settings))
                    val safeNotes = entry.notes.replace(",", " - ").replace("\n", " ")

                    sb.append("${entry.date},$dayName,$dayTypeTitle,$regHours,$ot1Hours,$ot2Hours,$totHours,$earnings,\"$safeNotes\"\n")
                }

                sb.append("\n${strings.reportSummaryTitle} - $monthTitle\n")
                sb.append("${strings.workDaysCountLabel},${summary.workDaysCount} ${strings.daysAbbrev}\n")
                sb.append("${strings.filterOff},${summary.offDaysCount + summary.vacationDaysCount + summary.sickDaysCount} ${strings.daysAbbrev}\n")
                sb.append("${strings.regularHoursLabel},${CalculationUtils.formatHours(summary.totalRegularHours)} ${strings.hoursAbbrev}\n")
                sb.append("${strings.overtimeHours1Label},${CalculationUtils.formatHours(summary.totalOvertimeHours1)} ${strings.hoursAbbrev}\n")
                sb.append("${strings.overtimeHours2Label},${CalculationUtils.formatHours(summary.totalOvertimeHours2)} ${strings.hoursAbbrev}\n")
                sb.append("${strings.totalHoursLabel},${CalculationUtils.formatHours(summary.totalHours)} ${strings.hoursAbbrev}\n")
                sb.append("${strings.baseEarningsLabel},${CalculationUtils.formatAmount(summary.regularEarnings)} ${settings.currencySymbol}\n")
                sb.append("${strings.overtimeHours1Label} (${settings.overtimeMultiplier}x),${CalculationUtils.formatAmount(summary.overtime1Earnings)} ${settings.currencySymbol}\n")
                sb.append("${strings.overtimeHours2Label} (${settings.overtimeMultiplier2}x),${CalculationUtils.formatAmount(summary.overtime2Earnings)} ${settings.currencySymbol}\n")
                sb.append("${strings.totalEarningsTitle},${CalculationUtils.formatAmount(summary.totalEarnings)} ${settings.currencySymbol}\n")

                fos.write(sb.toString().toByteArray(Charsets.UTF_8))
            }

            file
        } catch (e: Exception) {
            Log.e("ExportHelper", "Failed to export CSV", e)
            null
        }
    }

    fun exportToPdf(
        context: Context,
        monthPrefix: String,
        entries: List<WorkDayEntry>,
        settings: FinancialSettings,
        language: AppLanguage = AppLanguage.AR
    ): File? {
        return try {
            val strings = getAppStrings(language)
            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val fileName = "work_report_${monthPrefix}.pdf"
            val file = File(reportsDir, fileName)

            val summary = CalculationUtils.computeMonthSummary(entries, settings)
            val monthTitle = DateUtils.formatToDisplayMonth(monthPrefix, language)

            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }

            // Header Banner
            paint.color = Color.rgb(15, 118, 110) // Teal Primary
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, paint)

            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 18f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${strings.appName} - $monthTitle", pageWidth / 2f, 45f, paint)

            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val genDateStr = "${if (language == AppLanguage.AR) "تم الإنشاء" else if (language == AppLanguage.FR) "Généré le" else "Generated"}: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}"
            canvas.drawText(genDateStr, pageWidth / 2f, 68f, paint)

            // KPI Summary Cards
            val cardY = 105f
            val cardHeight = 55f
            val cardWidth = 125f
            val cardSpacing = 12f
            val startX = 25f

            val kpis = listOf(
                Pair(strings.totalEarningsTitle, "${CalculationUtils.formatAmount(summary.totalEarnings)} ${settings.currencySymbol}"),
                Pair(strings.totalHoursLabel, "${CalculationUtils.formatHours(summary.totalHours)} ${strings.hoursAbbrev}"),
                Pair(strings.overtimeHoursLabel, "${CalculationUtils.formatHours(summary.totalOvertimeHours)} ${strings.hoursAbbrev}"),
                Pair(strings.workDaysCountLabel, "${summary.workDaysCount} ${strings.daysAbbrev}")
            )

            paint.textAlign = Paint.Align.CENTER
            kpis.forEachIndexed { index, kpi ->
                val rx = startX + index * (cardWidth + cardSpacing)
                val rect = RectF(rx, cardY, rx + cardWidth, cardY + cardHeight)

                paint.color = Color.rgb(241, 245, 249) // light slate
                canvas.drawRoundRect(rect, 6f, 6f, paint)

                paint.color = Color.rgb(71, 85, 105)
                paint.textSize = 9f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText(kpi.first, rx + cardWidth / 2f, cardY + 20f, paint)

                paint.color = if (index == 0) Color.rgb(15, 118, 110) else Color.rgb(30, 41, 59)
                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(kpi.second, rx + cardWidth / 2f, cardY + 42f, paint)
            }

            // Table Header
            val tableTop = 180f
            paint.color = Color.rgb(30, 41, 59) // Slate
            canvas.drawRect(25f, tableTop, pageWidth - 25f, tableTop + 24f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER

            // Table Columns Positions
            val colDate = 60f
            val colDay = 118f
            val colType = 175f
            val colReg = 235f
            val colOt1 = 285f
            val colOt2 = 335f
            val colTot = 385f
            val colEarn = 450f
            val colNotes = 525f

            canvas.drawText(strings.dateCol, colDate, tableTop + 16f, paint)
            canvas.drawText(strings.dayCol, colDay, tableTop + 16f, paint)
            canvas.drawText(strings.typeCol, colType, tableTop + 16f, paint)
            canvas.drawText(strings.regCol, colReg, tableTop + 16f, paint)
            canvas.drawText(strings.ot1Col, colOt1, tableTop + 16f, paint)
            canvas.drawText(strings.ot2Col, colOt2, tableTop + 16f, paint)
            canvas.drawText(strings.totCol, colTot, tableTop + 16f, paint)
            canvas.drawText(strings.earnCol, colEarn, tableTop + 16f, paint)
            canvas.drawText(strings.notesCol, colNotes, tableTop + 16f, paint)

            var currentY = tableTop + 24f
            val rowHeight = 20f

            entries.forEachIndexed { index, entry ->
                // Check if page overflow
                if (currentY + rowHeight > pageHeight - 50f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 40f
                }

                // Row background (alternating zebra stripes)
                paint.color = if (index % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
                canvas.drawRect(25f, currentY, pageWidth - 25f, currentY + rowHeight, paint)

                paint.color = Color.rgb(30, 41, 59)
                paint.textSize = 8.5f
                paint.typeface = Typeface.DEFAULT
                paint.textAlign = Paint.Align.CENTER

                val dayName = DateUtils.getDayOfWeekShort(entry.date, language)
                val typeName = when (entry.dayType) {
                    "WORK" -> strings.typeWork
                    "OFF" -> strings.typeOff
                    "VACATION" -> strings.typeVacation
                    "SICK" -> strings.typeSick
                    else -> strings.typeWork
                }
                val earn = if (entry.isWorkDay) CalculationUtils.formatAmount(CalculationUtils.calculateDayEarnings(entry, settings)) else "-"
                val safeNotes = if (entry.notes.length > 14) entry.notes.take(13) + "…" else entry.notes

                canvas.drawText(entry.date, colDate, currentY + 14f, paint)
                canvas.drawText(dayName, colDay, currentY + 14f, paint)
                canvas.drawText(typeName, colType, currentY + 14f, paint)
                canvas.drawText(if (entry.isWorkDay) CalculationUtils.formatHours(entry.regularHours) else "-", colReg, currentY + 14f, paint)
                canvas.drawText(if (entry.isWorkDay) CalculationUtils.formatHours(entry.overtimeHours) else "-", colOt1, currentY + 14f, paint)
                canvas.drawText(if (entry.isWorkDay) CalculationUtils.formatHours(entry.overtimeHours2) else "-", colOt2, currentY + 14f, paint)
                canvas.drawText(if (entry.isWorkDay) CalculationUtils.formatHours(entry.totalHours) else "-", colTot, currentY + 14f, paint)
                canvas.drawText(earn, colEarn, currentY + 14f, paint)
                canvas.drawText(safeNotes, colNotes, currentY + 14f, paint)

                currentY += rowHeight
            }

            // Footer notes and signature space
            currentY += 25f
            if (currentY + 50f <= pageHeight - 30f) {
                paint.color = Color.rgb(100, 116, 139)
                paint.textSize = 8.5f
                paint.textAlign = Paint.Align.RIGHT
                val sigLabel = if (language == AppLanguage.AR) "توقيع الموظف / المسؤول: ____________________"
                else if (language == AppLanguage.FR) "Signature de l'employé / Responsable : ____________________"
                else "Employee / Supervisor Signature: ____________________"
                canvas.drawText(sigLabel, pageWidth - 35f, currentY + 20f, paint)

                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(strings.appName, 35f, currentY + 20f, paint)
            }

            pdfDocument.finishPage(page)

            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }
            pdfDocument.close()

            file
        } catch (e: Exception) {
            Log.e("ExportHelper", "Failed to export PDF", e)
            null
        }
    }

    fun getShareUri(context: Context, file: File): Uri {
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    fun createShareIntent(context: Context, file: File, mimeType: String, title: String): Intent {
        val uri = getShareUri(context, file)
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            clipData = ClipData.newRawUri(title, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createViewIntent(context: Context, file: File, mimeType: String, title: String): Intent {
        val uri = getShareUri(context, file)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            clipData = ClipData.newRawUri(title, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val shareIntent = createShareIntent(context, file, mimeType, title)
        val chooser = Intent.createChooser(shareIntent, title).apply {
            clipData = shareIntent.clipData
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun openFile(context: Context, file: File, mimeType: String, title: String): Boolean {
        return try {
            val viewIntent = createViewIntent(context, file, mimeType, title)
            context.startActivity(viewIntent)
            true
        } catch (e: Exception) {
            Log.e("ExportHelper", "Failed to open file directly, falling back to share", e)
            shareFile(context, file, mimeType, title)
            false
        }
    }

    fun saveToDownloads(context: Context, file: File, mimeType: String, displayName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = resolver.insert(collection, values) ?: return false
                resolver.openOutputStream(itemUri)?.use { out ->
                    file.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
                true
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadsDir.mkdirs()
                val destFile = File(downloadsDir, displayName)
                file.copyTo(destFile, overwrite = true)
                true
            }
        } catch (e: Exception) {
            Log.e("ExportHelper", "Failed to save file to downloads", e)
            false
        }
    }

    fun generateTextSummary(
        monthPrefix: String,
        entries: List<WorkDayEntry>,
        settings: FinancialSettings,
        language: AppLanguage = AppLanguage.AR
    ): String {
        val strings = getAppStrings(language)
        val summary = CalculationUtils.computeMonthSummary(entries, settings)
        val monthTitle = DateUtils.formatToDisplayMonth(monthPrefix, language)

        return """
            📊 *${strings.reportSummaryTitle} - $monthTitle*
            -----------------------------------
            🗓 *${strings.workDaysCountLabel}:* ${summary.workDaysCount} ${strings.daysAbbrev}
            🌴 *${strings.filterOff}:* ${summary.offDaysCount + summary.vacationDaysCount + summary.sickDaysCount} ${strings.daysAbbrev}
            ⏱ *${strings.regularHoursLabel}:* ${CalculationUtils.formatHours(summary.totalRegularHours)} ${strings.hoursAbbrev}
            ⚡ *${strings.overtimeHours1Label}:* ${CalculationUtils.formatHours(summary.totalOvertimeHours1)} ${strings.hoursAbbrev}
            🔥 *${strings.overtimeHours2Label}:* ${CalculationUtils.formatHours(summary.totalOvertimeHours2)} ${strings.hoursAbbrev}
            ⏰ *${strings.totalHoursLabel}:* ${CalculationUtils.formatHours(summary.totalHours)} ${strings.hoursAbbrev}
            -----------------------------------
            💰 *${strings.baseEarningsLabel}:* ${CalculationUtils.formatAmount(summary.regularEarnings)} ${settings.currencySymbol}
            ✨ *${strings.overtimeHours1Label}:* ${CalculationUtils.formatAmount(summary.overtime1Earnings)} ${settings.currencySymbol}
            🌟 *${strings.overtimeHours2Label}:* ${CalculationUtils.formatAmount(summary.overtime2Earnings)} ${settings.currencySymbol}
            💵 *${strings.totalEarningsTitle}:* ${CalculationUtils.formatAmount(summary.totalEarnings)} ${settings.currencySymbol}
        """.trimIndent()
    }
}
