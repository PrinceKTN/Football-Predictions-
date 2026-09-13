package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.data.model.PredictionSite
import com.example.data.model.SampleTip
import com.example.data.model.SiteCategory
import com.example.ui.PredictionViewModel
import com.example.ui.components.PricingBadge
import com.example.ui.components.RiskBadge
import com.example.ui.components.SnippetCredibilityAuditorCard
import com.example.ui.components.SportyBetPicksSection
import com.example.ui.theme.AmberVIP
import com.example.ui.theme.CrimsonLoss
import com.example.ui.theme.CyanOdds
import com.example.ui.theme.EmeraldWin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SitesDirectoryScreen(
    viewModel: PredictionViewModel,
    onNavigateToTesting: () -> Unit,
    onNavigateToThinking: () -> Unit,
    onNavigateToLiveSearch: (String) -> Unit
) {
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var expandedSiteId by remember { mutableStateOf<String?>(null) }

    val filteredSites = remember(viewModel.sitesDirectory, categoryFilter, searchQuery) {
        viewModel.sitesDirectory.filter { site ->
            val matchesCategory = categoryFilter == null || site.category == categoryFilter
            val matchesSearch = searchQuery.isBlank() ||
                    site.name.contains(searchQuery, ignoreCase = true) ||
                    site.algorithmType.contains(searchQuery, ignoreCase = true) ||
                    site.bestMarkets.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sites_directory_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldWin.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmeraldWin,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Prediction Platforms Audit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Free Daily Tips vs Paid VIP Reality",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Most paid subscription sites market false 85%+ win rates using extreme favorites (1.15-1.30 odds), which mathematically leads to long-term ruin. Compare verified algorithmic models, detect subscription traps, and taste free daily games before risking any money.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    FilledTonalButton(
                        onClick = onNavigateToTesting,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View 30-Day Historical Accuracy Chart", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Snippet Credibility Auditor (Gemini API)
        item {
            SnippetCredibilityAuditorCard(viewModel = viewModel)
        }

        // Curated Today & Tomorrow Best Picks for SportyBet
        item {
            SportyBetPicksSection(viewModel = viewModel)
        }

        // Search and filter chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search sites (e.g. Forebet, PredictZ, VIP, BTTS...)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_sites_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = categoryFilter == null,
                            onClick = { viewModel.setCategoryFilter(null) },
                            label = { Text("All Sites (${viewModel.sitesDirectory.size})") }
                        )
                    }
                    items(SiteCategory.values()) { cat ->
                        FilterChip(
                            selected = categoryFilter == cat,
                            onClick = {
                                viewModel.setCategoryFilter(if (categoryFilter == cat) null else cat)
                            },
                            label = { Text(cat.title) }
                        )
                    }
                }
            }
        }

        // Site Cards
        items(filteredSites, key = { it.id }) { site ->
            val isExpanded = expandedSiteId == site.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("site_card_${site.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Site Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = site.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = site.algorithmType,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        IconButton(onClick = {
                            expandedSiteId = if (isExpanded) null else site.id
                        }) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Badges Row
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PricingBadge(model = site.pricingModel)
                        RiskBadge(level = site.scamRiskLevel)
                        if (site.monthlyCostUsd > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AmberVIP.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$${site.monthlyCostUsd.toInt()}/mo",
                                    color = AmberVIP,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Comparison Metrics Box (Claimed vs Reality)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Claimed Rate",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        site.claimedWinRate,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Verified Reality",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        site.realisticVerifiedRate,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldWin
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Verified Yield (ROI)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        site.verifiedYieldRoi,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CyanOdds
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Top Value Markets",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        site.bestMarkets.joinToString(", "),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = site.auditSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )

                    // Expanded deep details
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider()

                            // Pros & Cons
                            Text("Strengths & Benefits", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = EmeraldWin)
                            site.pros.forEach { pro ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldWin, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(pro, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            Text("Pitfalls & Weaknesses", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = CrimsonLoss)
                            site.cons.forEach { con ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonLoss, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(con, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            // Free Testing Guide
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyanOdds.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = CyanOdds, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("How To Taste & Test For Free", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CyanOdds)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(site.howToTestFreeGuide, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            // Sample Daily Tips
                            if (site.sampleDailyPredictions.isNotEmpty()) {
                                Text("Active Sample Picks to Taste:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                site.sampleDailyPredictions.forEach { sampleTip ->
                                    SampleTipRow(
                                        tip = sampleTip,
                                        onAddToTest = {
                                            viewModel.addFromSampleTip(site, sampleTip)
                                            onNavigateToTesting()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.auditSiteWithHighThinking(site)
                                onNavigateToThinking()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_audit_site_${site.id}"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Deep AI Audit", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                onNavigateToLiveSearch("${site.name} today free betting predictions odds")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Live Intel", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SampleTipRow(
    tip: SampleTip,
    onAddToTest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tip.match, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${tip.tip} @ ${tip.odds} • ${tip.market}", color = CyanOdds, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Text(tip.rationale, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            FilledTonalButton(
                onClick = onAddToTest,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Taste Pick", fontSize = 11.sp)
            }
        }
    }
}
