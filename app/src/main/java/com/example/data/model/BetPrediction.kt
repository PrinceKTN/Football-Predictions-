package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bet_predictions")
data class BetPrediction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val siteId: String,
    val siteName: String,
    val matchTitle: String,
    val league: String,
    val predictionTip: String,
    val marketType: String, // "1X2", "Over/Under 2.5", "BTTS", "Double Chance", "Correct Score"
    val odds: Double,
    val stakeUnits: Double = 1.0,
    val date: String,
    val status: PredictionStatus = PredictionStatus.PENDING,
    val isFreeDaily: Boolean = true,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class PredictionStatus(val label: String) {
    PENDING("Pending ⏳"),
    WON("Won 🟢"),
    LOST("Lost 🔴"),
    VOID("Void ⚪")
}

data class SiteStats(
    val siteName: String,
    val totalBets: Int,
    val wonBets: Int,
    val lostBets: Int,
    val voidBets: Int,
    val pendingBets: Int,
    val winRatePercentage: Double,
    val netProfitUnits: Double,
    val roiPercentage: Double,
    val sampleSizeRating: String, // "Low (Need 30+)", "Moderate (30-50)", "Statistically Sound (50+)"
    val tasteVerdict: String // e.g., "Highly Profitable", "Break-Even", "Vig Drain"
)
