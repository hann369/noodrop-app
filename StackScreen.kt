package com.noodrop.app.ui.stack

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noodrop.app.data.model.*
import com.noodrop.app.ui.common.*
import com.noodrop.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StackScreen(vm: StackViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(s.toastMsg) { s.toastMsg?.let { snack.showSnackbar(it); vm.clearToast() } }

    if (s.showSheet) {
        ModalBottomSheet(onDismissRequest = vm::closeSheet, containerColor = MaterialTheme.colorScheme.surface) {
            AddCompoundSheet(s, vm)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snack) }, containerColor = MaterialTheme.colorScheme.background) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp).padding(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
                Text("My Stack", style = MaterialTheme.typography.displaySmall)
                NdOutlineButton("+ Add", vm::openSheet)
            }

            NdCard {
                SectionLabel("Active Compounds")
                if (s.stack.isEmpty()) {
                    EnhancedEmptyState(
                        icon = "🧬",
                        title = "Your Stack is Empty",
                        description = "Add nootropic compounds to build your personalized protocol. Start with evidence-based recommendations.",
                        primaryAction = "Add Compound" to vm::openSheet,
                        secondaryAction = "Browse Presets" to { /* Scroll to presets */ },
                    )
                } else {
                    s.stack.forEach { entry ->
                        key(entry.id) {  // Add key for better performance
                            SwipeableStackRow(
                                entry = entry,
                                onRemove = { vm.removeCompound(entry) }
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }

            NdCard {
                SectionLabel("Quick Load - Presets")
                if (ProtocolData.all.filter { it.presetEntries.isNotEmpty() }.isEmpty()) {
                    EnhancedEmptyState(
                        icon = "📋",
                        title = "No Presets Available",
                        description = "Check back later for new evidence-based protocol templates.",
                    )
                } else {
                    ProtocolData.all.filter { it.presetEntries.isNotEmpty() }.forEach { proto ->
                        PresetRow(proto, onLoad = { vm.loadPreset(proto) })
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }

            var noteText by remember(s.notes) { mutableStateOf(s.notes) }
            NdCard {
                SectionLabel("Protocol Notes")
                OutlinedTextField(
                    value = noteText, onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    placeholder = { Text("Track changes, observations, effects...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NdOrange),
                )
                Spacer(Modifier.height(10.dp))
                NdButton("Save Notes", onClick = { vm.saveNote(noteText) })
            }

            NdCard {
                SectionLabel("Stack Timeline")
                if (s.timeline.isEmpty()) {
                    Text("Stack changes appear here.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val recentTimeline = s.timeline.takeLast(3)  // Only show last 3
                    recentTimeline.forEachIndexed { i, entry ->
                        Row(
                            Modifier.padding(bottom = if (i < recentTimeline.lastIndex) 16.dp else 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.size(10.dp).clip(CircleShape).background(NdOrange))
                                if (i < recentTimeline.lastIndex) {
                                    Box(Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outline))
                                }
                            }
                            Column {
                                Text(entry.dateLabel, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(entry.text, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StackRow(entry: StackEntry, onRemove: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(entry.compoundName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        NdChip(entry.dose)
        Text(entry.timing.label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PresetRow(p: Protocol, onLoad: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable(onClick = onLoad)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(p.icon, style = MaterialTheme.typography.headlineMedium)
        Column(Modifier.weight(1f)) {
            Text(p.name, style = MaterialTheme.typography.titleMedium)
            Text(p.compounds.take(3).joinToString(" \u00B7 "), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        when (p.status) {
            ProtocolStatus.FREE        -> GreenChip("Free")
            ProtocolStatus.PAID        -> OrangeChip(p.price)
            ProtocolStatus.COMING_SOON -> NdChip("Soon")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCompoundSheet(s: StackState, vm: StackViewModel) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Add Compound", style = MaterialTheme.typography.headlineMedium)

        var compoundExp by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(compoundExp, { compoundExp = it }) {
            OutlinedTextField(
                value = s.selectedCompound.name, onValueChange = {}, readOnly = true,
                label = { Text("Compound") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(compoundExp) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NdOrange),
            )
            ExposedDropdownMenu(compoundExp, { compoundExp = false }) {
                CompoundData.all.forEach { c ->
                    DropdownMenuItem(
                        text = { Text("${c.name}  (${c.category})") },
                        onClick = { vm.selectCompound(c); compoundExp = false },
                    )
                }
            }
        }

        OutlinedTextField(s.doseInput, vm::updateDose, label = { Text("Dose") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NdOrange))

        var timingExp by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(timingExp, { timingExp = it }) {
            OutlinedTextField(
                value = s.timingInput.label, onValueChange = {}, readOnly = true,
                label = { Text("Timing") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(timingExp) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NdOrange),
            )
            ExposedDropdownMenu(timingExp, { timingExp = false }) {
                Timing.entries.forEach { t ->
                    DropdownMenuItem(text = { Text(t.label) }, onClick = { vm.updateTiming(t); timingExp = false })
                }
            }
        }

        NdButton("Add to Stack", onClick = vm::addCompound)
        Spacer(Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableStackRow(
    entry: StackEntry,
    onRemove: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            // Background when swiping
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                else -> Color.Transparent
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Delete",
                    tint = Color.White,
                )
            }
        },
        content = {
            StackRow(
                entry = entry,
                onRemove = onRemove
            )
        }
    )
}
