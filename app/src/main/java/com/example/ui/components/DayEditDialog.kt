package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DayType
import com.example.data.model.FinancialSettings
import com.example.data.model.WorkDayEntry
import com.example.ui.theme.DayOffBlue
import com.example.ui.theme.OvertimePurple
import com.example.ui.theme.SickRose
import com.example.ui.theme.VacationAmber
import com.example.ui.theme.WorkDayGreen
import com.example.util.CalculationUtils
import com.example.util.DateUtils
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings

@Composable
fun DayEditDialog(
    initialEntry: WorkDayEntry,
    settings: FinancialSettings,
    onDismiss: () -> Unit,
    onSave: (date: String, isWork: Boolean, dayType: String, regHours: Double, otHours: Double, otHours2: Double, notes: String) -> Unit,
    onDelete: (WorkDayEntry) -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current

    var selectedDayType by remember { mutableStateOf(initialEntry.dayType) }
    var isWorkDay by remember { mutableStateOf(initialEntry.isWorkDay) }
    var regularHours by remember { mutableDoubleStateOf(initialEntry.regularHours) }
    var overtimeHours by remember { mutableDoubleStateOf(initialEntry.overtimeHours) }
    var overtimeHours2 by remember { mutableDoubleStateOf(initialEntry.overtimeHours2) }
    var notes by remember { mutableStateOf(initialEntry.notes) }

    val formattedDate = DateUtils.formatArabicDateWithDayName(initialEntry.date, language)

    val tempEntry = remember(isWorkDay, regularHours, overtimeHours, overtimeHours2, selectedDayType) {
        initialEntry.copy(
            isWorkDay = isWorkDay,
            dayType = selectedDayType,
            regularHours = regularHours,
            overtimeHours = overtimeHours,
            overtimeHours2 = overtimeHours2
        )
    }
    val estimatedEarnings = CalculationUtils.calculateDayEarnings(tempEntry, settings)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.logHoursDialogTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.cancel,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day Type Selector Chips
                Text(
                    text = strings.dayTypeTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DayTypeOptionChip(
                        title = strings.typeWork,
                        isSelected = isWorkDay && selectedDayType == DayType.WORK.name,
                        activeColor = WorkDayGreen,
                        modifier = Modifier.weight(1f)
                    ) {
                        isWorkDay = true
                        selectedDayType = DayType.WORK.name
                        if (regularHours == 0.0) regularHours = settings.standardHoursPerDay
                    }

                    DayTypeOptionChip(
                        title = strings.typeOff,
                        isSelected = !isWorkDay && selectedDayType == DayType.OFF.name,
                        activeColor = DayOffBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        isWorkDay = false
                        selectedDayType = DayType.OFF.name
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DayTypeOptionChip(
                        title = strings.typeVacation,
                        isSelected = !isWorkDay && selectedDayType == DayType.VACATION.name,
                        activeColor = VacationAmber,
                        modifier = Modifier.weight(1f)
                    ) {
                        isWorkDay = false
                        selectedDayType = DayType.VACATION.name
                    }

                    DayTypeOptionChip(
                        title = strings.typeSick,
                        isSelected = !isWorkDay && selectedDayType == DayType.SICK.name,
                        activeColor = SickRose,
                        modifier = Modifier.weight(1f)
                    ) {
                        isWorkDay = false
                        selectedDayType = DayType.SICK.name
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isWorkDay) {
                    // Regular Hours Stepper with +1 / -1 whole hour increment
                    HoursStepperRow(
                        title = strings.regularHoursStepper,
                        hours = regularHours,
                        hoursUnit = strings.hoursAbbrev,
                        accentColor = MaterialTheme.colorScheme.primary,
                        onHoursChanged = { regularHours = it.coerceIn(0.0, 24.0) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Overtime Hours 1 Stepper (+1 / -1 whole hour increment)
                    HoursStepperRow(
                        title = "${strings.overtime1HoursStepper} (x${settings.overtimeMultiplier})",
                        hours = overtimeHours,
                        hoursUnit = strings.hoursAbbrev,
                        accentColor = OvertimePurple,
                        onHoursChanged = { overtimeHours = it.coerceIn(0.0, 24.0) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Overtime Hours 2 Stepper (+1 / -1 whole hour increment)
                    HoursStepperRow(
                        title = "${strings.overtime2HoursStepper} (x${settings.overtimeMultiplier2})",
                        hours = overtimeHours2,
                        hoursUnit = strings.hoursAbbrev,
                        accentColor = Color(0xFFD97706),
                        onHoursChanged = { overtimeHours2 = it.coerceIn(0.0, 24.0) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Earnings Preview Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = strings.expectedEarningsDay,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${strings.totalHoursLabel}: ${CalculationUtils.formatHours(regularHours + overtimeHours + overtimeHours2)} ${strings.hoursAbbrev}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${CalculationUtils.formatAmount(estimatedEarnings)} ${settings.currencySymbol}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Quick Notes Presets Chips
                Text(
                    text = strings.quickNotesTitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(strings.quickNotesPresets) { preset ->
                        val isContained = notes.contains(preset)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isContained) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = if (isContained) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    notes = if (notes.isBlank()) {
                                        preset
                                    } else if (isContained) {
                                        notes.replace(preset, "")
                                            .replace(" -  - ", " - ")
                                            .trim(' ', '-')
                                    } else {
                                        "$notes - $preset"
                                    }
                                }
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isContained) FontWeight.Bold else FontWeight.Normal,
                                color = if (isContained) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes Free-form Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.notesOptional) },
                    placeholder = { Text(strings.notesPlaceholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("day_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Save & Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (initialEntry.id > 0) {
                        IconButton(
                            onClick = { onDelete(initialEntry) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SickRose.copy(alpha = 0.12f))
                                .testTag("delete_day_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = strings.deleteEntry,
                                tint = SickRose
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.cancel)
                    }

                    Button(
                        onClick = {
                            onSave(
                                initialEntry.date,
                                isWorkDay,
                                selectedDayType,
                                if (isWorkDay) regularHours else 0.0,
                                if (isWorkDay) overtimeHours else 0.0,
                                if (isWorkDay) overtimeHours2 else 0.0,
                                notes
                            )
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("save_day_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.saveEntry, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayTypeOptionChip(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) activeColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, activeColor) else null,
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HoursStepperRow(
    title: String,
    hours: Double,
    hoursUnit: String,
    accentColor: Color,
    onHoursChanged: (Double) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${CalculationUtils.formatHours(hours)} $hoursUnit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Whole 1 hour decrement
                IconButton(
                    onClick = { onHoursChanged((hours - 1.0).coerceAtLeast(0.0)) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "-1",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Whole 1 hour increment
                IconButton(
                    onClick = { onHoursChanged((hours + 1.0).coerceIn(0.0, 24.0)) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "+1",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
