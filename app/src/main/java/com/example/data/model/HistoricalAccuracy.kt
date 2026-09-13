package com.example.data.model

data class DailyAccuracyPoint(
    val dayNumber: Int, // 1 to 30
    val dateLabel: String, // e.g. "Aug 15", "Sep 01"
    val accuracyPercentage: Double, // e.g. 56.5
    val betsWon: Int,
    val betsTotal: Int
)

data class SiteAccuracyHistory(
    val siteId: String,
    val siteName: String,
    val category: String,
    val colorHex: Long,
    val overall30DayAccuracy: Double,
    val total30DayBets: Int,
    val total30DayWon: Int,
    val netRoiPercentage: Double,
    val bestStreak: Int,
    val breakEvenThreshold: Double = 52.38, // Standard -110 / 1.91 break-even
    val dailyPoints: List<DailyAccuracyPoint>
)
