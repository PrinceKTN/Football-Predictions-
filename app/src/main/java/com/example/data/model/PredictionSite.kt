package com.example.data.model

data class PredictionSite(
    val id: String,
    val name: String,
    val category: SiteCategory,
    val pricingModel: PricingModel,
    val monthlyCostUsd: Double = 0.0,
    val claimedWinRate: String,
    val realisticVerifiedRate: String,
    val verifiedYieldRoi: String,
    val predictionMethodology: String,
    val algorithmType: String,
    val pros: List<String>,
    val cons: List<String>,
    val scamRiskLevel: RiskLevel,
    val bestMarkets: List<String>,
    val howToTestFreeGuide: String,
    val auditSummary: String,
    val primaryColorHex: Long = 0xFF10B981,
    val sampleDailyPredictions: List<SampleTip> = emptyList()
)

data class SampleTip(
    val match: String,
    val league: String,
    val tip: String,
    val odds: Double,
    val market: String,
    val rationale: String
)

enum class SiteCategory(val title: String) {
    MATHEMATICAL_MODEL("Mathematical Algorithm"),
    FREE_STATS_PORTAL("Free Stats & Predictions"),
    PAID_VIP_SERVICE("Paid VIP / Subscription"),
    VERIFIED_MARKETPLACE("Audited Tipster Network")
}

enum class PricingModel(val label: String) {
    TOTALLY_FREE("100% Free Daily Games"),
    FREEMIUM("Freemium (Free + VIP Tier)"),
    PAID_SUBSCRIPTION("Strictly Paid Subscription"),
    PER_TIPSTER_MARKETPLACE("Marketplace (Free & Paid Tipsters)")
}

enum class RiskLevel(val label: String) {
    LOW_TRANSPARENT("Low Risk (Transparent Data)"),
    MODERATE_PROMOTIONAL("Moderate (Promotional Upsells)"),
    HIGH_UNVERIFIED("High Risk (Unverified Claims/Drawdown Trap)")
}
