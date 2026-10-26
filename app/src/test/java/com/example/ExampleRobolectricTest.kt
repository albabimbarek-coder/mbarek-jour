package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.FinancialSettings
import com.example.data.model.WorkDayEntry
import com.example.util.AppLanguage
import com.example.util.CalculationUtils
import com.example.util.DateUtils
import com.example.util.getAppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ساعات العمل", appName)
    }

    @Test
    fun `calculate earnings with overtime correctly`() {
        val settings = FinancialSettings(
            hourlyRate = 20.0,
            overtimeMultiplier = 1.5,
            calculationMode = "HOURLY",
            currencySymbol = "د.م"
        )
        val entry = WorkDayEntry(
            date = "2026-10-24",
            isWorkDay = true,
            regularHours = 8.0,
            overtimeHours = 2.0
        )
        // 8 * 20 = 160; 2 * (20 * 1.5) = 60; Total = 220
        val earnings = CalculationUtils.calculateDayEarnings(entry, settings)
        assertEquals(220.0, earnings, 0.001)
        assertEquals("د.م", settings.currencySymbol)
    }

    @Test
    fun `verify 1 to 1 overtime multiplier without extra surcharge`() {
        val settings = FinancialSettings(
            hourlyRate = 25.0,
            overtimeMultiplier = 1.0,
            calculationMode = "HOURLY"
        )
        val entry = WorkDayEntry(
            date = "2026-10-24",
            isWorkDay = true,
            regularHours = 8.0,
            overtimeHours = 3.0
        )
        // 8 * 25 + 3 * 25 = 275
        val earnings = CalculationUtils.calculateDayEarnings(entry, settings)
        assertEquals(275.0, earnings, 0.001)
    }

    @Test
    fun `verify overtime 1 and overtime 2 combined daily calculation`() {
        val settings = FinancialSettings(
            hourlyRate = 20.0,
            overtimeMultiplier = 1.0,
            overtimeMultiplier2 = 1.5,
            calculationMode = "HOURLY"
        )
        val entry = WorkDayEntry(
            date = "2026-10-24",
            isWorkDay = true,
            regularHours = 8.0,
            overtimeHours = 2.0,
            overtimeHours2 = 1.0
        )
        // Total Hours = 8 + 2 + 1 = 11.0
        assertEquals(11.0, entry.totalHours, 0.001)

        // 8 * 20 = 160; 2 * 20 = 40; 1 * (20 * 1.5) = 30; Total = 230.0
        val earnings = CalculationUtils.calculateDayEarnings(entry, settings)
        assertEquals(230.0, earnings, 0.001)
    }

    @Test
    fun `verify quick notes presets are present in all languages`() {
        val ar = getAppStrings(AppLanguage.AR)
        val en = getAppStrings(AppLanguage.EN)
        val fr = getAppStrings(AppLanguage.FR)

        assertTrue(ar.quickNotesPresets.isNotEmpty())
        assertTrue(en.quickNotesPresets.isNotEmpty())
        assertTrue(fr.quickNotesPresets.isNotEmpty())
        assertEquals(6, ar.quickNotesPresets.size)
    }

    @Test
    fun `verify all three languages have definitions`() {
        val ar = getAppStrings(AppLanguage.AR)
        val en = getAppStrings(AppLanguage.EN)
        val fr = getAppStrings(AppLanguage.FR)

        assertNotNull(ar.appName)
        assertNotNull(en.appName)
        assertNotNull(fr.appName)
        assertEquals("ساعات العمل", ar.appName)
        assertEquals("Work Hours", en.appName)
        assertEquals("Suivi des Heures", fr.appName)
    }

    @Test
    fun `verify default overtime2 multiplier is 1x`() {
        val defaultSettings = FinancialSettings()
        assertEquals(1.0, defaultSettings.overtimeMultiplier, 0.001)
        assertEquals(1.0, defaultSettings.overtimeMultiplier2, 0.001)
    }

    @Test
    fun `verify export action strings in all languages`() {
        listOf(AppLanguage.AR, AppLanguage.EN, AppLanguage.FR).forEach { lang ->
            val strings = getAppStrings(lang)
            assertTrue(strings.shareActionTitle.isNotBlank())
            assertTrue(strings.saveActionTitle.isNotBlank())
            assertTrue(strings.openActionTitle.isNotBlank())
            assertTrue(strings.savedToDownloadsSuccess.isNotBlank())
            assertTrue(strings.exportFailedMsg.isNotBlank())
        }
    }

    @Test
    fun `verify csv export generation produces valid non empty file`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settings = FinancialSettings(overtimeMultiplier = 1.0, overtimeMultiplier2 = 1.0)
        val entries = listOf(
            WorkDayEntry(
                date = "2026-10-24",
                isWorkDay = true,
                regularHours = 8.0,
                overtimeHours = 2.0,
                overtimeHours2 = 1.0,
                notes = "تقرير اختبار"
            )
        )
        val file = com.example.util.ExportHelper.exportToCsv(context, "2026-10", entries, settings, AppLanguage.AR)
        assertNotNull(file)
        assertTrue(file!!.exists() && file.length() > 0)
    }

    @Test
    fun `verify pdf export does not crash on robolectric jvm`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settings = FinancialSettings(overtimeMultiplier = 1.0, overtimeMultiplier2 = 1.0)
        val entries = listOf(
            WorkDayEntry(
                date = "2026-10-24",
                isWorkDay = true,
                regularHours = 8.0,
                overtimeHours = 1.0,
                overtimeHours2 = 2.0,
                notes = "عمل إضافي"
            )
        )
        // Native Android PdfDocument is stubbed under headless JVM Robolectric; ensure graceful catch without unhandled crash
        val file = com.example.util.ExportHelper.exportToPdf(context, "2026-10", entries, settings, AppLanguage.AR)
        assertTrue(file == null || file.exists())
    }
}
