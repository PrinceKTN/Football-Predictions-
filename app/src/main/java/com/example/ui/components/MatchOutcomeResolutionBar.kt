package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BetPrediction
import com.example.data.model.PredictionStatus
import com.example.ui.theme.CrimsonLoss
import com.example.ui.theme.CyanOdds
import com.example.ui.theme.EmeraldWin
import java.util.Locale

/**
 * A dedicated, accessible UI component allowing users to manually mark a predicted game
 * as 'Won' or 'Lost' after match conclusion, automatically updating their local performance history.
 */
@Composable
fun MatchOutcomeResolutionBar(
    prediction: BetPrediction,
    onStatusChange: (PredictionStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }

    // Net unit profit if won
    val profitUnits = remember(prediction.odds, prediction.stakeUnits) {
        (prediction.odds - 1.0) * prediction.stakeUnits
    }

    val isPending = prediction.status == PredictionStatus.PENDING

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("outcome_resolution_bar_${prediction.id}")
    ) {
        // If the match is already resolved and not in editing mode, show the confirmed outcome summary
        if (!isPending && !isEditing) {
            ResolvedOutcomeSummary(
                prediction = prediction,
                profitUnits = profitUnits,
                onEditClick = { isEditing = true }
            )
        } else {
            // Active Outcome Resolution Selector (when pending or editing)
            ActiveOutcomeSelector(
                prediction = prediction,
                profitUnits = profitUnits,
                isEditing = isEditing,
                onSelectOutcome = { newStatus ->
                    onStatusChange(newStatus)
                    isEditing = false
                    val feedbackText = when (newStatus) {
                        PredictionStatus.WON -> "Marked as WON! +${String.format(Locale.US, "%.2f", profitUnits)}u added to ${prediction.siteName} history."
                        PredictionStatus.LOST -> "Marked as LOST. -${String.format(Locale.US, "%.2f", prediction.stakeUnits)}u recorded in ${prediction.siteName} history."
                        PredictionStatus.VOID -> "Marked as VOID / Push. Stake refunded in ${prediction.siteName} history."
                        PredictionStatus.PENDING -> "Reset to Pending status."
                    }
                    Toast.makeText(context, feedbackText, Toast.LENGTH_SHORT).show()
                },
                onCancelEdit = {
                    if (!isPending) isEditing = false
                }
            )
        }
    }
}

/**
 * Clean card showing confirmed result with quick 1-tap option to adjust if marked by mistake.
 */
@Composable
private fun ResolvedOutcomeSummary(
    prediction: BetPrediction,
    profitUnits: Double,
    onEditClick: () -> Unit
) {
    val (bgColor, borderColor, textColor, title, subText) = when (prediction.status) {
        PredictionStatus.WON -> OutcomeStyle(
            bgColor = EmeraldWin.copy(alpha = 0.15f),
            borderColor = EmeraldWin,
            textColor = EmeraldWin,
            title = "SETTLED: WON",
            subText = "+${String.format(Locale.US, "%.2f", profitUnits)}u profit logged"
        )
        PredictionStatus.LOST -> OutcomeStyle(
            bgColor = CrimsonLoss.copy(alpha = 0.15f),
            borderColor = CrimsonLoss,
            textColor = CrimsonLoss,
            title = "SETTLED: LOST",
            subText = "-${String.format(Locale.US, "%.2f", prediction.stakeUnits)}u stake lost"
        )
        PredictionStatus.VOID -> OutcomeStyle(
            bgColor = Color.Gray.copy(alpha = 0.15f),
            borderColor = Color.Gray,
            textColor = Color.LightGray,
            title = "SETTLED: VOID (PUSH)",
            subText = "Stake refunded"
        )
        PredictionStatus.PENDING -> OutcomeStyle(
            bgColor = Color.Transparent,
            borderColor = Color.Transparent,
            textColor = Color.White,
            title = "PENDING",
            subText = "Awaiting final score"
        )
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(borderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (prediction.status) {
                            PredictionStatus.WON -> Icons.Default.Check
                            PredictionStatus.LOST -> Icons.Default.Close
                            else -> Icons.Default.Remove
                        },
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                    Text(
                        text = subText,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Edit button (meets 48dp touch target)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onEditClick)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("edit_outcome_button_${prediction.id}")
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Outcome",
                    tint = CyanOdds,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Edit",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanOdds
                )
            }
        }
    }
}

/**
 * The prominent, interactive Won / Lost / Void buttons.
 */
@Composable
private fun ActiveOutcomeSelector(
    prediction: BetPrediction,
    profitUnits: Double,
    isEditing: Boolean,
    onSelectOutcome: (PredictionStatus) -> Unit,
    onCancelEdit: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = CyanOdds,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEditing) "Change Match Outcome:" else "Match Concluded? Mark Outcome:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isEditing) {
                    Text(
                        text = "Cancel",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onCancelEdit)
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large, tactile Won & Lost buttons with >= 48dp height touch target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // WON BUTTON (Green)
                Button(
                    onClick = { onSelectOutcome(PredictionStatus.WON) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (prediction.status == PredictionStatus.WON) EmeraldWin else EmeraldWin.copy(alpha = 0.85f),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("mark_won_button_${prediction.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Mark Won",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "WON",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Text(
                                text = "+${String.format(Locale.US, "%.2f", profitUnits)}u",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // LOST BUTTON (Red)
                Button(
                    onClick = { onSelectOutcome(PredictionStatus.LOST) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (prediction.status == PredictionStatus.LOST) CrimsonLoss else CrimsonLoss.copy(alpha = 0.85f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("mark_lost_button_${prediction.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Mark Lost",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "LOST",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "-${String.format(Locale.US, "%.2f", prediction.stakeUnits)}u",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Secondary Options: Void (Push) and Reset to Pending
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mark Void / Push
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onSelectOutcome(PredictionStatus.VOID) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("mark_void_button_${prediction.id}")
                ) {
                    Text(
                        text = "⚪ Void / Push (Refund)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // If editing an already concluded game, give option to reset back to Pending
                if (isEditing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSelectOutcome(PredictionStatus.PENDING) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("reset_pending_button_${prediction.id}")
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            tint = CyanOdds,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset to Pending",
                            fontSize = 11.sp,
                            color = CyanOdds
                        )
                    }
                }
            }
        }
    }
}

private data class OutcomeStyle(
    val bgColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val title: String,
    val subText: String
)
