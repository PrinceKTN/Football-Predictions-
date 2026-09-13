package com.example.data.model

data class SportyBetPick(
    val id: String,
    val match: String,
    val homeTeam: String,
    val awayTeam: String,
    val league: String,
    val country: String,
    val scheduleDay: String, // "Today" or "Tomorrow"
    val kickOffTime: String,
    val tip: String,
    val estimatedOdds: Double,
    val sportyBetMarketName: String, // e.g. "1X2", "Over/Under 2.5", "GG / Both Teams To Score", "Double Chance"
    val sportyBetSelection: String, // e.g. "1 (Home Win)", "Over 2.5", "Yes (GG)", "1X"
    val algorithmicSource: String,
    val confidenceLevel: String,
    val stepByStepSportyBetGuide: String
)
