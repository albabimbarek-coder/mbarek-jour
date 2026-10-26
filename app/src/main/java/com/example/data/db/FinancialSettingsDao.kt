package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FinancialSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialSettingsDao {

    @Query("SELECT * FROM financial_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<FinancialSettings?>

    @Query("SELECT * FROM financial_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): FinancialSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: FinancialSettings)
}
