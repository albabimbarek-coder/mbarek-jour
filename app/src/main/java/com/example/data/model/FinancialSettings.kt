package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_settings")
data class FinancialSettings(
    @PrimaryKey
    val id: Int = 1,
    val calculationMode: String = "HOURLY", // "HOURLY" or "MONTHLY_SALARY"
    val hourlyRate: Double = 30.0,
    val monthlyBaseSalary: Double = 5000.0,
    val standardHoursPerDay: Double = 8.0,
    val workingDaysPerMonth: Int = 22,
    val overtimeMultiplier: Double = 1.0,
    val overtimeMultiplier2: Double = 1.0,
    val currencySymbol: String = "د.م", // Default Dirham
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 18, // 6:00 PM
    val reminderMinute: Int = 0,
    val appLanguage: String = "ar",
    val appTheme: String = "SYSTEM" // "LIGHT", "DARK", "SYSTEM"
) {
    // Derived effective hourly rate for monthly salary mode
    val effectiveHourlyRate: Double
        get() {
            return if (calculationMode == "HOURLY") {
                hourlyRate
            } else {
                val totalStandardMonthlyHours = (standardHoursPerDay * workingDaysPerMonth).coerceAtLeast(1.0)
                monthlyBaseSalary / totalStandardMonthlyHours
            }
        }

    val overtimeHourlyRate: Double
        get() = effectiveHourlyRate * overtimeMultiplier

    val overtime2HourlyRate: Double
        get() = effectiveHourlyRate * overtimeMultiplier2
}
