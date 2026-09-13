package com.example.data.repository

import com.example.data.local.BetPredictionDao
import com.example.data.model.BetPrediction
import com.example.data.model.PredictionSite
import com.example.data.model.PredictionStatus
import com.example.data.model.PricingModel
import com.example.data.model.RiskLevel
import com.example.data.model.SampleTip
import com.example.data.model.SiteCategory
import com.example.data.model.SiteStats
import com.example.data.model.SportyBetPick
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PredictionRepository(private val dao: BetPredictionDao) {

    val allPredictions: Flow<List<BetPrediction>> = dao.getAllPredictions()

    val siteStats: Flow<List<SiteStats>> = allPredictions.map { predictions ->
        calculateStats(predictions)
    }

    suspend fun insert(prediction: BetPrediction): Long = dao.insertPrediction(prediction)

    suspend fun update(prediction: BetPrediction) = dao.updatePrediction(prediction)

    suspend fun delete(prediction: BetPrediction) = dao.deletePrediction(prediction)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun initializeDefaultPredictionsIfEmpty() {
        if (dao.getCount() == 0) {
            dao.insertAll(initialSamplePredictions)
        }
    }

    private fun calculateStats(predictions: List<BetPrediction>): List<SiteStats> {
        val grouped = predictions.groupBy { it.siteName }
        return grouped.map { (siteName, bets) ->
            val settledBets = bets.filter { it.status == PredictionStatus.WON || it.status == PredictionStatus.LOST }
            val won = bets.count { it.status == PredictionStatus.WON }
            val lost = bets.count { it.status == PredictionStatus.LOST }
            val voidCount = bets.count { it.status == PredictionStatus.VOID }
            val pending = bets.count { it.status == PredictionStatus.PENDING }

            val totalSettled = settledBets.size
            val winRate = if (totalSettled > 0) (won.toDouble() / totalSettled) * 100.0 else 0.0

            var totalProfit = 0.0
            var totalRisked = 0.0

            bets.forEach { bet ->
                when (bet.status) {
                    PredictionStatus.WON -> {
                        totalProfit += (bet.odds - 1.0) * bet.stakeUnits
                        totalRisked += bet.stakeUnits
                    }
                    PredictionStatus.LOST -> {
                        totalProfit -= bet.stakeUnits
                        totalRisked += bet.stakeUnits
                    }
                    PredictionStatus.VOID -> {
                        // Stake returned, no profit/loss
                    }
                    PredictionStatus.PENDING -> {
                        // Not settled yet
                    }
                }
            }

            val roi = if (totalRisked > 0.0) (totalProfit / totalRisked) * 100.0 else 0.0

            val sampleRating = when {
                totalSettled < 10 -> "Premature (<10 bets)"
                totalSettled < 30 -> "Early Phase (10-30 bets)"
                totalSettled < 50 -> "Moderate Sample (30-50 bets)"
                else -> "Statistically Valid (50+ bets)"
            }

            val tasteVerdict = when {
                totalSettled == 0 -> "Awaiting Results"
                roi > 12.0 && totalSettled >= 15 -> "High Edge (Leader)"
                roi > 3.0 -> "Positive Yield (Profitable)"
                roi >= -3.0 -> "Break-Even (Vig Boundary)"
                roi >= -15.0 -> "Negative Drift (House Edge)"
                else -> "Underperforming (Severe Vig Drain)"
            }

            SiteStats(
                siteName = siteName,
                totalBets = bets.size,
                wonBets = won,
                lostBets = lost,
                voidBets = voidCount,
                pendingBets = pending,
                winRatePercentage = winRate,
                netProfitUnits = totalProfit,
                roiPercentage = roi,
                sampleSizeRating = sampleRating,
                tasteVerdict = tasteVerdict
            )
        }.sortedByDescending { it.netProfitUnits }
    }

    // Curated Deep Analysis Database of the most prominent prediction platforms
    val analyzedSites: List<PredictionSite> = listOf(
        PredictionSite(
            id = "forebet",
            name = "Forebet",
            category = SiteCategory.MATHEMATICAL_MODEL,
            pricingModel = PricingModel.TOTALLY_FREE,
            monthlyCostUsd = 0.0,
            claimedWinRate = "~65% - 70% (on favored selections)",
            realisticVerifiedRate = "51.4% - 54.8% (flat stake)",
            verifiedYieldRoi = "+2.8% to -1.5% (league dependent)",
            predictionMethodology = "Pure mathematical algorithm based on Poisson distribution, Elo ratings, home advantage metrics, and probability percentage calculation.",
            algorithmType = "Poisson Distribution & Historical Statistical Regression",
            pros = listOf(
                "100% free daily predictions covering 500+ worldwide leagues",
                "Full transparency with probability percentages (e.g., 48% - 28% - 24%)",
                "Provides exact score predictions and Over/Under 2.5 goals probabilities",
                "No deceptive paywalls or fake VIP upsells"
            ),
            cons = listOf(
                "Cannot account for last-minute team news, manager changes, or weather",
                "High volume of games means users must filter for value manually",
                "Short odds (<1.40) inflate win percentage but have low or negative ROI"
            ),
            scamRiskLevel = RiskLevel.LOW_TRANSPARENT,
            bestMarkets = listOf("Over 2.5 Goals", "Double Chance (1X/X2)", "Under 3.5 Goals"),
            howToTestFreeGuide = "Take 30 consecutive matches where Forebet calculates >60% probability for a home win with minimum odds of 1.70. Record flat 1-unit bets. Reject odds below 1.50 as bookmaker margin erodes edge.",
            auditSummary = "Forebet is an industry benchmark for algorithmic baseline odds. It does not sell 'fixed tips' or scam packages. Best used as a secondary data verification tool rather than blindly copying 1X2 tips.",
            primaryColorHex = 0xFF0284C7,
            sampleDailyPredictions = listOf(
                SampleTip("Arsenal vs Chelsea", "Premier League", "Arsenal Win (1)", 1.85, "1X2", "Algorithm calculates 58% probability vs implied 54% bookmaker odds"),
                SampleTip("Dortmund vs Leverkusen", "Bundesliga", "Over 2.5 Goals", 1.68, "Over/Under 2.5", "Expected goals model projects 3.4 total match goals")
            )
        ),
        PredictionSite(
            id = "predictz",
            name = "PredictZ",
            category = SiteCategory.FREE_STATS_PORTAL,
            pricingModel = PricingModel.TOTALLY_FREE,
            monthlyCostUsd = 0.0,
            claimedWinRate = "Promotes high win streaks in top leagues",
            realisticVerifiedRate = "49.5% - 53.2% (1X2 match picks)",
            verifiedYieldRoi = "-0.5% to +2.1% (flat stake)",
            predictionMethodology = "Recent form weighting (last 5-6 games), home/away goal differential, league table position, and head-to-head records.",
            algorithmType = "Weighted Form Index & Goal Scoring Frequency",
            pros = listOf(
                "Completely free daily predictions with clean scoreline projections",
                "Detailed 5-game form guide and head-to-head match history",
                "Strong dedicated sections for Both Teams to Score (BTTS) and Over 2.5",
                "Long-standing reputation with no aggressive subscription traps"
            ),
            cons = listOf(
                "Tendency to favor heavy home favorites with minimal betting value",
                "Does not publish audited historical profit/loss ledger",
                "No live in-play adjustments"
            ),
            scamRiskLevel = RiskLevel.LOW_TRANSPARENT,
            bestMarkets = listOf("Both Teams To Score (BTTS - Yes)", "Home Win & Over 1.5"),
            howToTestFreeGuide = "Focus strictly on PredictZ's 'Both Teams to Score' predictions for teams with >70% BTTS season rate. Track 40 matches with 1-unit flat staking.",
            auditSummary = "Reliable free portal for quick form-checking. Never pay third parties who claim to resell 'VIP PredictZ leaks'—all legitimate PredictZ content is 100% free.",
            primaryColorHex = 0xFF10B981,
            sampleDailyPredictions = listOf(
                SampleTip("Real Madrid vs Sevilla", "La Liga", "Both Teams to Score (Yes)", 1.75, "BTTS", "Both sides conceded in 5 of their last 6 head-to-head encounters"),
                SampleTip("Atalanta vs Roma", "Serie A", "Over 2.5 Goals", 1.82, "Over/Under 2.5", "High average offensive tempo and open transition phases")
            )
        ),
        PredictionSite(
            id = "windrawwin",
            name = "Windrawwin",
            category = SiteCategory.FREE_STATS_PORTAL,
            pricingModel = PricingModel.TOTALLY_FREE,
            monthlyCostUsd = 0.0,
            claimedWinRate = "Displays daily accumulator strike rate",
            realisticVerifiedRate = "50.0% - 53.5%",
            verifiedYieldRoi = "+1.5% to -2.0%",
            predictionMethodology = "Extensive statistical database tracking half-time/full-time results, BTTS streaks, clean sheets, and predicted scorelines.",
            algorithmType = "Trend & Streak Regression Matrix",
            pros = listOf(
                "Deepest free statistical archive for niche European and South American leagues",
                "Popular 'Most Likely BTTS' and 'Most Likely Over 2.5' tables",
                "Provides daily accumulator suggestions for casual punters",
                "Completely free without subscription gates"
            ),
            cons = listOf(
                "Accumulator tips suffer from compounded bookmaker vig/margins",
                "Lacks advanced metrics like xG (Expected Goals) or shot quality",
                "No audited ROI ledger"
            ),
            scamRiskLevel = RiskLevel.LOW_TRANSPARENT,
            bestMarkets = listOf("Over 1.5 Goals", "Double Chance", "BTTS"),
            howToTestFreeGuide = "Test their 'BTTS Top Recommendations' as single bets only (never parlay/accumulate them, as vigorish multiplies).",
            auditSummary = "Great free statistical engine. The free daily tips are solid for recreational study, but punters must avoid parlaying them into 5-folds which kill long-term EV.",
            primaryColorHex = 0xFF6366F1,
            sampleDailyPredictions = listOf(
                SampleTip("Feyenoord vs Utrecht", "Eredivisie", "Over 2.5 Goals", 1.62, "Over/Under 2.5", "Eredivisie goal trend exceeds 3.10 match average"),
                SampleTip("Celtic vs Aberdeen", "Scottish Premiership", "Celtic -1.5 Handicap", 1.90, "Handicap", "Dominant home possession and high shot volume")
            )
        ),
        PredictionSite(
            id = "betensured",
            name = "Betensured",
            category = SiteCategory.PAID_VIP_SERVICE,
            pricingModel = PricingModel.FREEMIUM,
            monthlyCostUsd = 35.0,
            claimedWinRate = "Claims 85%+ win rate on VIP / Mega plans",
            realisticVerifiedRate = "54.0% - 59.0% (mostly low odds)",
            verifiedYieldRoi = "-4.5% to +1.2% (after subtracting $35-$70/mo fee)",
            predictionMethodology = "In-house analysts combined with statistical models. Offers free basic games and tiered VIP categories (Basic, Premium, Platinum).",
            algorithmType = "Human Tipster Syndicate + Historical Analysis",
            pros = listOf(
                "Offers a free daily tier allowing users to test without paying upfront",
                "Categorized plans (Draws, Over 1.5, Super Weekend, Expert Picks)",
                "Widely known brand in African and European football prediction circles"
            ),
            cons = listOf(
                "Claims of 85%+ win rate are heavily misleading (relies on odds of 1.20-1.35)",
                "Monthly subscription fee of $35-$70 creates massive 'subscription drag'",
                "A $50 fee requires a $2,500 bankroll just to break even at a 2% monthly ROI"
            ),
            scamRiskLevel = RiskLevel.MODERATE_PROMOTIONAL,
            bestMarkets = listOf("Over 1.5 Goals (Free tier)", "Double Chance"),
            howToTestFreeGuide = "Track their Free Plan tips for 30 consecutive days. Calculate whether the net gain would have paid for the $35 VIP subscription after unit sizing.",
            auditSummary = "Classic freemium model. While legitimate in providing tips, their high win rate claims exploit punters who don't understand that winning 80% of bets at 1.15 odds still loses money over time.",
            primaryColorHex = 0xFFF59E0B,
            sampleDailyPredictions = listOf(
                SampleTip("PSG vs Monaco", "Ligue 1", "PSG Win (1)", 1.55, "1X2", "Heavy home favorite selection from Basic tier"),
                SampleTip("Benfica vs Braga", "Primeira Liga", "Over 1.5 Goals", 1.25, "Over/Under 1.5", "Low odds banker pick used to prop up win rate")
            )
        ),
        PredictionSite(
            id = "tipstrr",
            name = "Tipstrr",
            category = SiteCategory.VERIFIED_MARKETPLACE,
            pricingModel = PricingModel.PER_TIPSTER_MARKETPLACE,
            monthlyCostUsd = 25.0,
            claimedWinRate = "Open proofing shows authentic 35% - 65% depending on odds",
            realisticVerifiedRate = "Audited: 42% - 58% depending on market",
            verifiedYieldRoi = "+4.0% to +14.0% (top 5% of verified tipsters)",
            predictionMethodology = "Automated third-party proofing platform where individual tipsters log selections before kick-off with verified bookmaker odds.",
            algorithmType = "Third-Party Audited Human & Algorithmic Handicappers",
            pros = listOf(
                "Real third-party verification: odds and timing cannot be faked retroactively",
                "Transparent Yield %, ROI %, Profit, Drawdown, and Strike Rate charts",
                "Many tipsters offer 7-day free trial periods or free daily tips",
                "Filters out 95% of fake '100% fixed match' internet scams"
            ),
            cons = listOf(
                "Subscription cost per individual tipster ($20-$50/month) adds up",
                "Odds drop fast after a top tipster posts a pick (closing line value erosion)",
                "Past performance does not guarantee future results (mean reversion risk)"
            ),
            scamRiskLevel = RiskLevel.LOW_TRANSPARENT,
            bestMarkets = listOf("Asian Handicap", "Value 1X2 Outliers", "Player Props"),
            howToTestFreeGuide = "Follow 3 verified tipsters with 500+ proofed bets and a yield >6%. Log their free daily tips in this app's Taste & Test ledger for 4 weeks.",
            auditSummary = "The gold standard for legitimate proofing. Unlike unverified Telegram or Instagram tipsters, Tipstrr locks picks before games start, making it impossible to erase losses.",
            primaryColorHex = 0xFF14B8A6,
            sampleDailyPredictions = listOf(
                SampleTip("Aston Villa vs Newcastle", "Premier League", "Newcastle +0.25 AH", 1.95, "Asian Handicap", "Value edge detected against soft market opening line"),
                SampleTip("Sevilla vs Real Betis", "La Liga", "Under 2.5 Goals", 1.88, "Over/Under 2.5", "Derby intensity historically reduces open goal opportunities")
            )
        ),
        PredictionSite(
            id = "blogabet",
            name = "Blogabet",
            category = SiteCategory.VERIFIED_MARKETPLACE,
            pricingModel = PricingModel.PER_TIPSTER_MARKETPLACE,
            monthlyCostUsd = 30.0,
            claimedWinRate = "Audited transparent records (Pinnacle/Bet365 verified)",
            realisticVerifiedRate = "48% - 60% (odds-dependent)",
            verifiedYieldRoi = "+3.5% to +11.0% (for verified PRO tipsters)",
            predictionMethodology = "Decade-old verified tipster network with automated bookmaker API integration that records exact odds at publication time.",
            algorithmType = "Audited Community Tipsters & Sharp Syndicate Bettors",
            pros = listOf(
                "Impossible to fake or doctor win slips—tracked via direct bookmaker APIs",
                "Thousands of free tipsters publishing daily tips for public testing",
                "Deep breakdown of yield by league, sport, and bookmaker",
                "Community comments and transparency flags"
            ),
            cons = listOf(
                "Top PRO tipsters can charge €50-€150/month",
                "Bookmakers quickly limit accounts following sharp tipsters",
                "Requires discipline to manage multiple tipster feeds"
            ),
            scamRiskLevel = RiskLevel.LOW_TRANSPARENT,
            bestMarkets = listOf("Asian Handicap", "Player Stats", "Niche League Totals"),
            howToTestFreeGuide = "Search for 'Active Free Tipsters' with >300 bets and positive yield. Add their picks to your daily test tracker to taste their current form.",
            auditSummary = "Highly respected platform for serious handicapping. A great testing ground because hundreds of experienced handicappers post free daily picks alongside paid ones.",
            primaryColorHex = 0xFF8B5CF6,
            sampleDailyPredictions = listOf(
                SampleTip("Lille vs Lens", "Ligue 1", "Draw (X)", 3.40, "1X2", "High parity derby match with elevated statistical draw frequency"),
                SampleTip("Bologna vs Fiorentina", "Serie A", "Both Teams to Score", 1.92, "BTTS", "Both defensive records showing structural vulnerability in away fixtures")
            )
        ),
        PredictionSite(
            id = "vitibet",
            name = "Vitibet",
            category = SiteCategory.MATHEMATICAL_MODEL,
            pricingModel = PricingModel.TOTALLY_FREE,
            monthlyCostUsd = 0.0,
            claimedWinRate = "Displays 'Tips of the Day' with high confidence",
            realisticVerifiedRate = "51.0% - 53.8%",
            verifiedYieldRoi = "-1.0% to +2.0%",
            predictionMethodology = "Mathematical indices calculated from home/away 6-match trends, goal difference, and head-to-head records.",
            algorithmType = "Predictive Rating Index (Table 1-2-X Scores)",
            pros = listOf(
                "Totally free with daily 'Tip of the Day' highlight list",
                "Includes hockey, basketball, and handball alongside football",
                "Easy-to-read prediction score table with index numbers"
            ),
            cons = listOf(
                "Index formulas are simple compared to modern machine learning models",
                "Does not track live odds changes or market market movements"
            ),
            scamRiskLevel = RiskLevel.LOW_TRANSPARENT,
            bestMarkets = listOf("1X2 Home Wins", "Tip of the Day"),
            howToTestFreeGuide = "Track strictly the top 3 'Tips of the Day' each weekend. Verify if the combined index score correlates with positive returns.",
            auditSummary = "Simple, lightweight free mathematical portal. Good for quick comparative analysis.",
            primaryColorHex = 0xFFEC4899,
            sampleDailyPredictions = listOf(
                SampleTip("Inter vs Juventus", "Serie A", "Inter Win (1)", 1.95, "1X2", "Index rating +24 favoring home dominance"),
                SampleTip("Bayern Munich vs Stuttgart", "Bundesliga", "Bayern Win & Over 2.5", 1.65, "Combo", "Index score projects 3-1 home outcome")
            )
        ),
        PredictionSite(
            id = "statarea",
            name = "Statarea",
            category = SiteCategory.FREE_STATS_PORTAL,
            pricingModel = PricingModel.TOTALLY_FREE,
            monthlyCostUsd = 0.0,
            claimedWinRate = "Aggregates community votes vs computer prediction",
            realisticVerifiedRate = "50.5% - 53.0%",
            verifiedYieldRoi = "-1.8% to +1.5%",
            predictionMethodology = "Dual analysis: computer statistical algorithm predictions side-by-side with crowd-sourced community consensus voting.",
            algorithmType = "Crowd Wisdom + Statistical Probability Engine",
            pros = listOf(
                "Interesting comparison between computer algorithm vs user community vote",
                "100% free daily games across all continents",
                "Highlights high-confidence percentage picks"
            ),
            cons = listOf(
                "Crowd voting is often emotionally biased toward popular teams (e.g. Man Utd, Barca)",
                "Site UI is dated and lacks advanced filtering"
            ),
            scamRiskLevel = RiskLevel.LOW_TRANSPARENT,
            bestMarkets = listOf("Double Chance (1X)", "Over 1.5 Goals"),
            howToTestFreeGuide = "Compare bets where the algorithm AND >75% of users agree vs bets where the algorithm contradicts the crowd. Log 25 of each in the tester!",
            auditSummary = "Great platform to study 'Wisdom of the Crowds' vs raw statistics. Reveals when public hype overprices a popular favorite.",
            primaryColorHex = 0xFFF97316,
            sampleDailyPredictions = listOf(
                SampleTip("Porto vs Sporting CP", "Primeira Liga", "1X Double Chance", 1.45, "Double Chance", "Algorithm gives 72% home safety margin"),
                SampleTip("Ajax vs AZ Alkmaar", "Eredivisie", "Over 2.5 Goals", 1.60, "Over/Under 2.5", "High offensive volume across previous 10 meetings")
            )
        )
    )

    // Pre-loaded realistic benchmark test data so the user can immediately see
    // the "Taste & Test" comparison table in action!
    private val initialSamplePredictions: List<BetPrediction> = listOf(
        BetPrediction(
            siteId = "forebet",
            siteName = "Forebet",
            matchTitle = "Arsenal vs Chelsea",
            league = "Premier League",
            predictionTip = "Arsenal Win (1)",
            marketType = "1X2",
            odds = 1.85,
            stakeUnits = 1.0,
            date = "2026-09-12",
            status = PredictionStatus.WON,
            isFreeDaily = true,
            notes = "Forebet gave 58% home win probability."
        ),
        BetPrediction(
            siteId = "forebet",
            siteName = "Forebet",
            matchTitle = "Dortmund vs Leverkusen",
            league = "Bundesliga",
            predictionTip = "Over 2.5 Goals",
            marketType = "Over/Under 2.5",
            odds = 1.68,
            stakeUnits = 1.0,
            date = "2026-09-12",
            status = PredictionStatus.WON,
            isFreeDaily = true,
            notes = "Finished 2-2. Clean hit."
        ),
        BetPrediction(
            siteId = "forebet",
            siteName = "Forebet",
            matchTitle = "Valencia vs Betis",
            league = "La Liga",
            predictionTip = "Under 2.5 Goals",
            marketType = "Over/Under 2.5",
            odds = 1.72,
            stakeUnits = 1.0,
            date = "2026-09-11",
            status = PredictionStatus.LOST,
            isFreeDaily = true,
            notes = "Late penalty resulted in 1-2 scoreline."
        ),
        BetPrediction(
            siteId = "predictz",
            siteName = "PredictZ",
            matchTitle = "Real Madrid vs Sevilla",
            league = "La Liga",
            predictionTip = "Both Teams To Score (Yes)",
            marketType = "BTTS",
            odds = 1.75,
            stakeUnits = 1.0,
            date = "2026-09-12",
            status = PredictionStatus.WON,
            isFreeDaily = true,
            notes = "Finished 3-1. Hit in 64th min."
        ),
        BetPrediction(
            siteId = "predictz",
            siteName = "PredictZ",
            matchTitle = "Atalanta vs Roma",
            league = "Serie A",
            predictionTip = "Over 2.5 Goals",
            marketType = "Over/Under 2.5",
            odds = 1.82,
            stakeUnits = 1.0,
            date = "2026-09-12",
            status = PredictionStatus.LOST,
            isFreeDaily = true,
            notes = "Finished 1-1, missed late chance."
        ),
        BetPrediction(
            siteId = "windrawwin",
            siteName = "Windrawwin",
            matchTitle = "Feyenoord vs Utrecht",
            league = "Eredivisie",
            predictionTip = "Over 2.5 Goals",
            marketType = "Over/Under 2.5",
            odds = 1.62,
            stakeUnits = 1.0,
            date = "2026-09-12",
            status = PredictionStatus.WON,
            isFreeDaily = true,
            notes = "High-tempo match hit 4 goals."
        ),
        BetPrediction(
            siteId = "betensured",
            siteName = "Betensured",
            matchTitle = "PSG vs Monaco",
            league = "Ligue 1",
            predictionTip = "PSG Win (1)",
            marketType = "1X2",
            odds = 1.55,
            stakeUnits = 1.0,
            date = "2026-09-12",
            status = PredictionStatus.WON,
            isFreeDaily = true,
            notes = "Free plan selection. Low odds."
        ),
        BetPrediction(
            siteId = "betensured",
            siteName = "Betensured",
            matchTitle = "Milan vs Napoli",
            league = "Serie A",
            predictionTip = "Double Chance 1X",
            marketType = "Double Chance",
            odds = 1.38,
            stakeUnits = 1.0,
            date = "2026-09-11",
            status = PredictionStatus.LOST,
            isFreeDaily = true,
            notes = "Napoli won 0-1 away."
        ),
        BetPrediction(
            siteId = "tipstrr",
            siteName = "Tipstrr",
            matchTitle = "Aston Villa vs Newcastle",
            league = "Premier League",
            predictionTip = "Newcastle +0.25 AH",
            marketType = "Asian Handicap",
            odds = 1.95,
            stakeUnits = 1.0,
            date = "2026-09-12",
            status = PredictionStatus.WON,
            isFreeDaily = true,
            notes = "Proofed free daily selection from top tipster."
        ),
        BetPrediction(
            siteId = "tipstrr",
            siteName = "Tipstrr",
            matchTitle = "Lille vs Lens",
            league = "Ligue 1",
            predictionTip = "Draw (X)",
            marketType = "1X2",
            odds = 3.35,
            stakeUnits = 1.0,
            date = "2026-09-13",
            status = PredictionStatus.PENDING,
            isFreeDaily = true,
            notes = "Active pending test pick."
        )
    )

    val todayAndTomorrowSportyBetPicks = listOf(
        SportyBetPick(
            id = "sb_1",
            match = "Arsenal vs Everton",
            homeTeam = "Arsenal",
            awayTeam = "Everton",
            league = "Premier League",
            country = "England",
            scheduleDay = "Today",
            kickOffTime = "17:30",
            tip = "Over 2.5 Goals",
            estimatedOdds = 1.72,
            sportyBetMarketName = "Over/Under Goals",
            sportyBetSelection = "Over 2.5",
            algorithmicSource = "Forebet Math (Poisson 2.85 Exp. Goals)",
            confidenceLevel = "High Statistical Edge",
            stepByStepSportyBetGuide = "1. Open SportyBet App or website.\n2. Tap 'Football' -> 'England' -> 'Premier League'.\n3. Select 'Arsenal vs Everton'.\n4. In the top market tab, scroll horizontally and tap 'Over/Under'.\n5. Look for 'Total Goals 2.5' and tap 'Over 2.5' (Odds ~1.72).\n6. Enter your stake in betslip and tap 'Place Bet'."
        ),
        SportyBetPick(
            id = "sb_2",
            match = "Real Madrid vs Mallorca",
            homeTeam = "Real Madrid",
            awayTeam = "Mallorca",
            league = "La Liga",
            country = "Spain",
            scheduleDay = "Today",
            kickOffTime = "20:00",
            tip = "Home Win (1) & Over 1.5 Goals",
            estimatedOdds = 1.55,
            sportyBetMarketName = "1X2 & Over/Under",
            sportyBetSelection = "Home & Over 1.5",
            algorithmicSource = "Windrawwin 72% Home Win Form",
            confidenceLevel = "Banker Single",
            stepByStepSportyBetGuide = "1. On SportyBet, tap 'Football' -> 'Spain' -> 'La Liga'.\n2. Open 'Real Madrid vs Mallorca'.\n3. Tap 'Combo' or '1X2 & O/U 1.5' tab.\n4. Select 'Home & Over 1.5' (or standard 1X2 '1' Home Win @ 1.32).\n5. Add to betslip."
        ),
        SportyBetPick(
            id = "sb_3",
            match = "Bayern Munich vs Borussia Dortmund",
            homeTeam = "Bayern Munich",
            awayTeam = "Borussia Dortmund",
            league = "Bundesliga",
            country = "Germany",
            scheduleDay = "Today",
            kickOffTime = "18:30",
            tip = "Both Teams To Score (GG)",
            estimatedOdds = 1.50,
            sportyBetMarketName = "GG / Both Teams To Score",
            sportyBetSelection = "Yes (GG)",
            algorithmicSource = "PredictZ 85% BTTS Trend Rating",
            confidenceLevel = "High Probability",
            stepByStepSportyBetGuide = "1. On SportyBet, navigate to 'Football' -> 'Germany' -> 'Bundesliga'.\n2. Select 'Bayern Munich vs Borussia Dortmund'.\n3. Tap the 'GG/NG' or 'Both Teams To Score' market tab.\n4. Tap 'Yes' (labeled 'GG' on SportyBet).\n5. Confirm odds in your betslip."
        ),
        SportyBetPick(
            id = "sb_4",
            match = "Inter Milan vs Torino",
            homeTeam = "Inter Milan",
            awayTeam = "Torino",
            league = "Serie A",
            country = "Italy",
            scheduleDay = "Tomorrow",
            kickOffTime = "19:45",
            tip = "Home Win (1)",
            estimatedOdds = 1.48,
            sportyBetMarketName = "1X2 (Match Result)",
            sportyBetSelection = "1 (Inter Win)",
            algorithmicSource = "Vitibet Index Score 3.8",
            confidenceLevel = "High Confidence",
            stepByStepSportyBetGuide = "1. On SportyBet, click 'Football' -> 'Italy' -> 'Serie A'.\n2. Find 'Inter vs Torino' under tomorrow's matches.\n3. On the main '1X2' columns on the match row, tap '1' (Home Win).\n4. Your betslip will automatically register Inter as the winner."
        ),
        SportyBetPick(
            id = "sb_5",
            match = "PSG vs Lyon",
            homeTeam = "Paris SG",
            awayTeam = "Lyon",
            league = "Ligue 1",
            country = "France",
            scheduleDay = "Tomorrow",
            kickOffTime = "20:00",
            tip = "Both Teams To Score (Yes) or Over 2.5",
            estimatedOdds = 1.65,
            sportyBetMarketName = "Both Teams To Score",
            sportyBetSelection = "Yes (GG)",
            algorithmicSource = "Statarea Goal Expectancy Index",
            confidenceLevel = "Value Selection",
            stepByStepSportyBetGuide = "1. On SportyBet, tap 'Football' -> 'France' -> 'Ligue 1'.\n2. Open 'Paris Saint-Germain vs Olympique Lyonnais'.\n3. Tap 'GG/NG' tab and select 'Yes'.\n4. Alternatively, select 'Over 2.5 Goals' in the 'Over/Under' tab."
        ),
        SportyBetPick(
            id = "sb_6",
            match = "Sporting CP vs Braga",
            homeTeam = "Sporting CP",
            awayTeam = "Braga",
            league = "Primeira Liga",
            country = "Portugal",
            scheduleDay = "Tomorrow",
            kickOffTime = "20:30",
            tip = "Double Chance 1X & Over 1.5",
            estimatedOdds = 1.42,
            sportyBetMarketName = "Double Chance & Over/Under",
            sportyBetSelection = "1X & Over 1.5",
            algorithmicSource = "Forebet Math Form (Unbeaten 14 Home matches)",
            confidenceLevel = "Safe Double Chance",
            stepByStepSportyBetGuide = "1. On SportyBet, select 'Football' -> 'Portugal' -> 'Primeira Liga'.\n2. Open 'Sporting vs Braga'.\n3. If you want pure safety, tap 'Double Chance' and select '1X'.\n4. For higher odds, choose 'Combo: 1X & Over 1.5'."
        )
    )
}
