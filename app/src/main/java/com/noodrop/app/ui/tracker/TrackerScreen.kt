package com.noodrop.app.ui.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noodrop.app.ui.common.*
import com.noodrop.app.ui.theme.*
import com.noodrop.app.util.SoundManager

@Composable
fun TrackerScreen(vm: TrackerViewModel = hiltViewModel()) {
    val s     = vm.state.collectAsState().value
    val snack = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(s.toast) { s.toast?.let { snack.showSnackbar(it); vm.clearToast() } }

    Scaffold(
        snackbarHost   = { SnackbarHost(snack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Column {
                Text(
                    "Daily Tracker",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    s.dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ── Checklist ─────────────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Text("Protocol Checklist", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        OrangeChip("${s.checked.size}/${s.stack.size}")
                    }

                    if (s.stack.isEmpty()) {
                        EmptyState("🧬", "Build your stack first")
                    } else {
                        s.stack.forEach { entry ->
                            val isChecked = entry.compoundName in s.checked
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isChecked) NdGreen.copy(alpha = 0.07f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable {
                                        // Play check sound on check (not uncheck)
                                        if (entry.compoundName !in s.checked) SoundManager.playCheck()
                                        vm.toggleCheck(entry.compoundName)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 11.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(if (isChecked) NdGreen else Color.Transparent)
                                            .border(2.dp, if (isChecked) NdGreen else MaterialTheme.colorScheme.outline, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (isChecked) Text("✓", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text(entry.compoundName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        Text(entry.dose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                NdChip(entry.timing.label)
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        OutlinedButton(
                            onClick  = {
                                SoundManager.playLog()
                                vm.saveCheckIn()
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape    = RoundedCornerShape(10.dp),
                            border   = androidx.compose.foundation.BorderStroke(1.5.dp, NdOrange),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = NdOrange),
                        ) {
                            Text("Save Check-in", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // ── Metrics section ───────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("How do you feel today?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))

                    TgMetricSlider("😊  Mood",      s.mood,   vm::setMood,   hint = null)
                    TgMetricSlider("🌫️  Brain Fog", s.fog,    vm::setFog,    hint = "1 = Crystal clear · 10 = Heavy fog")
                    TgMetricSlider("⚡  Energy",    s.energy, vm::setEnergy, hint = null)
                    TgMetricSlider("💪  Health",    s.health, vm::setHealth, hint = null)
                    TgMetricSlider("🎯  Focus",     s.focus,  vm::setFocus,  hint = null)

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value         = s.notes,
                        onValueChange = vm::setNotes,
                        label         = { Text("Notes") },
                        placeholder   = { Text("Observations, side effects...") },
                        modifier      = Modifier.fillMaxWidth().heightIn(min = 72.dp),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = NdOrange,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                    )

                    Spacer(Modifier.height(12.dp))

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(NdOrange, Color(0xFFFF8C42))))
                            .clickable {
                                SoundManager.playLog()
                                vm.logDay()
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Log Today →",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TgMetricSlider(
    label:         String,
    value:         Float,
    onValueChange: (Float) -> Unit,
    hint:          String?,
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NdOrange.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    value.toInt().toString(),
                    style      = MaterialTheme.typography.labelLarge,
                    color      = NdOrange,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Slider(
            value         = value,
            onValueChange = onValueChange,
            valueRange    = 1f..10f,
            steps         = 8,
            modifier      = Modifier.fillMaxWidth(),
            colors        = SliderDefaults.colors(
                thumbColor         = NdOrange,
                activeTrackColor   = NdOrange,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
        hint?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
