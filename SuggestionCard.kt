package com.noodrop.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noodrop.app.ui.dashboard.CompoundSuggestion
import com.noodrop.app.ui.theme.NdOrange

@Composable
fun SuggestionCard(
    suggestion: CompoundSuggestion,
    onViewDetails: () -> Unit,
    onAddToStack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NdCard(modifier = modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "💡",
                            modifier = Modifier.padding(end = 6.dp),
                        )
                        Text(
                            "Smart Suggestion",
                            style = MaterialTheme.typography.labelSmall,
                            color = NdOrange,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        suggestion.compound.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                // Confidence badge
                Box(
                    Modifier
                        .background(NdOrange.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${(suggestion.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = NdOrange,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Reason
            Text(
                suggestion.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Category + dose
            Row(
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        suggestion.compound.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                Text(
                    suggestion.compound.defaultDose,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Action buttons
            Row(
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onAddToStack,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NdOrange,
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        "Add to Stack",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        "Learn More",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

