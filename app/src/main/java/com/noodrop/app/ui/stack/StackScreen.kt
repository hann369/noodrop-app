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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noodrop.app.data.model.*
import com.noodrop.app.ui.common.*
import com.noodrop.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StackScreen(vm: StackViewModel = hiltViewModel()) {
    val s     = vm.state.collectAsState().value
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(s.toastMsg) { s.toastMsg?.let { snack.showSnackbar(it); vm.clearToast() } }

    if (s.showSheet) {
        ModalBottomSheet(
            onDismissRequest = vm::closeSheet,
            containerColor   = MaterialTheme.colorScheme.surface,
        ) {
            AddCompoundSheet(s, vm)
        }
    }

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
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column {
                    Text("My Stack", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${s.stack.size} compounds active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NdOrange)
                        .clickable(onClick = vm::openSheet)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Text(
                        "+ Add",
                        style      = MaterialTheme.typography.labelLarge,
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // ── Stat row (Dropset style) ───────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StackStatCard(
                    label    = "COMPOUNDS",
                    value    = s.stack.size.toString(),
                    sub      = "in stack",
                    modifier = Modifier.weight(1f),
                )
                StackStatCard(
                    label    = "MORNING",
                    value    = s.stack.count { it.timing == Timing.MORNING }.toString(),
                    sub      = "compounds",
                    modifier = Modifier.weight(1f),
                )
                StackStatCard(
                    label    = "EVENING",
                    value    = s.stack.count { it.timing == Timing.EVENING }.toString(),
                    sub      = "compounds",
                    modifier = Modifier.weight(1f),
                )
            }

            // ── Active Stack ──────────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Active Compounds", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))

                    if (s.stack.isEmpty()) {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("🧬", fontSize = 36.sp)
                            Text("Your Stack is Empty", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Add nootropic compounds to build your personalized protocol",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NdOrange.copy(alpha = 0.1f))
                                    .border(1.dp, NdOrange.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .clickable(onClick = vm::openSheet)
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                            ) {
                                Text("+ Add First Compound", style = MaterialTheme.typography.labelMedium, color = NdOrange)
                            }
                        }
                    } else {
                        s.stack.forEach { entry ->
                            key(entry.id) {
                                SwipeableStackRow(entry = entry, onRemove = { vm.removeCompound(entry) })
                            }
                        }
                    }
                }
            }

            // ── Quick Load Presets ────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Quick Load — Presets", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                    val freeProtocols = ProtocolData.all.filter {
                        it.presetEntries.isNotEmpty() && it.status == ProtocolStatus.FREE
                    }

                    if (freeProtocols.isEmpty()) {
                        EmptyState("📋", "No presets available")
                    } else {
                        freeProtocols.forEach { protocol ->
                            PresetRow(
                                protocol = protocol,
                                onLoad   = { vm.loadPreset(protocol) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Stat card ─────────────────────────────────────────────────────────────────
@Composable
private fun StackStatCard(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(3.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Preset Row ────────────────────────────────────────────────────────────────
@Composable
private fun PresetRow(protocol: Protocol, onLoad: () -> Unit) {
    val accentColor = when (protocol.accent) {
        ProtocolAccent.ORANGE -> NdOrange
        ProtocolAccent.GREEN  -> NdGreen
        ProtocolAccent.BLUE   -> NdBlue
        ProtocolAccent.PURPLE -> NdPurple
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) { Text(protocol.icon, fontSize = 18.sp) }
            Column {
                Text(protocol.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${protocol.presetEntries.size} compounds",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.12f))
                .clickable(onClick = onLoad)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text("Load", style = MaterialTheme.typography.labelMedium, color = accentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Add Compound Sheet ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCompoundSheet(s: StackState, vm: StackViewModel) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Add Compound", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        var compoundExp by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(compoundExp, { compoundExp = it }) {
            OutlinedTextField(
                value         = s.selectedCompound.name,
                onValueChange = {},
                readOnly      = true,
                label         = { Text("Compound") },
                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(compoundExp) },
                modifier      = Modifier.menuAnchor().fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = NdOrange),
            )
            ExposedDropdownMenu(compoundExp, { compoundExp = false }) {
                CompoundData.all.forEach { c ->
                    DropdownMenuItem(
                        text    = { Text("${c.name}  (${c.category})") },
                        onClick = { vm.selectCompound(c); compoundExp = false },
                    )
                }
            }
        }

        OutlinedTextField(
            value         = s.doseInput,
            onValueChange = vm::updateDose,
            label         = { Text("Dose") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = NdOrange),
        )

        var timingExp by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(timingExp, { timingExp = it }) {
            OutlinedTextField(
                value         = s.timingInput.label,
                onValueChange = {},
                readOnly      = true,
                label         = { Text("Timing") },
                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(timingExp) },
                modifier      = Modifier.menuAnchor().fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = NdOrange),
            )
            ExposedDropdownMenu(timingExp, { timingExp = false }) {
                Timing.entries.forEach { t ->
                    DropdownMenuItem(
                        text    = { Text(t.label) },
                        onClick = { vm.updateTiming(t); timingExp = false },
                    )
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(NdOrange, Color(0xFFFF8C42))))
                .clickable(onClick = vm::addCompound)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Add to Stack", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ── Swipeable Stack Row ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableStackRow(entry: StackEntry, onRemove: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onRemove(); true }
            else false
        }
    )

    SwipeToDismissBox(
        state             = dismissState,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White)
            }
        },
        content = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).background(NdOrange.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            entry.compoundName.take(2).uppercase(),
                            style      = MaterialTheme.typography.labelSmall,
                            color      = NdOrange,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Column {
                        Text(entry.compoundName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(entry.dose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                NdChip(entry.timing.label)
            }
        }
    )
}
