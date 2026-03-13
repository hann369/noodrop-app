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
import androidx.compose.ui.graphics.graphicsLayer
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
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Greeting ──────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        s.dateLabel,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        buildAnnotatedString {
                            append(s.greeting)
                            withStyle(SpanStyle(color = NdOrange)) { append(".") }
                        },
                        style      = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // ── Hero: Glass Stack Card (full width) ───────────────────────────
            StackProgressGlassCard(
                checkedCount = s.checkedToday.size,
                totalCount   = s.stackSize,
                modifier     = Modifier
                    .fillMaxWidth()
                    .height(172.dp),
            )

            // ── Heatmap (Dropset dot calendar) ────────────────────────────────
            if (s.streakDays.isNotEmpty()) {
                HeatmapCard(
                    days     = s.streakDays,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ── Activity Rings (Apple Watch style) ────────────────────────────
            ActivityRingsCard(
                rings = defaultActivityRings(
                    checkedCount = s.checkedToday.size,
                    stackSize    = s.stackSize,
                    streak       = s.streak,
                    mood         = s.todayMood.toIntOrNull() ?: 0,
                    moodStr      = s.todayMood,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Stack compound list (Dropset workout-row style) ───────────────
            if (s.stack.isNotEmpty()) {
                HeroCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text("Today's Protocol", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            OrangeChip("${s.checkedToday.size}/${s.stackSize}")
                        }
                        Spacer(Modifier.height(4.dp))
                        s.stack.forEach { entry ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (entry.compoundName in s.checkedToday)
                                            NdGreen.copy(alpha = 0.06f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 11.dp),
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
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
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
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(NdOrange, Color(0xFFFF8C42))
                        )
                    )
                    .clickable(onClick = onLogToday)
                    .padding(vertical = 18.dp),
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
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
        content = content,
    )
}

// ── Heatmap (Dropset dot calendar) ────────────────────────────────────────────
@Composable
private fun HeatmapCard(days: List<DayStatus>, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .padding(18.dp),
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
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                label.uppercase(),
                style  = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color  = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
