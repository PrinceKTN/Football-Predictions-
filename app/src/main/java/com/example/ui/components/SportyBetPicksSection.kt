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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

    val todayCount = viewModel.sportyBetPicks.count { it.scheduleDay == "Today" }
    val tomorrowCount = viewModel.sportyBetPicks.count { it.scheduleDay == "Tomorrow" }

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
        border = BorderStroke(1.5.dp, EmeraldWin.copy(alpha = 0.6f))
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EmeraldWin.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SportsSoccer,
                            contentDescription = null,
                            tint = EmeraldWin,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Best Picks (SportyBet Target)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Audited picks with exact SportyBet inputs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { showMarketCheatSheet = true }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "SportyBet Cheat Sheet",
                        tint = CyanOdds
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Easily differentiate matches playing Today vs advance value picks for Tomorrow. Tap any match to view the exact menu navigation path on SportyBet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // FCM Push Notifications Quick Status Banner
            val isPushActive by viewModel.isPushNotificationsEnabled.collectAsState()
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (isPushActive) EmeraldWin.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth().testTag("fcm_picks_status_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isPushActive) EmeraldWin.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (isPushActive) EmeraldWin else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isPushActive) "FCM Best Pick Alerts Active" else "FCM Alerts Paused",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Topic: #best_picks • Push on new post or update",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.sendTestBestPickAlert(isUpdate = false)
                            Toast.makeText(context, "🔥 Pushed Best Pick alert via FCM!", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyanOdds),
                        modifier = Modifier.testTag("test_fcm_banner_button")
                    ) {
                        Text("Test Alert", fontSize = 11.sp, color = CyanOdds, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // REFACTORED: Large, Distinctive Day Filter Buttons (Today vs Tomorrow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "TODAY" Button
                val isTodayActive = dayFilter == "Today"
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.sportyBetDayFilter.value = "Today" }
                        .testTag("filter_today_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTodayActive) EmeraldWin.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(
                        if (isTodayActive) 2.dp else 1.dp,
                        if (isTodayActive) EmeraldWin else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isTodayActive) EmeraldWin else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (isTodayActive) EmeraldWin else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Today ($todayCount)",
                            fontWeight = if (isTodayActive) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isTodayActive) EmeraldWin else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // "TOMORROW" Button
                val isTomorrowActive = dayFilter == "Tomorrow"
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.sportyBetDayFilter.value = "Tomorrow" }
                        .testTag("filter_tomorrow_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTomorrowActive) CyanOdds.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(
                        if (isTomorrowActive) 2.dp else 1.dp,
                        if (isTomorrowActive) CyanOdds else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            tint = if (isTomorrowActive) CyanOdds else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tomorrow ($tomorrowCount)",
                            fontWeight = if (isTomorrowActive) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isTomorrowActive) CyanOdds else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // "ALL" Button
                val isAllActive = dayFilter == "All"
                Card(
                    modifier = Modifier
                        .clickable { viewModel.sportyBetDayFilter.value = "All" }
                        .testTag("filter_all_days_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAllActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(
                        if (isAllActive) 1.5.dp else 1.dp,
                        if (isAllActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All",
                            fontWeight = if (isAllActive) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (isAllActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-banner describing active day
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (dayFilter) {
                            "Today" -> EmeraldWin.copy(alpha = 0.12f)
                            "Tomorrow" -> CyanOdds.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (dayFilter) {
                            "Today" -> Icons.Default.Bolt
                            "Tomorrow" -> Icons.Default.Event
                            else -> Icons.Default.Schedule
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = when (dayFilter) {
                            "Today" -> EmeraldWin
                            "Tomorrow" -> CyanOdds
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (dayFilter) {
                            "Today" -> "Showing $todayCount games for TODAY. Enter them into SportyBet now before kickoff."
                            "Tomorrow" -> "Showing $tomorrowCount advance games for TOMORROW. Good for early high-odds value."
                            else -> "Showing all $todayCount Today and $tomorrowCount Tomorrow picks side by side."
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of Picks
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                filteredPicks.forEach { pick ->
                    SportyBetPickItemCard(
                        pick = pick,
                        onAddToLedger = {
                            viewModel.addSportyBetPickToTestingLedger(pick)
                            Toast.makeText(context, "Added '${pick.match}' (${pick.scheduleDay}) to Testing Ledger!", Toast.LENGTH_SHORT).show()
                        },
                        onOpenGuide = { selectedPickForGuide = pick },
                        onCopy = {
                            val copyText = "Day: ${pick.scheduleDay.uppercase(Locale.ROOT)}\nMatch: ${pick.match}\nLeague: ${pick.league}\nSportyBet Market: ${pick.sportyBetMarketName}\nSelection: ${pick.sportyBetSelection}\nOdds: ${pick.estimatedOdds}"
                            clipboardManager.setText(AnnotatedString(copyText))
                            Toast.makeText(context, "Copied SportyBet pick to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        onPushAlert = {
                            viewModel.triggerBestPickAlert(pick, isUpdate = false)
                            Toast.makeText(context, "🔥 Sent Best Pick FCM Alert for ${pick.match}!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // SportyBet Step-by-Step Guide Dialog
    selectedPickForGuide?.let { pick ->
        val isToday = pick.scheduleDay == "Today"
        AlertDialog(
            onDismissRequest = { selectedPickForGuide = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isToday) Icons.Default.Bolt else Icons.Default.Event,
                        contentDescription = null,
                        tint = if (isToday) EmeraldWin else CyanOdds
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Input on SportyBet (${pick.scheduleDay})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isToday) EmeraldWin.copy(alpha = 0.2f) else CyanOdds.copy(alpha = 0.2f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isToday) "⚡ PLAY TODAY" else "📅 TOMORROW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isToday) EmeraldWin else CyanOdds
                            )
                        }

                        Text(
                            text = "Kickoff: ${pick.kickOffTime}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text("Match: ${pick.match}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("League: ${pick.country} - ${pick.league}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isToday) EmeraldWin.copy(alpha = 0.15f) else CyanOdds.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isToday) EmeraldWin.copy(alpha = 0.5f) else CyanOdds.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("SportyBet Market: ${pick.sportyBetMarketName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Tap Selection: ${pick.sportyBetSelection}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isToday) EmeraldWin else CyanOdds)
                            Text("Estimated Odds: ${pick.estimatedOdds}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Navigation steps on SportyBet:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = if (isToday) EmeraldWin else CyanOdds)
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
                    Icon(Icons.Default.Info, contentDescription = null, tint = CyanOdds)
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
    onCopy: () -> Unit,
    onPushAlert: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val isToday = pick.scheduleDay == "Today"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pick_card_${pick.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) EmeraldWin.copy(alpha = 0.07f) else CyanOdds.copy(alpha = 0.05f)
        ),
        border = BorderStroke(
            if (isToday) 2.dp else 1.8.dp,
            if (isToday) EmeraldWin.copy(alpha = 0.8f) else CyanOdds.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // HIGH-VISIBILITY DAY BADGE BANNER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isToday) EmeraldWin.copy(alpha = 0.2f) else CyanOdds.copy(alpha = 0.18f)
                    )
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isToday) EmeraldWin else CyanOdds)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isToday) Icons.Default.Bolt else Icons.Default.Event,
                        contentDescription = null,
                        tint = if (isToday) EmeraldWin else CyanOdds,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isToday) "● PLAY TODAY" else "📅 TOMORROW'S GAME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isToday) EmeraldWin else CyanOdds
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${pick.scheduleDay} @ ${pick.kickOffTime}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Match Title & League
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pick.match,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${pick.country} • ${pick.league}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SportyBet Specific Translation Box
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, (if (isToday) EmeraldWin else CyanOdds).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isToday) EmeraldWin else CyanOdds
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                (if (isToday) EmeraldWin else CyanOdds).copy(alpha = 0.18f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "@ ${pick.estimatedOdds}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = if (isToday) EmeraldWin else CyanOdds
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
                    color = if (isToday) EmeraldWin else CyanOdds
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
                    Text(
                        text = "Step-by-step SportyBet Guide (${pick.scheduleDay}):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isToday) EmeraldWin else CyanOdds
                    )
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isToday) EmeraldWin else CyanOdds
                    ),
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

                IconButton(
                    onClick = onPushAlert,
                    modifier = Modifier.size(34.dp).testTag("alert_pick_button_${pick.id}")
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = "Send FCM Alert for Pick",
                        tint = if (isToday) EmeraldWin else CyanOdds,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
