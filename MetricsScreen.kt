package com.noodrop.app.ui.metrics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noodrop.app.data.model.DayLog
import com.noodrop.app.data.model.CompoundImpactScore
import com.noodrop.app.ui.common.*
import com.noodrop.app.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun MetricsScreen(vm: MetricsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Health Metrics", style = MaterialTheme.typography.displaySmall)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7 to "7 Days", 14 to "2 Weeks", 30 to "30 Days").forEach { (days, label) ->
                    NdFilterChip(label, s.period == days, onClick = { vm.setPeriod(days) })
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Avg Mood",   s.avgMood,   "/10", modifier = Modifier.weight(1f))
                StatCard("Avg Fog",    s.avgFog,    "/10", modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Avg Energy", s.avgEnergy, "/10", modifier = Modifier.weight(1f))
                StatCard("Avg Health", s.avgHealth, "/10", modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Avg Focus", s.avgFocus, "/10", modifier = Modifier.weight(1f))
            }

            val hasData = s.logs.any { it.mood > 0 }
            if (!hasData) {
                NdCard { EmptyState("[Chart]", "Log your first day to see charts") }
            } else {
                NdCard {
                    SectionLabel("Mood & Brain Fog")
                    Spacer(Modifier.height(8.dp))
                    SimpleLineChart(
                        series = listOf(
                            s.logs.map { it.mood.toFloat() } to NdOrange,
                            s.logs.map { it.fog.toFloat() }  to NdBlue,
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ChartLegendItem(NdOrange, "Mood")
                        ChartLegendItem(NdBlue,   "Brain Fog")
                    }
                }
                NdCard {
                    SectionLabel("Energy & Focus")
                    Spacer(Modifier.height(8.dp))
                    SimpleLineChart(
                        series = listOf(
                            s.logs.map { it.energy.toFloat() } to NdGreen,
                            s.logs.map { it.focus.toFloat() }  to NdPurple,
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ChartLegendItem(NdGreen,  "Energy")
                        ChartLegendItem(NdPurple, "Focus")
                    }
                }
                NdCard {
                    SectionLabel("General Health")
                    Spacer(Modifier.height(8.dp))
                    SimpleBarChart(values = s.logs.map { it.health.toFloat() })
                }
            }

            NdCard {
                SectionLabel("Daily Log History")
                Spacer(Modifier.height(4.dp))
                LogTable(s.logs.sortedByDescending { it.date })
            }

            // NEW STEP 2: Top Compounds Section
            if (s.topCompounds.isNotEmpty()) {
                NdCard {
                    SectionLabel("🌟 Top Compounds For You")
                    Spacer(Modifier.height(8.dp))
                    s.topCompounds.forEach { impact ->
                        CompoundImpactRow(impact)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // NEW STEP 2: Best/Worst Days Analysis
            if (s.bestWorstAnalysis != null) {
                NdCard {
                    SectionLabel("📊 Your Trends")
                    Spacer(Modifier.height(12.dp))
                    val analysis = s.bestWorstAnalysis!!
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (analysis.bestDay != null) {
                            Column(
                                Modifier
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text("Best Day", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    analysis.bestDay.mood.toString(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        if (analysis.worstDay != null) {
                            Column(
                                Modifier
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text("Worst Day", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer)
                                Text(
                                    analysis.worstDay.mood.toString(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        analysis.moodTrend,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (analysis.compoundsInBestDay.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Best day stack: ${analysis.compoundsInBestDay.take(3).joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// - Simple Canvas line chart (no external library) -
@Composable
private fun SimpleLineChart(
    series: List<Pair<List<Float>, Color>>,
    modifier: Modifier = Modifier,
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier.fillMaxWidth().height(180.dp)) {
        val w = size.width
        val h = size.height
        val padL = 24f
        val padB = 24f
        val chartW = w - padL
        val chartH = h - padB

        // Grid lines
        for (i in 0..4) {
            val y = chartH - (chartH * i / 4f)
            drawLine(gridColor, Offset(padL, y), Offset(w, y), strokeWidth = 1f)
        }

        series.forEach { (values, color) ->
            val valid = values.filter { it > 0 }
            if (valid.size < 2) return@forEach
            val max = 10f
            val step = chartW / (values.size - 1).coerceAtLeast(1)

            val path = Path()
            var first = true
            values.forEachIndexed { i, v ->
                if (v <= 0) return@forEachIndexed
                val x = padL + i * step
                val y = chartH - (v / max * chartH)
                if (first) { path.moveTo(x, y); first = false }
                else path.lineTo(x, y)
            }
            drawPath(path, color, style = Stroke(width = 3f))
        }
    }
}

// - Simple Canvas bar chart -
@Composable
private fun SimpleBarChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
) {
    val barColor  = NdOrange
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier.fillMaxWidth().height(150.dp)) {
        val w = size.width
        val h = size.height
        val padB = 16f
        val chartH = h - padB
        val max = 10f
        val barW = (w / values.size.coerceAtLeast(1)) * 0.6f
        val gap  = (w / values.size.coerceAtLeast(1)) * 0.4f

        drawLine(gridColor, Offset(0f, chartH), Offset(w, chartH), strokeWidth = 1f)

        values.forEachIndexed { i, v ->
            if (v <= 0) return@forEachIndexed
            val barH = (v / max) * chartH
            val x = i * (barW + gap) + gap / 2f
            val y = chartH - barH
            drawRoundRect(
                color  = barColor,
                topLeft = Offset(x, y),
                size   = Size(barW, barH),
                cornerRadius = CornerRadius(4f),
            )
        }
    }
}

@Composable
private fun ChartLegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(10.dp)) { drawCircle(color) }
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// - Log table -
@Composable
private fun LogTable(logs: List<DayLog>) {
    val fmt = DateTimeFormatter.ofPattern("MMM d")
    if (logs.isEmpty()) {
        Text("No logs yet.", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Row(Modifier.fillMaxWidth()) {
        listOf("Date" to 2f, "Mood" to 1f, "Fog" to 1f, "Energy" to 1f, "Focus" to 1f, "Health" to 1f)
            .forEach { (h, w) ->
                Text(h, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(w))
            }
    }
    HorizontalDivider(Modifier.padding(vertical = 6.dp))
    logs.take(30).forEach { log ->
        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
            Text(log.date.format(fmt), style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(2f))
            listOf(log.mood, log.fog, log.energy, log.focus, log.health).forEach { v ->
                Text(if (v > 0) v.toString() else "-",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f))
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

// NEW STEP 2 COMPONENTS


@Composable
fun CompoundImpactRow(impact: CompoundImpactScore) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(impact.compoundName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("Used ${impact.usageCount}x", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val impactColor = if (impact.impactMood > 0) NdGreen else NdOrange
            Text(
                "%+.1f".format(impact.impactMood),
                style = MaterialTheme.typography.labelMedium,
                color = impactColor,
                fontWeight = FontWeight.SemiBold
            )
            Text("Mood", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
