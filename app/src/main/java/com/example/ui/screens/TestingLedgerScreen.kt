package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BetPrediction
import com.example.data.model.PredictionStatus
import com.example.data.model.SiteStats
import com.example.ui.PredictionViewModel
import com.example.ui.components.AddPredictionDialog
import com.example.ui.components.HistoricalAccuracyChartCard
import com.example.ui.components.MatchOutcomeResolutionBar
import com.example.ui.theme.AmberVIP
import com.example.ui.theme.CrimsonLoss
import com.example.ui.theme.CyanOdds
import com.example.ui.theme.EmeraldWin
import java.util.Locale

@Composable
fun TestingLedgerScreen(
    viewModel: PredictionViewModel
) {
    val predictions by viewModel.allPredictions.collectAsState()
    val siteStats by viewModel.siteStats.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val siteFilter by viewModel.siteFilter.collectAsState()

    val quickTipResult by viewModel.quickTipResult.collectAsState()
    val isQuickTipLoading by viewModel.isQuickTipLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var tipEvaluationDialogPrediction by remember { mutableStateOf<BetPrediction?>(null) }
    var showBenchmarkTable by remember { mutableStateOf(true) }

    val filteredPredictions = remember(predictions, statusFilter, siteFilter) {
        predictions.filter { p ->
            val matchesStatus = statusFilter == null || p.status == statusFilter
            val matchesSite = siteFilter == null || p.siteName.equals(siteFilter, ignoreCase = true)
            matchesStatus && matchesSite
        }
    }

    val pendingCount = remember(predictions) { predictions.count { it.status == PredictionStatus.PENDING } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_prediction")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Log Prediction")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Daily Pick", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("testing_ledger_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall KPI Header
            item {
                OverallPerformanceCard(predictions = predictions)
            }

            // 30-Day Historical Accuracy Chart (Native Compose Canvas)
            item {
                HistoricalAccuracyChartCard(viewModel = viewModel)
            }

            // Site Comparison / Benchmarking Section ("Taste Which Site Delivers the Best")
            item {
                SiteComparisonCard(
                    stats = siteStats,
                    isExpanded = showBenchmarkTable,
                    onToggle = { showBenchmarkTable = !showBenchmarkTable },
                    onSelectSiteFilter = { selectedSite ->
                        viewModel.setSiteFilter(if (siteFilter == selectedSite) null else selectedSite)
                    },
                    selectedFilter = siteFilter
                )
            }

            // Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tracked Predictions (${filteredPredictions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Status filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = statusFilter == null,
                                onClick = { viewModel.setStatusFilter(null) },
                                label = { Text("All Statuses") }
                            )
                        }
                        items(PredictionStatus.values()) { status ->
                            FilterChip(
                                selected = statusFilter == status,
                                onClick = {
                                    viewModel.setStatusFilter(if (statusFilter == status) null else status)
                                },
                                label = { Text(status.label) }
                            )
                        }
                    }

                    if (siteFilter != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Filtering by: $siteFilter", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { viewModel.setSiteFilter(null) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear site filter")
                            }
                        }
                    }
                }
            }

            // Quick settlement reminder for games awaiting outcome
            if (pendingCount > 0) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyanOdds.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pending_outcomes_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = CyanOdds,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "$pendingCount Game${if (pendingCount > 1) "s" else ""} Awaiting Match Outcome",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Mark 'Won' or 'Lost' on each below to build your local accuracy stats.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (statusFilter != PredictionStatus.PENDING) {
                                TextButton(
                                    onClick = { viewModel.setStatusFilter(PredictionStatus.PENDING) },
                                    modifier = Modifier.testTag("filter_pending_button")
                                ) {
                                    Text("Show (${pendingCount})", fontSize = 11.sp, color = CyanOdds, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Predictions List
            if (filteredPredictions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No predictions match your filter",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Use the '+ Log Daily Pick' button or visit the Sites Audit screen to taste free picks from Forebet, PredictZ, etc.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            } else {
                items(filteredPredictions, key = { it.id }) { prediction ->
                    PredictionItemCard(
                        prediction = prediction,
                        onStatusChange = { newStatus ->
                            viewModel.updateStatus(prediction, newStatus)
                        },
                        onDelete = {
                            viewModel.deletePrediction(prediction)
                        },
                        onAiEvaluate = {
                            tipEvaluationDialogPrediction = prediction
                            viewModel.quickEvaluateTip(prediction)
                        }
                    )
                }
            }

            // Bottom space for FAB
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Add Prediction Dialog
    if (showAddDialog) {
        AddPredictionDialog(
            availableSites = viewModel.sitesDirectory,
            onDismiss = { showAddDialog = false },
            onConfirm = { siteId, siteName, match, league, tip, market, odds, stake, notes, isFree ->
                viewModel.addPrediction(
                    siteId = siteId,
                    siteName = siteName,
                    matchTitle = match,
                    league = league,
                    predictionTip = tip,
                    marketType = market,
                    odds = odds,
                    stakeUnits = stake,
                    notes = notes,
                    isFreeDaily = isFree
                )
                showAddDialog = false
            }
        )
    }

    // Quick AI Evaluation Dialog
    if (tipEvaluationDialogPrediction != null) {
        val activePick = tipEvaluationDialogPrediction!!
        Dialog(onDismissRequest = { tipEvaluationDialogPrediction = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanOdds)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fast AI Value Analysis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = { tipEvaluationDialogPrediction = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${activePick.matchTitle} (${activePick.league})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Tip: ${activePick.predictionTip} @ Odds ${activePick.odds} [Source: ${activePick.siteName}]",
                        color = CyanOdds,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    if (isQuickTipLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Assessing implied probability and market value...", fontSize = 12.sp)
                        }
                    } else if (quickTipResult != null) {
                        Text(
                            text = quickTipResult!!.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { tipEvaluationDialogPrediction = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
fun OverallPerformanceCard(predictions: List<BetPrediction>) {
    val settled = predictions.filter { it.status == PredictionStatus.WON || it.status == PredictionStatus.LOST }
    val won = predictions.count { it.status == PredictionStatus.WON }
    val winRate = if (settled.isNotEmpty()) (won.toDouble() / settled.size) * 100.0 else 0.0

    var profit = 0.0
    var risked = 0.0
    predictions.forEach { p ->
        when (p.status) {
            PredictionStatus.WON -> {
                profit += (p.odds - 1.0) * p.stakeUnits
                risked += p.stakeUnits
            }
            PredictionStatus.LOST -> {
                profit -= p.stakeUnits
                risked += p.stakeUnits
            }
            else -> {}
        }
    }

    val roi = if (risked > 0.0) (profit / risked) * 100.0 else 0.0
    val isProfitable = profit >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Prediction Tasting Ledger",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Paper test results across all sites",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isProfitable) EmeraldWin.copy(alpha = 0.15f) else CrimsonLoss.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isProfitable) "+${String.format(Locale.US, "%.1f", roi)}% ROI" else "${String.format(Locale.US, "%.1f", roi)}% ROI",
                        color = if (isProfitable) EmeraldWin else CrimsonLoss,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(title = "Total Logged", value = "${predictions.size}")
                MetricColumn(title = "Win Rate", value = "${String.format(Locale.US, "%.1f", winRate)}%", highlightColor = if (winRate >= 52.4) EmeraldWin else MaterialTheme.colorScheme.onSurface)
                MetricColumn(title = "Net P/L", value = (if (profit >= 0) "+" else "") + String.format(Locale.US, "%.2f", profit) + " u", highlightColor = if (profit >= 0) EmeraldWin else CrimsonLoss)
                MetricColumn(title = "Pending", value = "${predictions.count { it.status == PredictionStatus.PENDING }}")
            }
        }
    }
}

@Composable
fun MetricColumn(title: String, value: String, highlightColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = highlightColor)
    }
}

@Composable
fun SiteComparisonCard(
    stats: List<SiteStats>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onSelectSiteFilter: (String) -> Unit,
    selectedFilter: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Leaderboard, contentDescription = null, tint = CyanOdds)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Site Comparison Benchmark",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "Which site actually delivers the best results?",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = onToggle,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(if (isExpanded) "Hide" else "Show Rankings", fontSize = 11.sp)
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (stats.isEmpty()) {
                        Text("No bets recorded yet. Add tips to rank sites!", fontSize = 12.sp)
                    } else {
                        stats.forEach { siteStat ->
                            val isSelected = selectedFilter.equals(siteStat.siteName, ignoreCase = true)
                            val profitSign = if (siteStat.netProfitUnits >= 0) "+" else ""

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectSiteFilter(siteStat.siteName) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = if (isSelected) BorderStroke(1.dp, EmeraldWin) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text(siteStat.siteName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            "${siteStat.wonBets}W - ${siteStat.lostBets}L (${String.format(Locale.US, "%.1f", siteStat.winRatePercentage)}%)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            siteStat.sampleSizeRating,
                                            fontSize = 10.sp,
                                            color = if (siteStat.totalBets < 30) AmberVIP else EmeraldWin
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                        Text(
                                            "$profitSign${String.format(Locale.US, "%.2f", siteStat.netProfitUnits)} u",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (siteStat.netProfitUnits >= 0) EmeraldWin else CrimsonLoss
                                        )
                                        Text(
                                            "${String.format(Locale.US, "%.1f", siteStat.roiPercentage)}% ROI",
                                            fontSize = 11.sp,
                                            color = CyanOdds
                                        )
                                        Text(
                                            siteStat.tasteVerdict,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (siteStat.netProfitUnits >= 0) EmeraldWin else CrimsonLoss
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionItemCard(
    prediction: BetPrediction,
    onStatusChange: (PredictionStatus) -> Unit,
    onDelete: () -> Unit,
    onAiEvaluate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prediction_card_${prediction.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            when (prediction.status) {
                PredictionStatus.WON -> EmeraldWin.copy(alpha = 0.5f)
                PredictionStatus.LOST -> CrimsonLoss.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top row: Site Badge & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanOdds.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(prediction.siteName, color = CyanOdds, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    if (prediction.isFreeDaily) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldWin.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("Free Daily", color = EmeraldWin, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    val isTodayGame = prediction.siteName.contains("Today", ignoreCase = true) || prediction.notes.contains("Today", ignoreCase = true)
                    val isTomorrowGame = prediction.siteName.contains("Tomorrow", ignoreCase = true) || prediction.notes.contains("Tomorrow", ignoreCase = true)

                    if (isTodayGame) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldWin.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("⚡ TODAY", color = EmeraldWin, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    } else if (isTomorrowGame) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyanOdds.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("📅 TOMORROW", color = CyanOdds, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Text(prediction.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Match Title & League
            Text(
                text = prediction.matchTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${prediction.league} • Market: ${prediction.marketType}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tip and Odds box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Pick / Selection", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(prediction.predictionTip, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Odds: @${String.format(Locale.US, "%.2f", prediction.odds)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CyanOdds)
                        Text("${prediction.stakeUnits} Unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (prediction.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Rationale: ${prediction.notes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            // Post-Match Outcome Resolution Component (Won / Lost / Push)
            MatchOutcomeResolutionBar(
                prediction = prediction,
                onStatusChange = onStatusChange
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Actions: AI Evaluate & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAiEvaluate,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyanOdds.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("ai_eval_button_${prediction.id}")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanOdds, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Audit", fontSize = 11.sp, color = CyanOdds)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_prediction_button_${prediction.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete from Ledger", tint = CrimsonLoss, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
