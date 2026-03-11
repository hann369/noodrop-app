package com.noodrop.app.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noodrop.app.data.model.DayStatus
import com.noodrop.app.ui.common.*
import com.noodrop.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogToday: () -> Unit,
    vm: DashboardViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            kotlinx.coroutines.delay(800)
            isRefreshing = false
        }
    }

    if (s.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NdOrange)
        }
        return
    }

    PullToRefreshBox(
        state       = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh   = { isRefreshing = true },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Greeting ──────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        buildAnnotatedString {
                            append(s.greeting)
                            withStyle(SpanStyle(color = NdOrange)) { append(".") }
                        },
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        s.dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ── Hero Row: Progress Ring + Body Weight style stat ──────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Protocol progress card (Dropset-style circle)
                HeroCard(
                    modifier = Modifier.weight(1f),
                    minHeight = 160.dp,
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Top right settings-style icon
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("⚙", fontSize = 12.sp)
                            }
                        }

                        // Animated progress ring
                        Box(Modifier.align(Alignment.CenterHorizontally)) {
                            ProgressRing(
                                checked  = s.checkedToday.size,
                                total    = s.stackSize.coerceAtLeast(1),
                                size     = 72.dp,
                                color    = NdOrange,
                            )
                        }

                        Column {
                            Text(
                                "Today's Stack",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "${s.checkedToday.size}/${s.stackSize} taken",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Streak + Mood column
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    HeroCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Box(
                                    Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center,
                                ) { Text("⚙", fontSize = 12.sp) }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                s.streak.toString(),
                                style      = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color      = NdOrange,
                            )
                            Text(
                                "Day streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    HeroCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Box(
                                    Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center,
                                ) { Text("⚙", fontSize = 12.sp) }
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    s.todayMood,
                                    style      = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (s.todayMood != "-") {
                                    Text(
                                        "/10",
                                        style    = MaterialTheme.typography.bodySmall,
                                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 6.dp),
                                    )
                                }
                            }
                            Text(
                                "Mood today",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // ── Heatmap (Dropset dot calendar) ────────────────────────────────
            if (s.streakDays.isNotEmpty()) {
                HeatmapCard(
                    days     = s.streakDays,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ── Stack compound list (Dropset workout-row style) ───────────────
            if (s.stack.isNotEmpty()) {
                HeroCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text("Today's Protocol", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            OrangeChip("${s.checkedToday.size}/${s.stackSize}")
                        }
                        Spacer(Modifier.height(2.dp))
                        s.stack.forEach { entry ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (entry.compoundName in s.checkedToday)
                                            NdGreen.copy(alpha = 0.07f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                ) {
                                    // Check indicator
                                    Box(
                                        Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (entry.compoundName in s.checkedToday) NdGreen
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (entry.compoundName in s.checkedToday) {
                                            Text("✓", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(entry.compoundName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    NdChip(entry.dose)
                                    NdChip(entry.timing.label)
                                }
                            }
                        }
                    }
                }
            } else {
                HeroCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("🧪", fontSize = 32.sp)
                        Text("Build Your Stack", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Add compounds to start tracking your protocol",
                            style     = MaterialTheme.typography.bodySmall,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            // ── Log Today button (Dropset FAB-style full width) ───────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(NdOrange, Color(0xFFFF8C42))
                        )
                    )
                    .clickable(onClick = onLogToday)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Log Today →",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White,
                )
            }

            // ── Suggestions ───────────────────────────────────────────────────
            if (s.suggestions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "INSIGHTS",
                        style    = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    s.suggestions.take(2).forEach { suggestion ->
                        SuggestionCard(
                            suggestion    = suggestion,
                            onViewDetails = {},
                            onAddToStack  = {},
                        )
                    }
                }
            }

            // ── Quick stats row ───────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickStatCard("Brain Fog", s.todayFog, "lower = better", Modifier.weight(1f))
                QuickStatCard("Compounds", s.stackSize.toString(), "in stack", Modifier.weight(1f))
            }
        }
    }
}

// ── Hero Card (Dropset dark card style) ───────────────────────────────────────
@Composable
private fun HeroCard(
    modifier:  Modifier = Modifier,
    minHeight: androidx.compose.ui.unit.Dp = 80.dp,
    content:   @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .defaultMinSize(minHeight = minHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface),
        content = content,
    )
}

// ── Animated Progress Ring (Dropset circle) ───────────────────────────────────
@Composable
private fun ProgressRing(
    checked: Int,
    total:   Int,
    size:    androidx.compose.ui.unit.Dp,
    color:   Color,
) {
    val progress = if (total > 0) checked.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(
        targetValue    = progress,
        animationSpec  = tween(800, easing = EaseOutCubic),
    )

    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = 6.dp.toPx()
            val radius      = (size.toPx() - strokeWidth) / 2
            val center      = androidx.compose.ui.geometry.Offset(size.toPx() / 2, size.toPx() / 2)

            // Track
            drawCircle(
                color  = color.copy(alpha = 0.15f),
                radius = radius,
                style  = Stroke(width = strokeWidth),
            )
            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    color      = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter  = false,
                    style      = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }
        // Center number
        Text(
            checked.toString(),
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = color,
        )
    }
}

// ── Heatmap (Dropset dot calendar) ────────────────────────────────────────────
@Composable
private fun HeatmapCard(days: List<DayStatus>, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Month labels
            val today   = LocalDate.now()
            val months  = (0 until 30).map { today.minusDays((29 - it).toLong()) }
            val fmt     = DateTimeFormatter.ofPattern("MMM")
            val monthLabels = mutableListOf<Pair<Int, String>>() // index → label
            var lastMonth = ""
            months.forEachIndexed { i, d ->
                val m = d.format(fmt)
                if (m != lastMonth) { monthLabels.add(i to m); lastMonth = m }
            }

            // Month label row
            Box(Modifier.fillMaxWidth().height(16.dp)) {
                monthLabels.forEach { (idx, label) ->
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            label,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = (idx * 14).dp),
                        )
                    }
                }
            }

            // Dot grid — 2 rows of 15 (like Dropset)
            val rows = days.chunked(15)
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { status ->
                        val dotColor = when (status) {
                            DayStatus.DONE    -> NdOrange
                            DayStatus.MISSED  -> NdRed.copy(alpha = 0.3f)
                            DayStatus.NO_DATA -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(dotColor),
                        )
                    }
                }
            }

            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                LegendDot(NdOrange, "Done")
                LegendDot(NdRed.copy(alpha = 0.3f), "Missed")
                LegendDot(MaterialTheme.colorScheme.surfaceVariant, "No data")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Quick stat card ───────────────────────────────────────────────────────────
@Composable
private fun QuickStatCard(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
    ) {
        Column {
            Text(
                label.uppercase(),
                style  = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color  = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
