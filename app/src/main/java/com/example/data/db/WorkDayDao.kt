package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WorkDayEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkDayDao {

    @Query("SELECT * FROM work_day_entries ORDER BY date DESC")
    fun getAllEntriesFlow(): Flow<List<WorkDayEntry>>

    @Query("SELECT * FROM work_day_entries WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC")
    fun getEntriesForMonthFlow(monthPrefix: String): Flow<List<WorkDayEntry>>

    @Query("SELECT * FROM work_day_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getEntriesBetweenFlow(startDate: String, endDate: String): Flow<List<WorkDayEntry>>

    @Query("SELECT * FROM work_day_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryByDate(date: String): WorkDayEntry?

    @Query("SELECT * FROM work_day_entries WHERE date = :date LIMIT 1")
    fun getEntryByDateFlow(date: String): Flow<WorkDayEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: WorkDayEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WorkDayEntry>)

    @Update
    suspend fun update(entry: WorkDayEntry)

    @Delete
    suspend fun delete(entry: WorkDayEntry)

    @Query("DELETE FROM work_day_entries WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM work_day_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM work_day_entries")
    suspend fun deleteAll()
}
