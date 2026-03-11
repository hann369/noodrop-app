package com.noodrop.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noodrop.app.data.model.Compound
import com.noodrop.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompoundDetailSheet(
    compound: Compound,
    onDismiss: () -> Unit,
    onAddToStack: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.95f),
        scrimColor = Color.Black.copy(alpha = 0.32f),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
        ) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        compound.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        compound.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            // Description
            if (compound.description.isNotEmpty()) {
                NdCard {
                    Text(
                        compound.description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Benefits Chips
            if (compound.benefits.isNotEmpty()) {
                SectionLabel("Key Benefits")
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    compound.benefits.forEach { benefit ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "✓ $benefit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Dosage & Timing
            SectionLabel("Standard Protocol")
            Spacer(Modifier.height(8.dp))
            NdCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DosageRow("Default Dose", compound.defaultDose)
                    DosageRow("Optimal Timing", compound.optimalTiming)
                    if (compound.halfLife != "Unknown") {
                        DosageRow("Half-Life", compound.halfLife)
                    }
                    if (compound.bioavailability != "Unknown") {
                        DosageRow("Bioavailability", compound.bioavailability)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Food Interactions
            if (compound.foodInteraction.isNotEmpty()) {
                SectionLabel("Absorption Tips")
                Spacer(Modifier.height(6.dp))
                NdCard {
                    Text(
                        "💡 ${compound.foodInteraction}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Synergies
            if (compound.synergies.isNotEmpty()) {
                SectionLabel("Works Well With")
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    compound.synergies.forEach { synergy ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    NdOrange.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "⚗️ $synergy",
                                style = MaterialTheme.typography.bodySmall,
                                color = NdOrange,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Safety Notes
            if (compound.safetyNotes.isNotEmpty()) {
                SectionLabel("Safety Notes")
                Spacer(Modifier.height(6.dp))
                NdCard {
                    Text(
                        "⚠️ ${compound.safetyNotes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Research Links
            if (compound.researchLinks.isNotEmpty()) {
                SectionLabel("Research")
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    compound.researchLinks.forEach { link ->
                        NdCard {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    link.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (link.authors.isNotEmpty()) {
                                    Text(
                                        link.authors + (if (link.year > 0) " (${link.year})" else ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    "View on PubMed →",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NdOrange,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Action Button
            NdButton(
                "Add to Stack →",
                onClick = onAddToStack,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DosageRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

