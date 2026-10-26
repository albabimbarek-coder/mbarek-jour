package com.example.util

import com.example.data.model.FinancialSettings
import com.example.data.model.WorkDayEntry
import java.util.Locale

data class MonthFinancialSummary(
    val totalEntries: Int,
    val workDaysCount: Int,
    val offDaysCount: Int,
    val vacationDaysCount: Int,
    val sickDaysCount: Int,
    val totalRegularHours: Double,
    val totalOvertimeHours1: Double,
    val totalOvertimeHours2: Double,
    val totalOvertimeHours: Double,
    val totalHours: Double,
    val regularEarnings: Double,
    val overtime1Earnings: Double,
    val overtime2Earnings: Double,
    val overtimeEarnings: Double,
    val totalEarnings: Double,
    val averageHoursPerWorkDay: Double,
    val effectiveHourlyRate: Double,
    val overtimeRate: Double,
    val overtimeRate2: Double,
    val currencySymbol: String
)

object CalculationUtils {

    fun calculateDayEarnings(entry: WorkDayEntry, settings: FinancialSettings): Double {
        if (!entry.isWorkDay) return 0.0

        val baseRate = entry.hourlyRateSnapshot ?: settings.effectiveHourlyRate
        val ot1Rate = baseRate * settings.overtimeMultiplier
        val ot2Rate = baseRate * settings.overtimeMultiplier2

        val regIncome = entry.regularHours * baseRate
        val ot1Income = entry.overtimeHours * ot1Rate
        val ot2Income = entry.overtimeHours2 * ot2Rate
        return regIncome + ot1Income + ot2Income
    }

    fun calculateDayRegularEarnings(entry: WorkDayEntry, settings: FinancialSettings): Double {
        if (!entry.isWorkDay) return 0.0
        val baseRate = entry.hourlyRateSnapshot ?: settings.effectiveHourlyRate
        return entry.regularHours * baseRate
    }

    fun calculateDayOvertime1Earnings(entry: WorkDayEntry, settings: FinancialSettings): Double {
        if (!entry.isWorkDay) return 0.0
        val baseRate = entry.hourlyRateSnapshot ?: settings.effectiveHourlyRate
        val overtimeRate = baseRate * settings.overtimeMultiplier
        return entry.overtimeHours * overtimeRate
    }

    fun calculateDayOvertime2Earnings(entry: WorkDayEntry, settings: FinancialSettings): Double {
        if (!entry.isWorkDay) return 0.0
        val baseRate = entry.hourlyRateSnapshot ?: settings.effectiveHourlyRate
        val overtime2Rate = baseRate * settings.overtimeMultiplier2
        return entry.overtimeHours2 * overtime2Rate
    }

    fun calculateDayOvertimeEarnings(entry: WorkDayEntry, settings: FinancialSettings): Double {
        return calculateDayOvertime1Earnings(entry, settings) + calculateDayOvertime2Earnings(entry, settings)
    }

    fun computeMonthSummary(
        entries: List<WorkDayEntry>,
        settings: FinancialSettings
    ): MonthFinancialSummary {
        var workDays = 0
        var offDays = 0
        var vacationDays = 0
        var sickDays = 0
        var regularHours = 0.0
        var overtimeHours1 = 0.0
        var overtimeHours2 = 0.0
        var regEarnings = 0.0
        var ot1Earnings = 0.0
        var ot2Earnings = 0.0

        for (entry in entries) {
            when {
                entry.isWorkDay -> {
                    workDays++
                    regularHours += entry.regularHours
                    overtimeHours1 += entry.overtimeHours
                    overtimeHours2 += entry.overtimeHours2
                    regEarnings += calculateDayRegularEarnings(entry, settings)
                    ot1Earnings += calculateDayOvertime1Earnings(entry, settings)
                    ot2Earnings += calculateDayOvertime2Earnings(entry, settings)
                }
                entry.dayType == "OFF" -> offDays++
                entry.dayType == "VACATION" -> vacationDays++
                entry.dayType == "SICK" -> sickDays++
                else -> offDays++
            }
        }

        val totalOtEarnings = ot1Earnings + ot2Earnings
        val totalOtHours = overtimeHours1 + overtimeHours2

        val totalEarnings = if (settings.calculationMode == "MONTHLY_SALARY") {
            // In monthly salary mode: base salary + overtime earnings
            settings.monthlyBaseSalary + totalOtEarnings
        } else {
            regEarnings + totalOtEarnings
        }

        val totalAllHours = regularHours + totalOtHours
        val avgHours = if (workDays > 0) totalAllHours / workDays else 0.0

        return MonthFinancialSummary(
            totalEntries = entries.size,
            workDaysCount = workDays,
            offDaysCount = offDays,
            vacationDaysCount = vacationDays,
            sickDaysCount = sickDays,
            totalRegularHours = regularHours,
            totalOvertimeHours1 = overtimeHours1,
            totalOvertimeHours2 = overtimeHours2,
            totalOvertimeHours = totalOtHours,
            totalHours = totalAllHours,
            regularEarnings = if (settings.calculationMode == "MONTHLY_SALARY") settings.monthlyBaseSalary else regEarnings,
            overtime1Earnings = ot1Earnings,
            overtime2Earnings = ot2Earnings,
            overtimeEarnings = totalOtEarnings,
            totalEarnings = totalEarnings,
            averageHoursPerWorkDay = avgHours,
            effectiveHourlyRate = settings.effectiveHourlyRate,
            overtimeRate = settings.overtimeHourlyRate,
            overtimeRate2 = settings.overtime2HourlyRate,
            currencySymbol = settings.currencySymbol
        )
    }

    fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format(Locale.US, "%,d", amount.toLong())
        } else {
            String.format(Locale.US, "%,.2f", amount)
        }
    }

    fun formatHours(hours: Double): String {
        return "${Math.round(hours)}"
    }
}
