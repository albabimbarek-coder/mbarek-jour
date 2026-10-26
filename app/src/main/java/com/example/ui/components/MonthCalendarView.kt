package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkDayEntry
import com.example.ui.theme.DayOffBlue
import com.example.ui.theme.OvertimePurple
import com.example.ui.theme.SickRose
import com.example.ui.theme.VacationAmber
import com.example.ui.theme.WorkDayGreen
import com.example.util.CalculationUtils
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MonthCalendarView(
    monthPrefix: String,
    entries: List<WorkDayEntry>,
    onDateClick: (String) -> Unit
) {
    val language = com.example.util.LocalAppLanguage.current
    val strings = com.example.util.LocalAppStrings.current
    val entriesMap = entries.associateBy { it.date }
    val today = DateUtils.getTodayDate()

    // Week day headers localized to current language
    val dayHeaders = DateUtils.getWeekDayHeaders(language)

    // Calculate calendar grid (42 cells: 6 weeks x 7 days)
    val cal = Calendar.getInstance().apply {
        val parts = monthPrefix.split("-")
        set(Calendar.YEAR, parts[0].toInt())
        set(Calendar.MONTH, parts[1].toInt() - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Calendar.SUNDAY = 1, SATURDAY = 7

    // Shift to Arabic week starting Saturday:
    // Saturday -> 0, Sunday -> 1, Monday -> 2, Tuesday -> 3, Wednesday -> 4, Thursday -> 5, Friday -> 6
    val leadingEmptyDays = when (firstDayOfWeek) {
        Calendar.SATURDAY -> 0
        Calendar.SUNDAY -> 1
        Calendar.MONDAY -> 2
        Calendar.TUESDAY -> 3
        Calendar.WEDNESDAY -> 4
        Calendar.THURSDAY -> 5
        Calendar.FRIDAY -> 6
        else -> 0
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Week headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayHeaders.forEach { header ->
                    Text(
                        text = header,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar cells in rows of 7
            val totalCells = leadingEmptyDays + maxDaysInMonth
            val totalRows = (totalCells + 6) / 7

            for (row in 0 until totalRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - leadingEmptyDays + 1

                        if (dayNumber in 1..maxDaysInMonth) {
                            val parts = monthPrefix.split("-")
                            val dateStr = String.format(Locale.US, "%s-%s-%02d", parts[0], parts[1], dayNumber)
                            val entry = entriesMap[dateStr]
                            val isToday = dateStr == today

                            CalendarDayCell(
                                dayNumber = dayNumber,
                                entry = entry,
                                isToday = isToday,
                                modifier = Modifier.weight(1f),
                                onClick = { onDateClick(dateStr) }
                            )
                        } else {
                            // Empty spacer cell
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                if (row < totalRows - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calendar Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = WorkDayGreen, label = strings.typeWork)
                LegendItem(color = OvertimePurple, label = strings.overtimeHoursLabel)
                LegendItem(color = DayOffBlue, label = strings.typeOff)
                LegendItem(color = VacationAmber, label = strings.typeVacation)
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    entry: WorkDayEntry?,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val statusColor = when {
        entry == null -> Color.Transparent
        entry.isWorkDay && entry.overtimeHours > 0 -> OvertimePurple
        entry.isWorkDay -> WorkDayGreen
        entry.dayType == "OFF" -> DayOffBlue
        entry.dayType == "VACATION" -> VacationAmber
        entry.dayType == "SICK" -> SickRose
        else -> DayOffBlue
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else if (entry != null) statusColor.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            )
            .then(
                if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                else Modifier
            )
            .clickable { onClick() }
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$dayNumber",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isToday || entry != null) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp
            )

            if (entry != null && entry.isWorkDay) {
                Text(
                    text = "${CalculationUtils.formatHours(entry.totalHours)}س",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            } else if (entry != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}
