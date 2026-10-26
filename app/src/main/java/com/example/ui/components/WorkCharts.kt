package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkDayEntry
import com.example.ui.theme.DayOffBlue
import com.example.ui.theme.GoldTertiaryLight
import com.example.ui.theme.OvertimePurple
import com.example.ui.theme.TealPrimaryLight
import com.example.ui.theme.VacationAmber
import com.example.ui.theme.WorkDayGreen
import com.example.util.CalculationUtils

@Composable
fun HoursDistributionChart(
    entries: List<WorkDayEntry>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ساعات العمل اليومية (عادية مقابل إضافية)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chart Legends
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(TealPrimaryLight)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "عادية",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(OvertimePurple)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "إضافي 1",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD97706))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "إضافي 2",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Draw stacked bars
            // We group entries or show up to 14 latest or active days
            val displayEntries = if (entries.isEmpty()) {
                emptyList()
            } else {
                entries.takeLast(14)
            }

            if (displayEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد سجلات كافية لعرض الرسم البياني",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxHours = (displayEntries.maxOfOrNull { it.totalHours } ?: 12.0).coerceAtLeast(10.0)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 24.dp.toPx()
                    val chartHeight = height - bottomPadding
                    val count = displayEntries.size
                    val slotWidth = width / count
                    val barWidth = (slotWidth * 0.55f).coerceIn(8.dp.toPx(), 22.dp.toPx())

                    // Draw baseline guideline
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(0f, chartHeight),
                        end = Offset(width, chartHeight),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw 8h standard guideline
                    val guide8Y = chartHeight - ((8.0 / maxHours) * chartHeight).toFloat()
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.25f),
                        start = Offset(0f, guide8Y),
                        end = Offset(width, guide8Y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    displayEntries.forEachIndexed { index, entry ->
                        val centerX = index * slotWidth + slotWidth / 2f

                        if (entry.isWorkDay && entry.totalHours > 0) {
                            val regH = (entry.regularHours / maxHours).toFloat() * chartHeight
                            val ot1H = (entry.overtimeHours / maxHours).toFloat() * chartHeight
                            val ot2H = (entry.overtimeHours2 / maxHours).toFloat() * chartHeight

                            val regTop = chartHeight - regH
                            val ot1Top = regTop - ot1H
                            val ot2Top = ot1Top - ot2H

                            // Regular bar (Teal)
                            drawRoundRect(
                                color = TealPrimaryLight,
                                topLeft = Offset(centerX - barWidth / 2f, regTop),
                                size = Size(barWidth, regH),
                                cornerRadius = CornerRadius(barWidth / 4f, barWidth / 4f)
                            )

                            // Overtime 1 bar on top of regular (Purple)
                            if (ot1H > 0) {
                                drawRoundRect(
                                    color = OvertimePurple,
                                    topLeft = Offset(centerX - barWidth / 2f, ot1Top),
                                    size = Size(barWidth, ot1H),
                                    cornerRadius = CornerRadius(barWidth / 4f, barWidth / 4f)
                                )
                            }

                            // Overtime 2 bar on top of OT1 (Amber)
                            if (ot2H > 0) {
                                drawRoundRect(
                                    color = Color(0xFFD97706),
                                    topLeft = Offset(centerX - barWidth / 2f, ot2Top),
                                    size = Size(barWidth, ot2H),
                                    cornerRadius = CornerRadius(barWidth / 4f, barWidth / 4f)
                                )
                            }
                        } else {
                            // Non-work day marker
                            drawCircle(
                                color = DayOffBlue.copy(alpha = 0.5f),
                                radius = barWidth / 3f,
                                center = Offset(centerX, chartHeight - 8.dp.toPx())
                            )
                        }
                    }
                }

                // X-Axis day labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    displayEntries.forEach { entry ->
                        val dayPart = entry.date.takeLast(2)
                        Text(
                            text = dayPart,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayTypeBreakdownDonut(
    workDays: Int,
    offDays: Int,
    vacationDays: Int,
    sickDays: Int,
    modifier: Modifier = Modifier
) {
    val totalDays = (workDays + offDays + vacationDays + sickDays).coerceAtLeast(1)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "توزيع أيام الشهر",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Donut Chart Canvas
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        val stroke = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                        var startAngle = -90f

                        val slices = listOf(
                            Pair(workDays.toFloat() / totalDays, WorkDayGreen),
                            Pair(offDays.toFloat() / totalDays, DayOffBlue),
                            Pair(vacationDays.toFloat() / totalDays, VacationAmber),
                            Pair(sickDays.toFloat() / totalDays, Color(0xFFF43F5E))
                        )

                        slices.forEach { (ratio, color) ->
                            if (ratio > 0f) {
                                val sweep = ratio * 360f
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep.coerceAtLeast(3f),
                                    useCenter = false,
                                    style = stroke
                                )
                                startAngle += sweep
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalDays",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "إجمالي الأيام",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Legend Column
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DonutLegendRow(color = WorkDayGreen, label = "أيام عمل", count = workDays)
                    DonutLegendRow(color = DayOffBlue, label = "أيام راحة", count = offDays)
                    DonutLegendRow(color = VacationAmber, label = "إجازة سنوية", count = vacationDays)
                    DonutLegendRow(color = Color(0xFFF43F5E), label = "مرضي", count = sickDays)
                }
            }
        }
    }
}

@Composable
private fun DonutLegendRow(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
