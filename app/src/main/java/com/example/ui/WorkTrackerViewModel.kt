package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.WorkTrackerDatabase
import com.example.data.model.DayType
import com.example.data.model.FinancialSettings
import com.example.data.model.WorkDayEntry
import com.example.data.repository.WorkRepository
import com.example.notification.NotificationScheduler
import com.example.util.AppLanguage
import com.example.util.CalculationUtils
import com.example.util.DateUtils
import com.example.util.ExportHelper
import com.example.util.MonthFinancialSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class DayFilter {
    ALL,
    WORK,
    OFF
}

class WorkTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WorkTrackerDatabase.getDatabase(application)
    private val repository = WorkRepository(
        database.workDayDao(),
        database.financialSettingsDao()
    )

    private val _currentMonthPrefix = MutableStateFlow(DateUtils.getCurrentMonthPrefix())
    val currentMonthPrefix: StateFlow<String> = _currentMonthPrefix.asStateFlow()

    private val _selectedFilter = MutableStateFlow(DayFilter.ALL)
    val selectedFilter: StateFlow<DayFilter> = _selectedFilter.asStateFlow()

    val settings: StateFlow<FinancialSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialSettings()
    )

    // Language state
    val appLanguage: StateFlow<AppLanguage> = settings.map {
        AppLanguage.fromCode(it.appLanguage)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppLanguage.AR
    )

    // Theme state: "LIGHT", "DARK", "SYSTEM"
    val appTheme: StateFlow<String> = settings.map {
        it.appTheme
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "SYSTEM"
    )

    // Raw entries for currently selected month
    val currentMonthEntries: StateFlow<List<WorkDayEntry>> = _currentMonthPrefix
        .flatMapLatest { prefix ->
            repository.getEntriesForMonthFlow(prefix)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered entries for UI display
    val filteredEntries: StateFlow<List<WorkDayEntry>> = combine(
        currentMonthEntries,
        _selectedFilter
    ) { entries, filter ->
        when (filter) {
            DayFilter.ALL -> entries
            DayFilter.WORK -> entries.filter { it.isWorkDay }
            DayFilter.OFF -> entries.filter { !it.isWorkDay }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dynamic Monthly Financial and Hours Summary
    val monthSummary: StateFlow<MonthFinancialSummary> = combine(
        currentMonthEntries,
        settings
    ) { entries, settingsVal ->
        CalculationUtils.computeMonthSummary(entries, settingsVal)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalculationUtils.computeMonthSummary(emptyList(), FinancialSettings())
    )

    // Standard Editing modal state
    private val _editingEntry = MutableStateFlow<WorkDayEntry?>(null)
    val editingEntry: StateFlow<WorkDayEntry?> = _editingEntry.asStateFlow()

    private val _isEditorOpen = MutableStateFlow(false)
    val isEditorOpen: StateFlow<Boolean> = _isEditorOpen.asStateFlow()

    // Quick Return from Work Modal state
    private val _isQuickReturnOpen = MutableStateFlow(false)
    val isQuickReturnOpen: StateFlow<Boolean> = _isQuickReturnOpen.asStateFlow()

    // Navigation state
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    init {
        // Schedule WorkManager daily notification for 6:00 PM (18:00)
        viewModelScope.launch {
            val current = repository.getSettings()
            if (current.reminderEnabled) {
                NotificationScheduler.scheduleDailyReminder(
                    context = getApplication(),
                    hourOfDay = current.reminderHour,
                    minute = current.reminderMinute
                )
            }
        }
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
    }

    fun setFilter(filter: DayFilter) {
        _selectedFilter.value = filter
    }

    fun previousMonth() {
        _currentMonthPrefix.value = DateUtils.getPreviousMonthPrefix(_currentMonthPrefix.value)
    }

    fun nextMonth() {
        _currentMonthPrefix.value = DateUtils.getNextMonthPrefix(_currentMonthPrefix.value)
    }

    fun goToCurrentMonth() {
        _currentMonthPrefix.value = DateUtils.getCurrentMonthPrefix()
    }

    fun setMonthPrefix(prefix: String) {
        _currentMonthPrefix.value = prefix
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch {
            val updated = settings.value.copy(appLanguage = language.code)
            repository.updateSettings(updated)
        }
    }

    fun setAppTheme(theme: String) {
        viewModelScope.launch {
            val updated = settings.value.copy(appTheme = theme)
            repository.updateSettings(updated)
        }
    }

    fun updateReminderConfig(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            val updated = settings.value.copy(
                reminderEnabled = enabled,
                reminderHour = hour,
                reminderMinute = minute
            )
            repository.updateSettings(updated)

            if (enabled) {
                NotificationScheduler.scheduleDailyReminder(getApplication(), hour, minute)
            } else {
                NotificationScheduler.cancelReminder(getApplication())
            }
        }
    }

    fun openEditorForDate(dateStr: String) {
        viewModelScope.launch {
            val existing = repository.getEntryByDate(dateStr)
            _editingEntry.value = existing ?: WorkDayEntry(
                date = dateStr,
                isWorkDay = true,
                dayType = DayType.WORK.name,
                regularHours = settings.value.standardHoursPerDay,
                overtimeHours = 0.0,
                overtimeHours2 = 0.0,
                notes = ""
            )
            _isEditorOpen.value = true
        }
    }

    fun openEditorForNewToday() {
        openEditorForDate(DateUtils.getTodayDate())
    }

    fun closeEditor() {
        _isEditorOpen.value = false
        _editingEntry.value = null
    }

    fun openQuickReturnDialog() {
        _isQuickReturnOpen.value = true
    }

    fun closeQuickReturnDialog() {
        _isQuickReturnOpen.value = false
    }

    fun quickLogTodayReturn(
        regularHours: Double,
        overtimeHours: Double,
        overtimeHours2: Double = 0.0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val todayStr = DateUtils.getTodayDate()
            val existing = repository.getEntryByDate(todayStr)
            val entryToSave = (existing ?: WorkDayEntry(date = todayStr)).copy(
                date = todayStr,
                isWorkDay = true,
                dayType = DayType.WORK.name,
                regularHours = regularHours,
                overtimeHours = overtimeHours,
                overtimeHours2 = overtimeHours2,
                notes = notes
            )
            repository.saveEntry(entryToSave)
            closeQuickReturnDialog()
        }
    }

    fun saveEntry(
        date: String,
        isWorkDay: Boolean,
        dayType: String,
        regularHours: Double,
        overtimeHours: Double,
        overtimeHours2: Double = 0.0,
        notes: String
    ) {
        viewModelScope.launch {
            val existing = repository.getEntryByDate(date)
            val entryToSave = (existing ?: WorkDayEntry(date = date)).copy(
                date = date,
                isWorkDay = isWorkDay,
                dayType = dayType,
                regularHours = if (isWorkDay) regularHours else 0.0,
                overtimeHours = if (isWorkDay) overtimeHours else 0.0,
                overtimeHours2 = if (isWorkDay) overtimeHours2 else 0.0,
                notes = notes
            )
            repository.saveEntry(entryToSave)
            closeEditor()
        }
    }

    fun quickToggleWorkDay(entry: WorkDayEntry) {
        viewModelScope.launch {
            val updated = if (entry.isWorkDay) {
                entry.copy(
                    isWorkDay = false,
                    dayType = DayType.OFF.name,
                    regularHours = 0.0,
                    overtimeHours = 0.0,
                    overtimeHours2 = 0.0
                )
            } else {
                entry.copy(
                    isWorkDay = true,
                    dayType = DayType.WORK.name,
                    regularHours = settings.value.standardHoursPerDay,
                    overtimeHours = 0.0,
                    overtimeHours2 = 0.0
                )
            }
            repository.saveEntry(updated)
        }
    }

    fun deleteEntry(entry: WorkDayEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            if (_editingEntry.value?.id == entry.id) {
                closeEditor()
            }
        }
    }

    fun updateFinancialSettings(updatedSettings: FinancialSettings) {
        viewModelScope.launch {
            repository.updateSettings(updatedSettings)
            if (updatedSettings.reminderEnabled) {
                NotificationScheduler.scheduleDailyReminder(
                    getApplication(),
                    updatedSettings.reminderHour,
                    updatedSettings.reminderMinute
                )
            } else {
                NotificationScheduler.cancelReminder(getApplication())
            }
        }
    }

    fun exportPdf(context: Context, onResult: (File?) -> Unit) {
        val file = ExportHelper.exportToPdf(
            context = context,
            monthPrefix = _currentMonthPrefix.value,
            entries = currentMonthEntries.value,
            settings = settings.value,
            language = appLanguage.value
        )
        onResult(file)
    }

    fun exportCsv(context: Context, onResult: (File?) -> Unit) {
        val file = ExportHelper.exportToCsv(
            context = context,
            monthPrefix = _currentMonthPrefix.value,
            entries = currentMonthEntries.value,
            settings = settings.value,
            language = appLanguage.value
        )
        onResult(file)
    }

    fun shareSummaryText(context: Context) {
        val summaryText = ExportHelper.generateTextSummary(
            monthPrefix = _currentMonthPrefix.value,
            entries = currentMonthEntries.value,
            settings = settings.value,
            language = appLanguage.value
        )
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, summaryText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
