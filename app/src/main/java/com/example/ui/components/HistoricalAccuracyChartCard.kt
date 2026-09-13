package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyAccuracyPoint
import com.example.data.model.SiteAccuracyHistory
import com.example.ui.PredictionViewModel
import com.example.ui.theme.AmberVIP
import com.example.ui.theme.CrimsonLoss
import com.example.ui.theme.CyanOdds
import com.example.ui.theme.EmeraldWin
import java.util.Locale

@Composable
fun HistoricalAccuracyChartCard(
    viewModel: PredictionViewModel,
    modifier: Modifier = Modifier
) {
    val siteHistories = viewModel.siteHistories
    val selectedSiteId by viewModel.selectedHistoricalSiteId.collectAsState()
    val chartMode by viewModel.chartViewMode.collectAsState() // "trend" or "bars"
    var isExpanded by remember { mutableStateOf(true) }
    var showMathExplanation by remember { mutableStateOf(false) }

    val activeSite = siteHistories.firstOrNull { it.siteId == selectedSiteId } ?: siteHistories.first()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("historical_accuracy_chart"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, CyanOdds.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
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
                            .background(CyanOdds.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = CyanOdds,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Historical Accuracy (Last 30 Days)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tracked accuracy percentage vs 52.4% break-even line",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showMathExplanation = !showMathExplanation }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Accuracy Explanation",
                            tint = CyanOdds,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Math Explanation Dropdown
            AnimatedVisibility(visible = showMathExplanation) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Why 52.38% is the Golden Threshold:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CyanOdds
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "At typical betting odds (-110 / 1.91), bookmakers charge ~4.5% commission (the 'vig' or juice). To simply break even without losing money, an algorithmic prediction model must achieve at least 52.38% long-term accuracy. Anything above 55% over a 30-day window generates statistically verified profitability.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Mode Selector Tabs (30-Day Trend Curve vs 30-Day Site Comparison Bars)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Trend Line Mode Button
                        val isTrend = chartMode == "trend"
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setChartViewMode("trend") }
                                .testTag("chart_mode_trend"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTrend) CyanOdds.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = BorderStroke(
                                if (isTrend) 1.5.dp else 1.dp,
                                if (isTrend) CyanOdds else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ShowChart,
                                    contentDescription = null,
                                    tint = if (isTrend) CyanOdds else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "30-Day Trend Curve",
                                    fontSize = 12.sp,
                                    fontWeight = if (isTrend) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isTrend) CyanOdds else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Bar Comparison Mode Button
                        val isBars = chartMode == "bars"
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setChartViewMode("bars") }
                                .testTag("chart_mode_bars"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isBars) EmeraldWin.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = BorderStroke(
                                if (isBars) 1.5.dp else 1.dp,
                                if (isBars) EmeraldWin else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = if (isBars) EmeraldWin else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Site Comparison Bars",
                                    fontSize = 12.sp,
                                    fontWeight = if (isBars) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isBars) EmeraldWin else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Site Selection Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.testTag("chart_site_chips")
                    ) {
                        items(siteHistories) { siteHistory ->
                            val isSelected = siteHistory.siteId == selectedSiteId
                            val siteColor = Color(siteHistory.colorHex)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectHistoricalSite(siteHistory.siteId) },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(siteColor)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "${siteHistory.siteName} (${String.format(Locale.US, "%.1f", siteHistory.overall30DayAccuracy)}%)",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = siteColor.copy(alpha = 0.2f),
                                    selectedLabelColor = siteColor
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) siteColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    borderWidth = if (isSelected) 1.5.dp else 1.dp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Top Metrics Banner for Selected Site
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "30-DAY ACCURACY",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", activeSite.overall30DayAccuracy)}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (activeSite.overall30DayAccuracy >= 52.4) EmeraldWin else CrimsonLoss
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (activeSite.overall30DayAccuracy >= 52.4) "(+${String.format(Locale.US, "%.1f", activeSite.overall30DayAccuracy - 52.38)}% over vig)" else "(below vig)",
                                    fontSize = 10.sp,
                                    color = if (activeSite.overall30DayAccuracy >= 52.4) EmeraldWin else CrimsonLoss
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SAMPLE SIZE",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${activeSite.total30DayWon}W / ${activeSite.total30DayBets} Bets",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "EST. ROI / YIELD",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = (if (activeSite.netRoiPercentage >= 0) "+" else "") + "${String.format(Locale.US, "%.1f", activeSite.netRoiPercentage)}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeSite.netRoiPercentage >= 0) EmeraldWin else CrimsonLoss
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // THE CHART RENDERER
                    if (chartMode == "trend") {
                        NativeComposeTrendLineChart(site = activeSite)
                    } else {
                        NativeComposeSiteComparisonBars(
                            histories = siteHistories,
                            selectedId = selectedSiteId,
                            onSelect = { viewModel.selectHistoricalSite(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chart Legend & Guide
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(3.dp)
                                    .background(AmberVIP, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "52.4% Break-Even",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AmberVIP
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(3.dp)
                                    .background(EmeraldWin, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "60% Elite Model Target",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldWin
                            )
                        }

                        Text(
                            text = "30 Days (Aug 15 - Sep 13)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Native Jetpack Compose Canvas Trend Line Chart with Interactive Scrubber
 */
@Composable
fun NativeComposeTrendLineChart(
    site: SiteAccuracyHistory,
    modifier: Modifier = Modifier
) {
    var touchedPointIndex by remember { mutableStateOf<Int?>(null) }
    val points = site.dailyPoints
    val primaryColor = Color(site.colorHex)

    // Smooth animation on data swap
    val progress = remember { Animatable(0f) }
    LaunchedEffect(site.siteId) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 600))
    }

    val touchedPoint = touchedPointIndex?.let { index ->
        if (index in points.indices) points[index] else null
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Scrubber Tooltip Overlay if user touches canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (touchedPoint != null) {
                val isAboveBreakEven = touchedPoint.accuracyPercentage >= 52.38
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (isAboveBreakEven) EmeraldWin else AmberVIP, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Day ${touchedPoint.dayNumber} (${touchedPoint.dateLabel})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", touchedPoint.accuracyPercentage)}% Accuracy",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = if (isAboveBreakEven) EmeraldWin else CrimsonLoss
                    )
                    Text(
                        text = "(${touchedPoint.betsWon}/${touchedPoint.betsTotal} hits)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Touch or drag across the chart to inspect daily accuracy",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // THE CANVAS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .pointerInput(points) {
                    detectTapGestures(
                        onPress = { offset ->
                            val paddingLeft = 40.dp.toPx()
                            val paddingRight = 16.dp.toPx()
                            val chartWidth = size.width - paddingLeft - paddingRight
                            if (chartWidth > 0 && offset.x >= paddingLeft && offset.x <= size.width - paddingRight) {
                                val normalizedX = (offset.x - paddingLeft) / chartWidth
                                val index = (normalizedX * (points.size - 1)).toInt().coerceIn(0, points.size - 1)
                                touchedPointIndex = index
                            }
                        }
                    )
                }
                .pointerInput(points) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            val paddingLeft = 40.dp.toPx()
                            val paddingRight = 16.dp.toPx()
                            val chartWidth = size.width - paddingLeft - paddingRight
                            if (chartWidth > 0 && change.position.x >= paddingLeft && change.position.x <= size.width - paddingRight) {
                                val normalizedX = (change.position.x - paddingLeft) / chartWidth
                                val index = (normalizedX * (points.size - 1)).toInt().coerceIn(0, points.size - 1)
                                touchedPointIndex = index
                            }
                        },
                        onDragEnd = {
                            // keep last touched or clear
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                val paddingLeft = 42.dp.toPx()
                val paddingRight = 18.dp.toPx()
                val paddingTop = 20.dp.toPx()
                val paddingBottom = 28.dp.toPx()

                val chartWidth = size.width - paddingLeft - paddingRight
                val chartHeight = size.height - paddingTop - paddingBottom

                if (chartWidth <= 0 || chartHeight <= 0 || points.isEmpty()) return@Canvas

                val minAcc = 35.0
                val maxAcc = 75.0
                val rangeAcc = maxAcc - minAcc

                fun getYForAccuracy(acc: Double): Float {
                    val normalized = ((acc - minAcc) / rangeAcc).toFloat()
                    return paddingTop + chartHeight * (1f - normalized)
                }

                fun getXForIndex(index: Int): Float {
                    return paddingLeft + chartWidth * (index.toFloat() / (points.size - 1).toFloat())
                }

                // 1. Draw Grid Lines and Y-Axis Labels
                val gridLevels = listOf(40.0, 50.0, 52.38, 60.0, 70.0)
                gridLevels.forEach { level ->
                    val y = getYForAccuracy(level)
                    val isBreakEven = level == 52.38
                    val isElite = level == 60.0

                    val lineColor = when {
                        isBreakEven -> AmberVIP.copy(alpha = 0.85f)
                        isElite -> EmeraldWin.copy(alpha = 0.5f)
                        else -> Color.Gray.copy(alpha = 0.2f)
                    }

                    val strokeWidth = if (isBreakEven) 2.dp.toPx() else 1.dp.toPx()
                    val pathEffect = if (isBreakEven || isElite) PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f) else null

                    drawLine(
                        color = lineColor,
                        start = Offset(paddingLeft, y),
                        end = Offset(size.width - paddingRight, y),
                        strokeWidth = strokeWidth,
                        pathEffect = pathEffect
                    )
                }

                // 2. Build Bezier Path for Accuracy Percentage
                val linePath = Path()
                val fillPath = Path()

                val animProgress = progress.value
                val visibleCount = (points.size * animProgress).toInt().coerceAtLeast(1)

                for (i in 0 until visibleCount) {
                    val x = getXForIndex(i)
                    val y = getYForAccuracy(points[i].accuracyPercentage)

                    if (i == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, getYForAccuracy(minAcc))
                        fillPath.lineTo(x, y)
                    } else {
                        val prevX = getXForIndex(i - 1)
                        val prevY = getYForAccuracy(points[i - 1].accuracyPercentage)
                        val controlX1 = prevX + (x - prevX) / 2f
                        val controlX2 = prevX + (x - prevX) / 2f
                        linePath.cubicTo(controlX1, prevY, controlX2, y, x, y)
                        fillPath.cubicTo(controlX1, prevY, controlX2, y, x, y)
                    }

                    if (i == visibleCount - 1) {
                        fillPath.lineTo(x, getYForAccuracy(minAcc))
                        fillPath.close()
                    }
                }

                // Draw Gradient Area Fill under the curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.35f),
                            primaryColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        startY = paddingTop,
                        endY = paddingTop + chartHeight
                    )
                )

                // Draw Smooth Line Stroke
                drawPath(
                    path = linePath,
                    color = primaryColor,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw Data Points on specific intervals (every 3 days)
                for (i in 0 until visibleCount) {
                    if (i % 3 == 0 || i == points.size - 1) {
                        val x = getXForIndex(i)
                        val y = getYForAccuracy(points[i].accuracyPercentage)
                        val isOver = points[i].accuracyPercentage >= 52.38
                        drawCircle(
                            color = if (isOver) EmeraldWin else AmberVIP,
                            radius = 3.5.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 1.5.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }

                // 3. Draw Active Touch Scrubber if user is scrubbing
                touchedPointIndex?.let { index ->
                    if (index in 0 until visibleCount) {
                        val touchX = getXForIndex(index)
                        val touchY = getYForAccuracy(points[index].accuracyPercentage)

                        // Vertical scrubber guide line
                        drawLine(
                            color = Color.White.copy(alpha = 0.8f),
                            start = Offset(touchX, paddingTop),
                            end = Offset(touchX, paddingTop + chartHeight),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Outer glowing pulse
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.3f),
                            radius = 9.dp.toPx(),
                            center = Offset(touchX, touchY)
                        )
                        // Inner active point
                        drawCircle(
                            color = primaryColor,
                            radius = 5.dp.toPx(),
                            center = Offset(touchX, touchY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(touchX, touchY)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // X-Axis Timeline Markers (Day 1, Day 10, Day 20, Day 30)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 42.dp, end = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Day 1 (${points.firstOrNull()?.dateLabel ?: ""})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Day 10", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Day 20", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Day 30 (Today)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanOdds)
        }
    }
}

/**
 * Native Compose Horizontal Bar Chart comparing all tracked betting sites over 30 Days
 */
@Composable
fun NativeComposeSiteComparisonBars(
    histories: List<SiteAccuracyHistory>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        histories.forEach { site ->
            val isSelected = site.siteId == selectedId
            val siteColor = Color(site.colorHex)
            val isProfitable = site.overall30DayAccuracy >= 52.38

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(site.siteId) },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = if (isSelected) BorderStroke(1.5.dp, siteColor) else null
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(siteColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = site.siteName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${site.category}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", site.overall30DayAccuracy)}%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (isProfitable) EmeraldWin else CrimsonLoss
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background((if (isProfitable) EmeraldWin else CrimsonLoss).copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isProfitable) "+${String.format(Locale.US, "%.1f", site.netRoiPercentage)}% ROI" else "${String.format(Locale.US, "%.1f", site.netRoiPercentage)}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isProfitable) EmeraldWin else CrimsonLoss
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar showing accuracy relative to 70% scale with 52.4% line marked
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        // Accuracy Filled Bar
                        val fraction = (site.overall30DayAccuracy / 70.0).toFloat().coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(14.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(siteColor.copy(alpha = 0.7f), siteColor)
                                    )
                                )
                        )

                        // 52.4% Break-Even Vertical Line Marker
                        val breakEvenFraction = (52.38 / 70.0).toFloat()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(breakEvenFraction)
                                .height(14.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(14.dp)
                                    .background(AmberVIP)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${site.total30DayWon} wins / ${site.total30DayBets} tracked picks",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isProfitable) "Beats Bookmaker Juice" else "Sub-optimal Vig Drag",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isProfitable) EmeraldWin else AmberVIP
                        )
                    }
                }
            }
        }
    }
}
