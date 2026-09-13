package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SportyBetPick
import com.example.ui.PredictionViewModel
import com.example.ui.theme.AmberVIP
import com.example.ui.theme.CyanOdds
import com.example.ui.theme.EmeraldWin
import java.util.Locale

@Composable
fun SportyBetPicksSection(
    viewModel: PredictionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val dayFilter by viewModel.sportyBetDayFilter.collectAsState()
    var selectedPickForGuide by remember { mutableStateOf<SportyBetPick?>(null) }
    var showMarketCheatSheet by remember { mutableStateOf(false) }

    val filteredPicks = viewModel.sportyBetPicks.filter { pick ->
        when (dayFilter) {
            "Today" -> pick.scheduleDay == "Today"
            "Tomorrow" -> pick.scheduleDay == "Tomorrow"
            else -> true
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sportybet_picks_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, EmeraldWin.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmeraldWin.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SportsSoccer,
                            contentDescription = null,
                            tint = EmeraldWin,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Best Picks (Today & Tomorrow)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Curated algorithmic picks ready for SportyBet entry",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { showMarketCheatSheet = true }) {
                    Icon(
                        Icons.Default.HelpOutline,
                        contentDescription = "SportyBet Cheat Sheet",
                        tint = CyanOdds
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle description
            Text(
                text = "These matches are calculated using Poisson goal expectancy and Poisson distribution models. Below each game is the exact SportyBet market name and selection so you can input them manually without confusion.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips (All, Today, Tomorrow) + Cheat Sheet button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("All", "Today", "Tomorrow")) { option ->
                        FilterChip(
                            selected = dayFilter == option,
                            onClick = { viewModel.sportyBetDayFilter.value = option },
                            label = { Text(option, fontSize = 12.sp) }
                        )
                    }
                }

                TextButton(
                    onClick = { showMarketCheatSheet = true },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyanOdds)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SportyBet Cheat Sheet", fontSize = 11.sp, color = CyanOdds, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // List of Picks
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredPicks.forEach { pick ->
                    SportyBetPickItemCard(
                        pick = pick,
                        onAddToLedger = {
                            viewModel.addSportyBetPickToTestingLedger(pick)
                            Toast.makeText(context, "Added '${pick.match}' to your Testing Ledger!", Toast.LENGTH_SHORT).show()
                        },
                        onOpenGuide = { selectedPickForGuide = pick },
                        onCopy = {
                            val copyText = "Match: ${pick.match}\nLeague: ${pick.league}\nSportyBet Market: ${pick.sportyBetMarketName}\nSelection: ${pick.sportyBetSelection}\nOdds: ${pick.estimatedOdds}"
                            clipboardManager.setText(AnnotatedString(copyText))
                            Toast.makeText(context, "Copied SportyBet pick to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // SportyBet Step-by-Step Guide Dialog
    selectedPickForGuide?.let { pick ->
        AlertDialog(
            onDismissRequest = { selectedPickForGuide = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = EmeraldWin)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("How to input on SportyBet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Match: ${pick.match}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("League: ${pick.country} - ${pick.league}", fontSize = 13.sp)
                    Text("Schedule: ${pick.scheduleDay} @ ${pick.kickOffTime}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("SportyBet Market: ${pick.sportyBetMarketName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Tap Selection: ${pick.sportyBetSelection}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldWin)
                            Text("Estimated Odds: ${pick.estimatedOdds}", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Step-by-step Navigation:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = pick.stepByStepSportyBetGuide,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addSportyBetPickToTestingLedger(pick)
                        Toast.makeText(context, "Added to Testing Ledger!", Toast.LENGTH_SHORT).show()
                        selectedPickForGuide = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldWin)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to Ledger & Close")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPickForGuide = null }) {
                    Text("Close")
                }
            }
        )
    }

    // SportyBet Market Cheat Sheet Dialog
    if (showMarketCheatSheet) {
        AlertDialog(
            onDismissRequest = { showMarketCheatSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = CyanOdds)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SportyBet Market Cheat Sheet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "How SportyBet markets work and where to find them:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val cheatSheetItems = listOf(
                        "1X2 (Match Result)" to "1 = Home team wins. X = Match ends in a Draw. 2 = Away team wins.",
                        "Over/Under Goals" to "Over 2.5 means 3 or more total goals (e.g. 2-1, 3-0). Under 2.5 means 2 or fewer goals (e.g. 0-0, 1-0, 1-1).",
                        "GG / NG (Both Teams To Score)" to "GG (Yes) = Both teams score at least 1 goal. NG (No) = At least one team keeps a clean sheet (0-0, 1-0, 0-2).",
                        "Double Chance (1X, 12, X2)" to "1X = Home team wins or draws. X2 = Away team wins or draws. 12 = Either team wins (no draw).",
                        "DNB (Draw No Bet)" to "You pick Home or Away. If match ends in a draw, your money is refunded (odds void).",
                        "Handicap (+1, -1)" to "Gives an underdog a goal lead or favorites a goal deficit before kickoff."
                    )

                    cheatSheetItems.forEach { (market, explanation) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(market, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CyanOdds)
                            Text(explanation, fontSize = 11.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showMarketCheatSheet = false }) {
                    Text("Got It")
                }
            }
        )
    }
}

@Composable
fun SportyBetPickItemCard(
    pick: SportyBetPick,
    onAddToLedger: () -> Unit,
    onOpenGuide: () -> Unit,
    onCopy: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Day badge + League + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (pick.scheduleDay == "Today") EmeraldWin.copy(alpha = 0.2f) else AmberVIP.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = pick.scheduleDay.uppercase(Locale.ROOT),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pick.scheduleDay == "Today") EmeraldWin else AmberVIP
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${pick.country} • ${pick.league}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = pick.kickOffTime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Match Title
            Text(
                text = pick.match,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // SportyBet Specific Translation Box
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CyanOdds.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SportyBet Market: ${pick.sportyBetMarketName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Select: ",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = pick.sportyBetSelection,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldWin
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(CyanOdds.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "@ ${pick.estimatedOdds}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CyanOdds
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Model Source & Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Model: ${pick.algorithmicSource}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = pick.confidenceLevel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldWin
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expandable Step-by-Step preview
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text("How to input on SportyBet:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CyanOdds)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pick.stepByStepSportyBetGuide,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (expanded) "Hide Guide" else "How to Bet", fontSize = 11.sp)
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Button(
                    onClick = onAddToLedger,
                    modifier = Modifier.weight(1.3f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldWin),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to Ledger", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Pick", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
