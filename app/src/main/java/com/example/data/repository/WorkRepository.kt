package com.example.data.repository

import com.example.data.db.FinancialSettingsDao
import com.example.data.db.WorkDayDao
import com.example.data.model.FinancialSettings
import com.example.data.model.WorkDayEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkRepository(
    private val workDayDao: WorkDayDao,
    private val financialSettingsDao: FinancialSettingsDao
) {

    fun getEntriesForMonthFlow(monthPrefix: String): Flow<List<WorkDayEntry>> {
        return workDayDao.getEntriesForMonthFlow(monthPrefix)
    }

    suspend fun getEntryByDate(date: String): WorkDayEntry? {
        return workDayDao.getEntryByDate(date)
    }

    fun getEntryByDateFlow(date: String): Flow<WorkDayEntry?> {
        return workDayDao.getEntryByDateFlow(date)
    }

    suspend fun saveEntry(entry: WorkDayEntry): Long {
        return workDayDao.insertOrUpdate(entry)
    }

    suspend fun deleteEntry(entry: WorkDayEntry) {
        workDayDao.delete(entry)
    }

    suspend fun deleteEntryById(id: Long) {
        workDayDao.deleteById(id)
    }

    val settingsFlow: Flow<FinancialSettings> = financialSettingsDao.getSettingsFlow().map {
        it ?: FinancialSettings()
    }

    suspend fun getSettings(): FinancialSettings {
        return financialSettingsDao.getSettings() ?: FinancialSettings()
    }

    suspend fun updateSettings(settings: FinancialSettings) {
        financialSettingsDao.insertOrUpdate(settings)
    }
}
