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
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FinancialSettings
import com.example.data.model.WorkDayEntry
import com.example.ui.theme.OvertimePurple
import com.example.ui.theme.TealPrimaryLight
import com.example.ui.theme.WorkDayGreen
import com.example.util.CalculationUtils
import com.example.util.DateUtils
import com.example.util.LocalAppStrings

@Composable
fun QuickReturnDialog(
    settings: FinancialSettings,
    onDismiss: () -> Unit,
    onConfirm: (regularHours: Double, overtimeHours: Double, overtimeHours2: Double, notes: String) -> Unit
) {
    val strings = LocalAppStrings.current
    val language = com.example.util.LocalAppLanguage.current
    var regularHours by remember { mutableDoubleStateOf(settings.standardHoursPerDay) }
    var overtimeHours by remember { mutableDoubleStateOf(0.0) }
    var overtimeHours2 by remember { mutableDoubleStateOf(0.0) }
    var notes by remember { mutableStateOf("") }

    val todayStr = DateUtils.getTodayDate()
    val formattedDate = DateUtils.formatArabicDateWithDayName(todayStr, language)

    val tempEntry = remember(regularHours, overtimeHours, overtimeHours2) {
        WorkDayEntry(
            date = todayStr,
            isWorkDay = true,
            regularHours = regularHours,
            overtimeHours = overtimeHours,
            overtimeHours2 = overtimeHours2
        )
    }
    val todayEarnings = CalculationUtils.calculateDayEarnings(tempEntry, settings)

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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with Home/Return Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = strings.backFromWorkPrompt,
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
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_quick_return_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = strings.cancel)
                    }
                }

                // Quick Preset Chips for Common Shifts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickPresetChip(
                        title = "${settings.standardHoursPerDay.toInt()} ${strings.hoursAbbrev}",
                        subtitle = strings.regularHoursLabel,
                        isSelected = regularHours == settings.standardHoursPerDay && overtimeHours == 0.0 && overtimeHours2 == 0.0,
                        modifier = Modifier.weight(1f)
                    ) {
                        regularHours = settings.standardHoursPerDay
                        overtimeHours = 0.0
                        overtimeHours2 = 0.0
                    }

                    QuickPresetChip(
                        title = "+1 س (OT1)",
                        subtitle = "${settings.overtimeMultiplier}x",
                        isSelected = regularHours == settings.standardHoursPerDay && overtimeHours == 1.0 && overtimeHours2 == 0.0,
                        modifier = Modifier.weight(1f)
                    ) {
                        regularHours = settings.standardHoursPerDay
                        overtimeHours = 1.0
                        overtimeHours2 = 0.0
                    }

                    QuickPresetChip(
                        title = "+1 س (OT2)",
                        subtitle = "${settings.overtimeMultiplier2}x",
                        isSelected = regularHours == settings.standardHoursPerDay && overtimeHours == 0.0 && overtimeHours2 == 1.0,
                        modifier = Modifier.weight(1f)
                    ) {
                        regularHours = settings.standardHoursPerDay
                        overtimeHours = 0.0
                        overtimeHours2 = 1.0
                    }
                }

                // Stepper Controls (+1 / -1 whole hours)
                QuickStepperCard(
                    title = strings.regularHoursStepper,
                    hours = regularHours,
                    hoursUnit = strings.hoursAbbrev,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onHoursChanged = { regularHours = it.coerceIn(0.0, 24.0) }
                )

                QuickStepperCard(
                    title = "${strings.overtime1HoursStepper} (x${settings.overtimeMultiplier})",
                    hours = overtimeHours,
                    hoursUnit = strings.hoursAbbrev,
                    accentColor = OvertimePurple,
                    onHoursChanged = { overtimeHours = it.coerceIn(0.0, 24.0) }
                )

                QuickStepperCard(
                    title = "${strings.overtime2HoursStepper} (x${settings.overtimeMultiplier2})",
                    hours = overtimeHours2,
                    hoursUnit = strings.hoursAbbrev,
                    accentColor = androidx.compose.ui.graphics.Color(0xFFD97706),
                    onHoursChanged = { overtimeHours2 = it.coerceIn(0.0, 24.0) }
                )

                // Today Estimated Earnings Card
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
                            .padding(14.dp),
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
                            text = "${CalculationUtils.formatAmount(todayEarnings)} ${settings.currencySymbol}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Quick Notes Presets Chips
                Text(
                    text = strings.quickNotesTitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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

                // Optional Free-form Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.notesOptional) },
                    placeholder = { Text(strings.notesPlaceholder) },
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_notes_input")
                )

                // Confirm and Cancel buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.cancel)
                    }

                    Button(
                        onClick = { onConfirm(regularHours, overtimeHours, overtimeHours2, notes) },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("confirm_quick_return_button"),
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
private fun QuickPresetChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickStepperCard(
    title: String,
    hours: Double,
    hoursUnit: String = "س",
    accentColor: androidx.compose.ui.graphics.Color,
    onHoursChanged: (Double) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${CalculationUtils.formatHours(hours)} $hoursUnit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onHoursChanged((hours - 1.0).coerceAtLeast(0.0)) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "-1",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { onHoursChanged((hours + 1.0).coerceIn(0.0, 24.0)) },
                    modifier = Modifier
                        .size(36.dp)
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
