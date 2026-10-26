package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "work_day_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class WorkDayEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val isWorkDay: Boolean = true,
    val dayType: String = DayType.WORK.name,
    val regularHours: Double = 8.0,
    val overtimeHours: Double = 0.0,
    val overtimeHours2: Double = 0.0,
    val notes: String = "",
    val hourlyRateSnapshot: Double? = null
) {
    val totalHours: Double
        get() = if (isWorkDay) regularHours + overtimeHours + overtimeHours2 else 0.0

    val parsedDayType: DayType
        get() = DayType.fromString(dayType)
}
