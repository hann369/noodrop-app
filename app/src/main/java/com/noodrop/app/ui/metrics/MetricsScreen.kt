package com.noodrop.app.ui.metrics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noodrop.app.data.model.CompoundImpactScore
import com.noodrop.app.data.model.DayLog
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
                .padding(top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "Health Metrics",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Track your cognitive performance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ── Period filter ─────────────────────────────────────────────────
            Row(
                Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(7 to "7D", 14 to "14D", 30 to "30D").forEach { (days, label) ->
                    val selected = s.period == days
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) NdOrange else MaterialTheme.colorScheme.surface
                            )
                            .then(
                                if (!selected) Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                else Modifier
                            )
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            label,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color      = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ── Horizontal stat scroll (Dropset style) ────────────────────────
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricStatCard("😊", "Mood",   s.avgMood,   NdOrange)
                MetricStatCard("🌫️", "Fog",    s.avgFog,    NdBlue,   invertGood = true)
                MetricStatCard("⚡", "Energy", s.avgEnergy, NdGreen)
                MetricStatCard("🎯", "Focus",  s.avgFocus,  NdPurple)
                MetricStatCard("💪", "Health", s.avgHealth, NdGreen)
            }

            val hasData = s.logs.any { it.mood > 0 }

            if (!hasData) {
                // FIX: emoji instead of [Chart] string
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("📊", fontSize = 40.sp)
                        Text("No Data Yet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Log your first day to see charts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                // ── Mood & Fog chart ──────────────────────────────────────────
                ChartCard(
                    title  = "Mood & Brain Fog",
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    SmoothLineChart(
                        series = listOf(
                            s.logs.map { it.mood.toFloat() }   to NdOrange,
                            s.logs.map { it.fog.toFloat() }    to NdBlue,
                        )
                    )
                    Row(
                        Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ChartLegend(NdOrange, "Mood")
                        ChartLegend(NdBlue,   "Brain Fog")
                    }
                }

                // ── Energy & Focus chart ──────────────────────────────────────
                ChartCard(
                    title  = "Energy & Focus",
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    SmoothLineChart(
                        series = listOf(
                            s.logs.map { it.energy.toFloat() } to NdGreen,
                            s.logs.map { it.focus.toFloat() }  to NdPurple,
                        )
                    )
                    Row(
                        Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ChartLegend(NdGreen,  "Energy")
                        ChartLegend(NdPurple, "Focus")
                    }
                }

                // ── Health bar chart ──────────────────────────────────────────
                ChartCard(
                    title  = "General Health",
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    SimpleBarChart(values = s.logs.map { it.health.toFloat() })
                }

                // ── Best/Worst day cards ───────────────────────────────────────
                s.bestWorstAnalysis?.let { analysis ->
                    Row(
                        Modifier.padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        analysis.bestDay?.let { day ->
                            TrendCard(
                                label     = "Best Day",
                                value     = day.mood.toString(),
                                sub       = "mood score",
                                color     = NdGreen,
                                modifier  = Modifier.weight(1f),
                            )
                        }
                        analysis.worstDay?.let { day ->
                            TrendCard(
                                label     = "Worst Day",
                                value     = day.mood.toString(),
                                sub       = "mood score",
                                color     = NdRed,
                                modifier  = Modifier.weight(1f),
                            )
                        }
                        TrendCard(
                            label    = "Trend",
                            value    = analysis.moodTrend.take(1),
                            sub      = analysis.moodTrend.drop(2),
                            color    = NdOrange,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // ── Top compounds ──────────────────────────────────────────────
                if (s.topCompounds.isNotEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                            .padding(20.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Top Compounds", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            s.topCompounds.take(4).forEach { impact ->
                                CompoundImpactRow(impact)
                            }
                        }
                    }
                }
            }

            // ── Log history table ──────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Log History", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    LogTable(s.logs.sortedByDescending { it.date })
                }
            }
        }
    }
}

// ── Horizontal metric stat card (Dropset style) ───────────────────────────────
@Composable
private fun MetricStatCard(
    emoji:      String,
    label:      String,
    value:      String,
    accent:     Color,
    invertGood: Boolean = false,
) {
    val numVal = value.toFloatOrNull() ?: 0f
    val isGood = if (invertGood) numVal < 5f else numVal >= 6f

    Box(
        Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(emoji, fontSize = 18.sp)
                if (value != "-") {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isGood) NdGreen else NdOrange),
                    )
                }
            }
            Text(
                value,
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = if (value == "-") MaterialTheme.colorScheme.onSurfaceVariant else accent,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Chart card wrapper ────────────────────────────────────────────────────────
@Composable
private fun ChartCard(
    title:    String,
    modifier: Modifier = Modifier,
    content:  @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .padding(20.dp),
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ── Trend card ────────────────────────────────────────────────────────────────
@Composable
private fun TrendCard(label: String, value: String, sub: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .border(0.5.dp, color.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Smooth line chart ─────────────────────────────────────────────────────────
@Composable
private fun SmoothLineChart(series: List<Pair<List<Float>, Color>>) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(Modifier.fillMaxWidth().height(160.dp)) {
        val w      = size.width
        val h      = size.height
        val padL   = 8f
        val padB   = 8f
        val chartW = w - padL
        val chartH = h - padB

        // Grid
        for (i in 0..4) {
            val y = chartH - (chartH * i / 4f)
            drawLine(gridColor, Offset(padL, y), Offset(w, y), strokeWidth = 0.5f)
        }

        series.forEach { (values, color) ->
            val valid = values.filter { it > 0 }
            if (valid.size < 2) return@forEach
            val max  = 10f
            val step = chartW / (values.size - 1).coerceAtLeast(1)

            // Gradient fill
            val fillPath = Path()
            var firstX = 0f
            var firstDrawn = false
            values.forEachIndexed { i, v ->
                if (v <= 0) return@forEachIndexed
                val x = padL + i * step
                val y = chartH - (v / max * chartH)
                if (!firstDrawn) { fillPath.moveTo(x, chartH); fillPath.lineTo(x, y); firstX = x; firstDrawn = true }
                else fillPath.lineTo(x, y)
            }
            fillPath.lineTo(padL + (values.size - 1) * step, chartH)
            fillPath.close()
            drawPath(fillPath, Brush.verticalGradient(listOf(color.copy(alpha = 0.2f), Color.Transparent)))

            // Line
            val linePath = Path()
            var first = true
            values.forEachIndexed { i, v ->
                if (v <= 0) return@forEachIndexed
                val x = padL + i * step
                val y = chartH - (v / max * chartH)
                if (first) { linePath.moveTo(x, y); first = false }
                else linePath.lineTo(x, y)
            }
            drawPath(linePath, color, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
        }
    }
}

// ── Bar chart ─────────────────────────────────────────────────────────────────
@Composable
private fun SimpleBarChart(values: List<Float>) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(Modifier.fillMaxWidth().height(120.dp)) {
        val w      = size.width
        val h      = size.height
        val padB   = 8f
        val chartH = h - padB
        val max    = 10f
        val n      = values.size.coerceAtLeast(1)
        val barW   = (w / n) * 0.55f
        val gap    = (w / n) * 0.45f

        drawLine(gridColor, Offset(0f, chartH), Offset(w, chartH), strokeWidth = 0.5f)

        values.forEachIndexed { i, v ->
            if (v <= 0) return@forEachIndexed
            val barH = (v / max) * chartH
            val x    = i * (barW + gap) + gap / 2f
            drawRoundRect(
                color        = NdOrange.copy(alpha = 0.85f),
                topLeft      = Offset(x, chartH - barH),
                size         = Size(barW, barH),
                cornerRadius = CornerRadius(4f),
            )
        }
    }
}

@Composable
private fun ChartLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Log history table ─────────────────────────────────────────────────────────
@Composable
private fun LogTable(logs: List<DayLog>) {
    val fmt = DateTimeFormatter.ofPattern("MMM d")
    if (logs.isEmpty()) {
        Text("No logs yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    // Header
    Row(Modifier.fillMaxWidth()) {
        listOf("Date" to 2f, "Mood" to 1f, "Fog" to 1f, "Energy" to 1f, "Focus" to 1f).forEach { (h, w) ->
            Text(h.uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(w))
        }
    }
    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
    logs.take(20).forEach { log ->
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Text(log.date.format(fmt), style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f))
            listOf(log.mood, log.fog, log.energy, log.focus).forEach { v ->
                val color = when {
                    v <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                    v >= 7 -> NdGreen
                    v <= 3 -> NdRed
                    else   -> MaterialTheme.colorScheme.onSurface
                }
                Text(if (v > 0) v.toString() else "–",
                    style = MaterialTheme.typography.bodySmall, color = color, modifier = Modifier.weight(1f))
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

// ── Compound impact row ───────────────────────────────────────────────────────
@Composable
fun CompoundImpactRow(impact: CompoundImpactScore) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(NdOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(impact.compoundName.take(2).uppercase(),
                    style = MaterialTheme.typography.labelSmall, color = NdOrange, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(impact.compoundName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text("${impact.usageCount}x used", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        val impactColor = if (impact.impactMood >= 0) NdGreen else NdRed
        Box(
            Modifier.clip(RoundedCornerShape(6.dp)).background(impactColor.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(
                "%+.1f mood".format(impact.impactMood),
                style = MaterialTheme.typography.labelSmall, color = impactColor, fontWeight = FontWeight.Bold,
            )
        }
    }
}
