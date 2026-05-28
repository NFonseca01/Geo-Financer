
package com.atlas.geofinancer.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity): Long

    @Query("UPDATE places SET departureTime = :departure, durationMinutes = :duration WHERE id = :id")
    suspend fun updatePlaceDeparture(id: Int, departure: String, duration: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
}
