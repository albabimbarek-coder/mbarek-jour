package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WorkTrackerViewModel
import com.example.ui.components.DayTypeBreakdownDonut
import com.example.ui.components.HoursDistributionChart
import com.example.ui.theme.DayOffBlue
import com.example.ui.theme.GoldTertiaryLight
import com.example.ui.theme.OvertimePurple
import com.example.ui.theme.TealPrimaryLight
import com.example.ui.theme.WorkDayGreen
import com.example.util.CalculationUtils
import com.example.util.DateUtils
import com.example.util.LocalAppStrings

@Composable
fun StatsScreen(
    viewModel: WorkTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val monthPrefix by viewModel.currentMonthPrefix.collectAsState()
    val entries by viewModel.currentMonthEntries.collectAsState()
    val summary by viewModel.monthSummary.collectAsState()
    val settings by viewModel.settings.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "السابق",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    val language = com.example.util.LocalAppLanguage.current
                    Text(
                        text = "${strings.statsTitle}: ${DateUtils.formatToDisplayMonth(monthPrefix, language)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "التالي",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Hero Financial Metric Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.netEarningsTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${CalculationUtils.formatAmount(summary.totalEarnings)} ${settings.currencySymbol}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val totalCalc = (summary.regularEarnings + summary.overtimeEarnings).coerceAtLeast(1.0)
                    val regRatio = (summary.regularEarnings / totalCalc).toFloat()

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${strings.baseEarningsLabel}: ${CalculationUtils.formatAmount(summary.regularEarnings)} ${settings.currencySymbol}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "${strings.overtimeHoursLabel}: ${CalculationUtils.formatAmount(summary.overtimeEarnings)} ${settings.currencySymbol}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GoldTertiaryLight
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { regRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.onPrimary,
                            trackColor = GoldTertiaryLight
                        )
                    }
                }
            }
        }

        // 4 Grid KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiMetricCard(
                    title = strings.totalHoursLabel,
                    value = "${CalculationUtils.formatHours(summary.totalHours)} ${strings.hoursAbbrev}",
                    subtitle = "${strings.regularHoursLabel}: ${CalculationUtils.formatHours(summary.totalRegularHours)}",
                    icon = Icons.Default.AccessTime,
                    accentColor = TealPrimaryLight,
                    modifier = Modifier.weight(1f)
                )

                KpiMetricCard(
                    title = strings.overtimeHoursLabel,
                    value = "${CalculationUtils.formatHours(summary.totalOvertimeHours)} ${strings.hoursAbbrev}",
                    subtitle = "x${settings.overtimeMultiplier}",
                    icon = Icons.Default.ElectricBolt,
                    accentColor = OvertimePurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiMetricCard(
                    title = strings.actualWorkDaysLabel,
                    value = "${summary.workDaysCount} ${strings.daysAbbrev}",
                    subtitle = "${summary.offDaysCount + summary.vacationDaysCount + summary.sickDaysCount} ${strings.filterOff}",
                    icon = Icons.Default.CalendarToday,
                    accentColor = WorkDayGreen,
                    modifier = Modifier.weight(1f)
                )

                val avgDailyEarn = if (summary.workDaysCount > 0) summary.totalEarnings / summary.workDaysCount else 0.0
                KpiMetricCard(
                    title = strings.avgHoursPerDayLabel,
                    value = "${CalculationUtils.formatHours(summary.averageHoursPerWorkDay)} ${strings.hoursAbbrev}",
                    subtitle = "${CalculationUtils.formatAmount(avgDailyEarn)} ${settings.currencySymbol}",
                    icon = Icons.Default.Speed,
                    accentColor = DayOffBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Daily/Weekly Hours Distribution Chart
        item {
            HoursDistributionChart(entries = entries)
        }

        // Donut Chart for Days Breakdown
        item {
            DayTypeBreakdownDonut(
                workDays = summary.workDaysCount,
                offDays = summary.offDaysCount,
                vacationDays = summary.vacationDaysCount,
                sickDays = summary.sickDaysCount
            )
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
