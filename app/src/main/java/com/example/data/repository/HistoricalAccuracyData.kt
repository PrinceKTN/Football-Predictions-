package com.example.data.repository

import com.example.data.model.DailyAccuracyPoint
import com.example.data.model.SiteAccuracyHistory
import java.util.Locale

object HistoricalAccuracyData {

    // Generates 30 days of data ending at today (Sep 13)
    private fun generatePoints(
        baseAccuracy: Double,
        volatility: Double,
        dailyBetVolume: Int,
        seedOffset: Int
    ): List<DailyAccuracyPoint> {
        val points = mutableListOf<DailyAccuracyPoint>()
        // 30 days from Day 1 (Aug 15) to Day 30 (Sep 13)
        for (day in 1..30) {
            val dateLabel = if (day <= 17) {
                val augDay = 14 + day
                "Aug $augDay"
            } else {
                val sepDay = day - 17
                val padded = if (sepDay < 10) "0$sepDay" else "$sepDay"
                "Sep $padded"
            }

            // Pseudo-random deterministic sine wave for smooth natural variance
            val angle = (day + seedOffset) * 0.45
            val sinVariance = kotlin.math.sin(angle) * volatility
            val cosVariance = kotlin.math.cos(angle * 1.3) * (volatility * 0.4)
            val currentAcc = (baseAccuracy + sinVariance + cosVariance).coerceIn(38.0, 78.0)

            val totalBets = dailyBetVolume + ((day * 7 + seedOffset) % 3)
            val wonBets = (totalBets * (currentAcc / 100.0)).toInt().coerceIn(1, totalBets)
            val actualAcc = (wonBets.toDouble() / totalBets.toDouble()) * 100.0

            points.add(
                DailyAccuracyPoint(
                    dayNumber = day,
                    dateLabel = dateLabel,
                    accuracyPercentage = String.format(Locale.US, "%.1f", actualAcc).toDouble(),
                    betsWon = wonBets,
                    betsTotal = totalBets
                )
            )
        }
        return points
    }

    val siteHistories: List<SiteAccuracyHistory> by lazy {
        val forebetPoints = generatePoints(baseAccuracy = 57.2, volatility = 5.8, dailyBetVolume = 12, seedOffset = 2)
        val predictzPoints = generatePoints(baseAccuracy = 55.4, volatility = 6.2, dailyBetVolume = 10, seedOffset = 7)
        val windrawwinPoints = generatePoints(baseAccuracy = 54.8, volatility = 5.5, dailyBetVolume = 9, seedOffset = 13)
        val vitibetPoints = generatePoints(baseAccuracy = 52.6, volatility = 4.8, dailyBetVolume = 8, seedOffset = 19)
        val statareaPoints = generatePoints(baseAccuracy = 51.5, volatility = 5.2, dailyBetVolume = 11, seedOffset = 23)
        val betensuredPoints = generatePoints(baseAccuracy = 49.2, volatility = 7.1, dailyBetVolume = 7, seedOffset = 31)
        val tipstrrPoints = generatePoints(baseAccuracy = 58.6, volatility = 6.8, dailyBetVolume = 6, seedOffset = 41)

        // All-Sites Aggregate
        val aggregatePoints = (1..30).map { day ->
            val pts = listOf(
                forebetPoints[day - 1],
                predictzPoints[day - 1],
                windrawwinPoints[day - 1],
                vitibetPoints[day - 1],
                statareaPoints[day - 1],
                betensuredPoints[day - 1],
                tipstrrPoints[day - 1]
            )
            val totalWon = pts.sumOf { it.betsWon }
            val totalBets = pts.sumOf { it.betsTotal }
            val acc = if (totalBets > 0) (totalWon.toDouble() / totalBets) * 100.0 else 50.0
            DailyAccuracyPoint(
                dayNumber = day,
                dateLabel = pts.first().dateLabel,
                accuracyPercentage = String.format(Locale.US, "%.1f", acc).toDouble(),
                betsWon = totalWon,
                betsTotal = totalBets
            )
        }

        listOf(
            SiteAccuracyHistory(
                siteId = "all_aggregate",
                siteName = "All Sites Benchmark",
                category = "Cross-Site Consensus",
                colorHex = 0xFF38BDF8, // Light Blue
                overall30DayAccuracy = 54.3,
                total30DayBets = aggregatePoints.sumOf { it.betsTotal },
                total30DayWon = aggregatePoints.sumOf { it.betsWon },
                netRoiPercentage = 3.2,
                bestStreak = 11,
                dailyPoints = aggregatePoints
            ),
            SiteAccuracyHistory(
                siteId = "forebet",
                siteName = "Forebet",
                category = "Poisson Math Model",
                colorHex = 0xFF10B981, // Emerald
                overall30DayAccuracy = 57.2,
                total30DayBets = forebetPoints.sumOf { it.betsTotal },
                total30DayWon = forebetPoints.sumOf { it.betsWon },
                netRoiPercentage = 6.4,
                bestStreak = 8,
                dailyPoints = forebetPoints
            ),
            SiteAccuracyHistory(
                siteId = "predictz",
                siteName = "PredictZ",
                category = "Trend & Form Analysis",
                colorHex = 0xFF3B82F6, // Blue
                overall30DayAccuracy = 55.4,
                total30DayBets = predictzPoints.sumOf { it.betsTotal },
                total30DayWon = predictzPoints.sumOf { it.betsWon },
                netRoiPercentage = 4.1,
                bestStreak = 7,
                dailyPoints = predictzPoints
            ),
            SiteAccuracyHistory(
                siteId = "windrawwin",
                siteName = "Windrawwin",
                category = "Form & Over/Under Trends",
                colorHex = 0xFF06B6D4, // Cyan
                overall30DayAccuracy = 54.8,
                total30DayBets = windrawwinPoints.sumOf { it.betsTotal },
                total30DayWon = windrawwinPoints.sumOf { it.betsWon },
                netRoiPercentage = 3.6,
                bestStreak = 6,
                dailyPoints = windrawwinPoints
            ),
            SiteAccuracyHistory(
                siteId = "vitibet",
                siteName = "Vitibet",
                category = "Rating Index Table",
                colorHex = 0xFFEC4899, // Pink
                overall30DayAccuracy = 52.6,
                total30DayBets = vitibetPoints.sumOf { it.betsTotal },
                total30DayWon = vitibetPoints.sumOf { it.betsWon },
                netRoiPercentage = 0.8,
                bestStreak = 5,
                dailyPoints = vitibetPoints
            ),
            SiteAccuracyHistory(
                siteId = "statarea",
                siteName = "Statarea",
                category = "Crowd Wisdom Engine",
                colorHex = 0xFFEAB308, // Yellow
                overall30DayAccuracy = 51.5,
                total30DayBets = statareaPoints.sumOf { it.betsTotal },
                total30DayWon = statareaPoints.sumOf { it.betsWon },
                netRoiPercentage = -1.2,
                bestStreak = 4,
                dailyPoints = statareaPoints
            ),
            SiteAccuracyHistory(
                siteId = "betensured",
                siteName = "Betensured",
                category = "Commercial Freemium Portal",
                colorHex = 0xFFF59E0B, // Amber
                overall30DayAccuracy = 49.2,
                total30DayBets = betensuredPoints.sumOf { it.betsTotal },
                total30DayWon = betensuredPoints.sumOf { it.betsWon },
                netRoiPercentage = -4.5,
                bestStreak = 4,
                dailyPoints = betensuredPoints
            ),
            SiteAccuracyHistory(
                siteId = "tipstrr",
                siteName = "Tipstrr",
                category = "Audited Tipster Network",
                colorHex = 0xFF8B5CF6, // Purple
                overall30DayAccuracy = 58.6,
                total30DayBets = tipstrrPoints.sumOf { it.betsTotal },
                total30DayWon = tipstrrPoints.sumOf { it.betsWon },
                netRoiPercentage = 8.1,
                bestStreak = 9,
                dailyPoints = tipstrrPoints
            )
        )
    }
}
