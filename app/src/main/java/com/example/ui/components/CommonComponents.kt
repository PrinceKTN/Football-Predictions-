package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PricingModel
import com.example.data.model.RiskLevel
import com.example.ui.theme.AmberVIP
import com.example.ui.theme.AmberVIPLight
import com.example.ui.theme.CrimsonLoss
import com.example.ui.theme.CrimsonLossLight
import com.example.ui.theme.CyanOdds
import com.example.ui.theme.CyanOddsLight
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.EmeraldWinLight

@Composable
fun RiskBadge(level: RiskLevel, modifier: Modifier = Modifier) {
    val (bg, fg) = when (level) {
        RiskLevel.LOW_TRANSPARENT -> EmeraldWinLight to Color(0xFF065F46)
        RiskLevel.MODERATE_PROMOTIONAL -> AmberVIPLight to Color(0xFF92400E)
        RiskLevel.HIGH_UNVERIFIED -> CrimsonLossLight to Color(0xFF991B1B)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("risk_badge_${level.name}")
    ) {
        Text(
            text = level.label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PricingBadge(model: PricingModel, modifier: Modifier = Modifier) {
    val (bg, fg) = when (model) {
        PricingModel.TOTALLY_FREE -> EmeraldWin.copy(alpha = 0.15f) to EmeraldWin
        PricingModel.FREEMIUM -> CyanOdds.copy(alpha = 0.15f) to CyanOdds
        PricingModel.PAID_SUBSCRIPTION -> AmberVIP.copy(alpha = 0.15f) to AmberVIP
        PricingModel.PER_TIPSTER_MARKETPLACE -> Color(0xFF8B5CF6).copy(alpha = 0.15f) to Color(0xFF8B5CF6)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("pricing_badge_${model.name}")
    ) {
        Text(
            text = model.label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun MetricChip(label: String, value: String, color: Color = MaterialTheme.colorScheme.primary) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$label: $value",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
