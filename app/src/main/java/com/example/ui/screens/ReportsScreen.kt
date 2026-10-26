package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkDayEntry
import com.example.ui.WorkTrackerViewModel
import com.example.ui.theme.DayOffBlue
import com.example.ui.theme.SickRose
import com.example.ui.theme.WorkDayGreen
import com.example.util.AppLanguage
import com.example.util.CalculationUtils
import com.example.util.DateUtils
import com.example.util.ExportHelper
import com.example.util.LocalAppLanguage
import com.example.util.LocalAppStrings

@Composable
fun ReportsScreen(
    viewModel: WorkTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val monthPrefix by viewModel.currentMonthPrefix.collectAsState()
    val entries by viewModel.currentMonthEntries.collectAsState()
    val summary by viewModel.monthSummary.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var activeExportDialog by remember { mutableStateOf<ExportDialogState?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector Header
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
                            contentDescription = "Previous",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "${strings.reportTitle}: ${DateUtils.formatToDisplayMonth(monthPrefix, language)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Report Summary Banner
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.reportSummaryTitle} ${DateUtils.formatToDisplayMonth(monthPrefix, language)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${entries.size} ${strings.daysAbbrev}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    ReportSummaryRow(
                        label = strings.totalHoursLabel,
                        value = "${CalculationUtils.formatHours(summary.totalHours)} ${strings.hoursAbbrev} (${CalculationUtils.formatHours(summary.totalRegularHours)} ${strings.regularHoursLabel} + ${CalculationUtils.formatHours(summary.totalOvertimeHours1)} ${strings.overtimeHours1Label} + ${CalculationUtils.formatHours(summary.totalOvertimeHours2)} ${strings.overtimeHours2Label})"
                    )

                    ReportSummaryRow(
                        label = strings.actualWorkDaysLabel,
                        value = "${summary.workDaysCount} ${strings.daysAbbrev} (${summary.offDaysCount + summary.vacationDaysCount + summary.sickDaysCount} ${strings.filterOff})"
                    )

                    ReportSummaryRow(
                        label = strings.totalEarningsTitle,
                        value = "${CalculationUtils.formatAmount(summary.totalEarnings)} ${settings.currencySymbol}",
                        valueColor = MaterialTheme.colorScheme.primary,
                        isBold = true
                    )
                }
            }
        }

        // Export Options Cards
        item {
            Text(
                text = strings.exportOptionsTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Export PDF Card
        item {
            ExportActionCard(
                title = strings.exportPdfTitle,
                description = strings.exportPdfDesc,
                icon = Icons.Default.PictureAsPdf,
                accentColor = SickRose,
                buttonText = strings.exportPdfButton,
                buttonTag = "export_pdf_button",
                onPrimaryClick = {
                    val file = ExportHelper.exportToPdf(
                        context = context,
                        monthPrefix = monthPrefix,
                        entries = entries,
                        settings = settings,
                        language = language
                    )
                    if (file != null) {
                        activeExportDialog = ExportDialogState(
                            title = "${strings.appName} - ${DateUtils.formatToDisplayMonth(monthPrefix, language)}",
                            file = file,
                            mimeType = "application/pdf",
                            fileName = "work_report_${monthPrefix}.pdf",
                            isPdf = true
                        )
                    } else {
                        statusMessage = strings.exportFailedMsg
                    }
                },
                onShareClick = {
                    val file = ExportHelper.exportToPdf(
                        context = context,
                        monthPrefix = monthPrefix,
                        entries = entries,
                        settings = settings,
                        language = language
                    )
                    if (file != null) {
                        ExportHelper.shareFile(
                            context = context,
                            file = file,
                            mimeType = "application/pdf",
                            title = "${strings.appName} - ${DateUtils.formatToDisplayMonth(monthPrefix, language)}"
                        )
                        statusMessage = strings.pdfSuccessMsg
                    } else {
                        statusMessage = strings.exportFailedMsg
                    }
                },
                onSaveClick = {
                    val file = ExportHelper.exportToPdf(
                        context = context,
                        monthPrefix = monthPrefix,
                        entries = entries,
                        settings = settings,
                        language = language
                    )
                    val fileName = "work_report_${monthPrefix}.pdf"
                    if (file != null && ExportHelper.saveToDownloads(context, file, "application/pdf", fileName)) {
                        statusMessage = "${strings.savedToDownloadsSuccess} ($fileName)"
                    } else {
                        statusMessage = strings.exportFailedMsg
                    }
                },
                onOpenClick = {
                    val file = ExportHelper.exportToPdf(
                        context = context,
                        monthPrefix = monthPrefix,
                        entries = entries,
                        settings = settings,
                        language = language
                    )
                    if (file != null) {
                        ExportHelper.openFile(
                            context = context,
                            file = file,
                            mimeType = "application/pdf",
                            title = "${strings.appName} - ${DateUtils.formatToDisplayMonth(monthPrefix, language)}"
                        )
                    } else {
                        statusMessage = strings.exportFailedMsg
                    }
                }
            )
        }

        // Export Excel / CSV Card
        item {
            ExportActionCard(
                title = strings.exportCsvTitle,
                description = strings.exportCsvDesc,
                icon = Icons.Default.TableChart,
                accentColor = WorkDayGreen,
                buttonText = strings.exportCsvButton,
                buttonTag = "export_csv_button",
                onPrimaryClick = {
                    val file = ExportHelper.exportToCsv(
                        context = context,
                        monthPrefix = monthPrefix,
                        entries = entries,
                        settings = settings,
                        language = language
                    )
                    if (file != null) {
                        activeExportDialog = ExportDialogState(
                            title = "${strings.appName} - ${DateUtils.formatToDisplayMonth(monthPrefix, language)}",
                            file = file,
                            mimeType = "text/csv",
                            fileName = "work_report_${monthPrefix}.csv",
                            isPdf = false
                        )
                    } else {
                        statusMessage = strings.exportFailedMsg
                    }
                },
                onShareClick = {
                    val file = ExportHelper.exportToCsv(
                        context = context,
                        monthPrefix = monthPrefix,
                        entries = entries,
                        settings = settings,
                        language = language
                    )
                    if (file != null) {
                        ExportHelper.shareFile(
                            context = context,
                            file = file,
                            mimeType = "text/csv",
                            title = "${strings.appName} - ${DateUtils.formatToDisplayMonth(monthPrefix, language)}"
                        )
                        statusMessage = strings.csvSuccessMsg
                    } else {
                        statusMessage = strings.exportFailedMsg
                    }
                },
                onSaveClick = {
                    val file = ExportHelper.exportToCsv(
                        context = context,
                        monthPrefix = monthPrefix,
                        entries = entries,
                        settings = settings,
                        language = language
                    )
                    val fileName = "work_report_${monthPrefix}.csv"
                    if (file != null && ExportHelper.saveToDownloads(context, file, "text/csv", fileName)) {
                        statusMessage = "${strings.savedToDownloadsSuccess} ($fileName)"
                    } else {
                        statusMessage = strings.exportFailedMsg
                    }
                },
                onOpenClick = {
                    val file = ExportHelper.exportToCsv(
                        context = context,
                        monthPrefix = monthPrefix,
                        entries = entries,
                        settings = settings,
                        language = language
                    )
                    if (file != null) {
                        ExportHelper.openFile(
                            context = context,
                            file = file,
                            mimeType = "text/csv",
                            title = "${strings.appName} - ${DateUtils.formatToDisplayMonth(monthPrefix, language)}"
                        )
                    } else {
                        statusMessage = strings.exportFailedMsg
                    }
                }
            )
        }

        // Quick WhatsApp / Text Summary Share
        item {
            QuickShareTextCard(
                title = strings.shareTextTitle,
                description = strings.shareTextDesc,
                icon = Icons.Default.Share,
                accentColor = DayOffBlue,
                buttonText = strings.shareTextButton,
                buttonTag = "share_summary_button",
                onClick = {
                    val summaryText = ExportHelper.generateTextSummary(
                        monthPrefix = monthPrefix,
                        entries = entries,
                        settings = settings,
                        language = language
                    )
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, summaryText)
                        type = "text/plain"
                    }
                    val shareIntent = android.content.Intent.createChooser(sendIntent, strings.shareTextButton)
                    shareIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(shareIntent)
                }
            )
        }

        if (statusMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Detailed Preview Table of Month Days
        item {
            Text(
                text = strings.previewTableTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (entries.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.noRecordsForReport,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(entries) { entry ->
                ReportDayRowCard(
                    entry = entry,
                    settings = settings,
                    language = language
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (activeExportDialog != null) {
        val state = activeExportDialog!!
        AlertDialog(
            onDismissRequest = { activeExportDialog = null },
            shape = RoundedCornerShape(20.dp),
            icon = {
                Icon(
                    imageVector = if (state.isPdf) Icons.Default.PictureAsPdf else Icons.Default.TableChart,
                    contentDescription = null,
                    tint = if (state.isPdf) SickRose else WorkDayGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (state.isPdf) strings.exportPdfTitle else strings.exportCsvTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = state.fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 1. Share Option
                    Button(
                        onClick = {
                            ExportHelper.shareFile(
                                context = context,
                                file = state.file,
                                mimeType = state.mimeType,
                                title = state.title
                            )
                            statusMessage = if (state.isPdf) strings.pdfSuccessMsg else strings.csvSuccessMsg
                            activeExportDialog = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isPdf) SickRose else WorkDayGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_share_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.shareActionTitle, fontWeight = FontWeight.Bold)
                    }

                    // 2. Save to Downloads Option
                    OutlinedButton(
                        onClick = {
                            val success = ExportHelper.saveToDownloads(
                                context = context,
                                file = state.file,
                                mimeType = state.mimeType,
                                displayName = state.fileName
                            )
                            statusMessage = if (success) "${strings.savedToDownloadsSuccess} (${state.fileName})" else strings.exportFailedMsg
                            activeExportDialog = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_save_button")
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.saveActionTitle, fontWeight = FontWeight.Bold)
                    }

                    // 3. Open Directly Option
                    OutlinedButton(
                        onClick = {
                            ExportHelper.openFile(
                                context = context,
                                file = state.file,
                                mimeType = state.mimeType,
                                title = state.title
                            )
                            activeExportDialog = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_open_button")
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.openActionTitle, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { activeExportDialog = null }
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

data class ExportDialogState(
    val title: String,
    val file: java.io.File,
    val mimeType: String,
    val fileName: String,
    val isPdf: Boolean
)

@Composable
private fun ExportActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    buttonText: String,
    buttonTag: String,
    onPrimaryClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    onOpenClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Button (opens full options dialog)
            Button(
                onClick = onPrimaryClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(buttonTag)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Direct 1-tap quick action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onShareClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.shareActionTitle, fontSize = 10.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onSaveClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.saveActionTitle, fontSize = 10.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onOpenClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.openActionTitle, fontSize = 10.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun QuickShareTextCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    buttonText: String,
    buttonTag: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(buttonTag)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ReportSummaryRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ReportDayRowCard(
    entry: WorkDayEntry,
    settings: com.example.data.model.FinancialSettings,
    language: AppLanguage
) {
    val strings = LocalAppStrings.current
    val dayName = DateUtils.getDayOfWeekShort(entry.date, language)
    val dayEarnings = if (entry.isWorkDay) CalculationUtils.calculateDayEarnings(entry, settings) else 0.0

    val dayTypeLabel = when (entry.dayType) {
        "WORK" -> strings.typeWork
        "OFF" -> strings.typeOff
        "VACATION" -> strings.typeVacation
        "SICK" -> strings.typeSick
        else -> strings.typeWork
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${entry.date} ($dayName)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (entry.isWorkDay) {
                        val ot1Text = if (entry.overtimeHours > 0) " + ${CalculationUtils.formatHours(entry.overtimeHours)} ${strings.hoursAbbrev} (${strings.overtimeHours1Label})" else ""
                        val ot2Text = if (entry.overtimeHours2 > 0) " + ${CalculationUtils.formatHours(entry.overtimeHours2)} ${strings.hoursAbbrev} (${strings.overtimeHours2Label})" else ""
                        "${CalculationUtils.formatHours(entry.regularHours)} ${strings.hoursAbbrev}$ot1Text$ot2Text"
                    } else {
                        dayTypeLabel
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (entry.isWorkDay) MaterialTheme.colorScheme.primary else DayOffBlue
                )
            }

            if (entry.isWorkDay) {
                Text(
                    text = "${CalculationUtils.formatAmount(dayEarnings)} ${settings.currencySymbol}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "-",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
