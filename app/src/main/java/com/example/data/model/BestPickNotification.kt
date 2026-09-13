package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "best_pick_notifications")
data class BestPickNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val matchTitle: String,
    val predictionTip: String,
    val odds: Double,
    val confidence: Int,
    val siteSource: String,
    val sportyBetCode: String = "",
    val scheduleDay: String = "Today",
    val updateType: String = "NEW_PICK", // "NEW_PICK", "ODDS_UPDATE", "SETTLEMENT_ALERT"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
