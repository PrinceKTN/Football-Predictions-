package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BestPickNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface BestPickNotificationDao {
    @Query("SELECT * FROM best_pick_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<BestPickNotification>>

    @Query("SELECT COUNT(*) FROM best_pick_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: BestPickNotification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<BestPickNotification>)

    @Query("UPDATE best_pick_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE best_pick_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM best_pick_notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM best_pick_notifications")
    suspend fun clearAll()
}
