package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BetPrediction
import kotlinx.coroutines.flow.Flow

@Dao
interface BetPredictionDao {
    @Query("SELECT * FROM bet_predictions ORDER BY timestamp DESC")
    fun getAllPredictions(): Flow<List<BetPrediction>>

    @Query("SELECT * FROM bet_predictions WHERE siteId = :siteId ORDER BY timestamp DESC")
    fun getPredictionsBySite(siteId: String): Flow<List<BetPrediction>>

    @Query("SELECT COUNT(*) FROM bet_predictions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: BetPrediction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(predictions: List<BetPrediction>)

    @Update
    suspend fun updatePrediction(prediction: BetPrediction)

    @Delete
    suspend fun deletePrediction(prediction: BetPrediction)

    @Query("DELETE FROM bet_predictions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bet_predictions")
    suspend fun clearAll()
}
