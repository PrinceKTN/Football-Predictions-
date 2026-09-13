package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BetPrediction
import com.example.data.model.BestPickNotification

@Database(entities = [BetPrediction::class, BestPickNotification::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun betPredictionDao(): BetPredictionDao
    abstract fun bestPickNotificationDao(): BestPickNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bet_predictions_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
