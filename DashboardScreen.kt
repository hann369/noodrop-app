package com.noodrop.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noodrop.app.ui.common.*
import com.noodrop.app.ui.theme.NdOrange
import com.noodrop.app.ui.theme.NdRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onLogToday: () -> Unit,
    vm: DashboardViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    // Handle refresh
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            // Simulate refresh delay
            kotlinx.coroutines.delay(1000)
            // In a real app, you'd refresh data here
            isRefreshing = false
        }
    }

    if (s.isLoading && !isRefreshing) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NdOrange)
        }
        return
    }

    PullToRefreshBox(
        state = pullRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column {
                Text(
                    buildAnnotatedString {
                        append(s.greeting)
                        withStyle(SpanStyle(color = NdOrange)) { append(".") }
                    },
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(s.dateLabel, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Streak",    s.streak.toString(),    "days",           NdOrange, Modifier.weight(1f))
                StatCard("Mood",      s.todayMood,            "/ 10",           modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Brain Fog", s.todayFog,             "lower = better", modifier = Modifier.weight(1f))
                StatCard("Compounds", s.stackSize.toString(), "in stack",       modifier = Modifier.weight(1f))
            }

            NdCard {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    SectionLabel("Today's Protocol")
                    OrangeChip("${s.checkedToday.size}/${s.stackSize}")
                }
                Spacer(Modifier.height(4.dp))
                if (s.stack.isEmpty()) {
                    EnhancedEmptyState(
                        icon = "🧪",
                        title = "Build Your Stack",
                        description = "Create your personalized nootropic protocol to get started with tracking and optimization.",
                        primaryAction = "Go to Stack" to { /* Navigate to stack */ },
                    )
                } else {
                    s.stack.forEach { entry ->
                        ChecklistRow(
                            name = entry.compoundName, dose = entry.dose,
                            timing = entry.timing.label,
                            checked = entry.compoundName in s.checkedToday,
                            onClick = { /* Integrate with daily tracker if needed */ },
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }
            }

            NdCard {
                SectionLabel("7-Day Wellbeing")
                Spacer(Modifier.height(6.dp))
                if (s.recentLogs.any { it.mood > 0 }) {
                    val trend = s.recentLogs.filter { it.mood > 0 }.map { it.mood }.joinToString(" \u2192 ")
                    Text(trend, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    EnhancedEmptyState(
                        icon = "📊",
                        title = "Track Your Progress",
                        description = "Start logging your daily metrics to see how your stack affects your wellbeing over time.",
                        primaryAction = "Log Today" to onLogToday,
                    )
                }
            }

            // NEW: Evidence-Based Suggestions
            if (s.suggestions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel("🎯 Personalized Insights")
                    s.suggestions.take(2).forEach { suggestion ->
                        SuggestionCard(
                            suggestion = suggestion,
                            onViewDetails = { /* Navigate to compound detail */ },
                            onAddToStack = { /* Add to stack */ },
                        )
                    }
                }
            }

            NdCard {
                SectionLabel("30-Day Consistency")
                Spacer(Modifier.height(8.dp))
                if (s.streakDays.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        s.streakDays.chunked(7).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                row.forEach { StreakDot(it) }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        LegendDot(NdOrange, "Done")
                        LegendDot(NdRed.copy(alpha = 0.4f), "Missed")
                        LegendDot(MaterialTheme.colorScheme.surfaceVariant, "No data")
                    }
                }
            }

            NdButton("Log Today \u2192", onClick = onLogToday)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
