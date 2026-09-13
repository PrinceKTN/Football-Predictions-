package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.PredictionSite

@Composable
fun AddPredictionDialog(
    availableSites: List<PredictionSite>,
    preselectedSite: PredictionSite? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        siteId: String,
        siteName: String,
        matchTitle: String,
        league: String,
        predictionTip: String,
        marketType: String,
        odds: Double,
        stakeUnits: Double,
        notes: String,
        isFreeDaily: Boolean
    ) -> Unit
) {
    var selectedSite by remember { mutableStateOf(preselectedSite ?: availableSites.firstOrNull()) }
    var customSiteName by remember { mutableStateOf("") }
    var matchTitle by remember { mutableStateOf("") }
    var league by remember { mutableStateOf("") }
    var predictionTip by remember { mutableStateOf("") }
    var marketType by remember { mutableStateOf("1X2") }
    var oddsText by remember { mutableStateOf("1.85") }
    var stakeText by remember { mutableStateOf("1.0") }
    var notes by remember { mutableStateOf("") }
    var isFreeDaily by remember { mutableStateOf(true) }

    var siteDropdownExpanded by remember { mutableStateOf(false) }
    var marketDropdownExpanded by remember { mutableStateOf(false) }

    val markets = listOf("1X2", "Over/Under 2.5", "Over/Under 1.5", "BTTS", "Double Chance", "Asian Handicap", "Correct Score", "Draw No Bet")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Log Daily Prediction for Testing", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Track this prediction to test whether this site delivers positive yield over time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Site Selector
                Text("Prediction Source / Site", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = selectedSite?.name ?: customSiteName.ifBlank { "Select Site" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select site",
                            modifier = Modifier.clickable { siteDropdownExpanded = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { siteDropdownExpanded = true }
                        .testTag("site_select_field")
                )

                DropdownMenu(
                    expanded = siteDropdownExpanded,
                    onDismissRequest = { siteDropdownExpanded = false }
                ) {
                    availableSites.forEach { site ->
                        DropdownMenuItem(
                            text = { Text("${site.name} (${site.pricingModel.label})") },
                            onClick = {
                                selectedSite = site
                                siteDropdownExpanded = false
                            }
                        )
                    }
                }

                // Match
                OutlinedTextField(
                    value = matchTitle,
                    onValueChange = { matchTitle = it },
                    label = { Text("Match (e.g. Arsenal vs Chelsea)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("match_title_input"),
                    singleLine = true
                )

                // League
                OutlinedTextField(
                    value = league,
                    onValueChange = { league = it },
                    label = { Text("League / Tournament (e.g. Premier League)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Tip / Pick
                OutlinedTextField(
                    value = predictionTip,
                    onValueChange = { predictionTip = it },
                    label = { Text("Prediction / Pick (e.g. Home Win / Over 2.5)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prediction_tip_input"),
                    singleLine = true
                )

                // Market Type Dropdown
                Text("Market Category", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = marketType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select Market",
                            modifier = Modifier.clickable { marketDropdownExpanded = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { marketDropdownExpanded = true }
                )

                DropdownMenu(
                    expanded = marketDropdownExpanded,
                    onDismissRequest = { marketDropdownExpanded = false }
                ) {
                    markets.forEach { market ->
                        DropdownMenuItem(
                            text = { Text(market) },
                            onClick = {
                                marketType = market
                                marketDropdownExpanded = false
                            }
                        )
                    }
                }

                // Odds & Stake row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = oddsText,
                        onValueChange = { oddsText = it },
                        label = { Text("Decimal Odds") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("odds_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = stakeText,
                        onValueChange = { stakeText = it },
                        label = { Text("Stake (Units)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Free daily checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isFreeDaily,
                        onCheckedChange = { isFreeDaily = it }
                    )
                    Text("This is a Free Daily Prediction (Testing tier)", style = MaterialTheme.typography.bodyMedium)
                }

                // Notes / Probability
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Confidence (e.g. Forebet 58% Home)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sId = selectedSite?.id ?: "custom"
                    val sName = selectedSite?.name ?: (if (customSiteName.isNotBlank()) customSiteName else "Independent Site")
                    val odds = oddsText.toDoubleOrNull() ?: 1.85
                    val stake = stakeText.toDoubleOrNull() ?: 1.0
                    val match = if (matchTitle.isBlank()) "Match Pick" else matchTitle
                    val tip = if (predictionTip.isBlank()) "Selection" else predictionTip
                    val lge = if (league.isBlank()) "Soccer" else league

                    onConfirm(sId, sName, match, lge, tip, marketType, odds, stake, notes, isFreeDaily)
                },
                modifier = Modifier.testTag("submit_prediction_button")
            ) {
                Text("Save to Test Ledger")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
