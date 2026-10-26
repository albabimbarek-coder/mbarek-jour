package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.FinancialSettings
import com.example.data.model.WorkDayEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [WorkDayEntry::class, FinancialSettings::class],
    version = 3,
    exportSchema = false
)
abstract class WorkTrackerDatabase : RoomDatabase() {

    abstract fun workDayDao(): WorkDayDao
    abstract fun financialSettingsDao(): FinancialSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: WorkTrackerDatabase? = null

        fun getDatabase(context: Context): WorkTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkTrackerDatabase::class.java,
                    "work_tracker_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    // Insert initial default financial settings
                    database.financialSettingsDao().insertOrUpdate(
                        FinancialSettings(
                            id = 1,
                            calculationMode = "HOURLY",
                            hourlyRate = 35.0,
                            monthlyBaseSalary = 5000.0,
                            standardHoursPerDay = 8.0,
                            workingDaysPerMonth = 22,
                            overtimeMultiplier = 1.0,
                            overtimeMultiplier2 = 1.5,
                            currencySymbol = "د.م"
                        )
                    )

                    // Seed some sample records for current month
                    val calendar = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

                    val sampleEntries = mutableListOf<WorkDayEntry>()
                    // Populate up to 10 past days in the current month
                    for (day in 1..currentDay) {
                        calendar.set(Calendar.DAY_OF_MONTH, day)
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        val dateStr = dateFormat.format(calendar.time)

                        val isFridayOrSaturday = dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY
                        if (isFridayOrSaturday) {
                            sampleEntries.add(
                                WorkDayEntry(
                                    date = dateStr,
                                    isWorkDay = false,
                                    dayType = "OFF",
                                    regularHours = 0.0,
                                    overtimeHours = 0.0,
                                    notes = "عطلة نهاية الأسبوع"
                                )
                            )
                        } else {
                            // Alternate overtime occasionally
                            val overtime = if (day % 3 == 0) 2.0 else 0.0
                            sampleEntries.add(
                                WorkDayEntry(
                                    date = dateStr,
                                    isWorkDay = true,
                                    dayType = "WORK",
                                    regularHours = 8.0,
                                    overtimeHours = overtime,
                                    notes = if (overtime > 0) "إنجاز مهام إضافية وتدقيق التقارير" else "ساعات عمل عادية"
                                )
                            )
                        }
                    }

                    if (sampleEntries.isNotEmpty()) {
                        database.workDayDao().insertAll(sampleEntries)
                    }
                }
            }
        }
    }
}
