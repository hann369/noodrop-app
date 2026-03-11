package com.noodrop.app.ui.tracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noodrop.app.ui.common.*
import com.noodrop.app.ui.theme.NdOrange

@Composable
fun TrackerScreen(vm: TrackerViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(s.toast) { s.toast?.let { snack.showSnackbar(it); vm.clearToast() } }

    Scaffold(snackbarHost = { SnackbarHost(snack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp).padding(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column {
                Text("Daily Tracker", style = MaterialTheme.typography.displaySmall)
                Text(s.dateLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Checklist card
            NdCard {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    SectionLabel("Protocol Checklist")
                    OrangeChip("${s.checked.size}/${s.stack.size}")
                }
                Spacer(Modifier.height(4.dp))
                if (s.stack.isEmpty()) {
                    EmptyState("[Stack]", "Build your stack first")
                } else {
                    s.stack.forEach { entry ->
                        ChecklistRow(
                            name    = entry.compoundName,
                            dose    = entry.dose,
                            timing  = entry.timing.label,
                            checked = entry.compoundName in s.checked,
                            onClick = { vm.toggleCheck(entry.compoundName) },
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    NdOutlineButton("Save Check-in", vm::saveCheckIn, Modifier.fillMaxWidth())
                }
            }

            // Metrics card
            NdCard {
                SectionLabel("How do you feel today?")
                MetricSlider("?  Mood",      s.mood,   vm::setMood)
                MetricSlider("?  Brain Fog", s.fog,    vm::setFog,    "1 = Crystal clear . 10 = Heavy fog")
                MetricSlider("?  Energy",    s.energy, vm::setEnergy)
                MetricSlider("?  Health",    s.health, vm::setHealth)
                MetricSlider("?  Focus",     s.focus,  vm::setFocus)

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = s.notes, onValueChange = vm::setNotes,
                    label = { Text("Notes") },
                    placeholder = { Text("Observations, side effects...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NdOrange),
                )
                Spacer(Modifier.height(12.dp))
                NdButton("Log Today ?", vm::logDay)
            }
        }
    }
}
