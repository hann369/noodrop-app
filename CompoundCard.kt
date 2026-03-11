package com.noodrop.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noodrop.app.data.model.Compound
import com.noodrop.app.ui.theme.NdOrange

@Composable
fun CompoundCard(
    compound: Compound,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NdCard(
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        compound.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        compound.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    compound.defaultDose,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = NdOrange,
                )
            }

            // Description (truncated)
            if (compound.description.isNotEmpty()) {
                Text(
                    compound.description.take(100) + if (compound.description.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }

            // Benefits as small chips
            if (compound.benefits.isNotEmpty()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    compound.benefits.take(3).forEach { benefit ->
                        Box(
                            Modifier
                                .background(
                                    NdOrange.copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                benefit,
                                style = MaterialTheme.typography.labelSmall,
                                color = NdOrange,
                            )
                        }
                    }
                }
            }

            // Timing hint
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("⏰", style = MaterialTheme.typography.labelSmall)
                Text(
                    compound.optimalTiming,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            // Tap to learn more
            Text(
                "Tap to learn more →",
                style = MaterialTheme.typography.labelSmall,
                color = NdOrange,
            )
        }
    }
}

