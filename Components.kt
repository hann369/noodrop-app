package com.noodrop.app.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.noodrop.app.data.model.DayStatus
import com.noodrop.app.ui.theme.*
import kotlinx.coroutines.delay

// - Card -
@Composable
fun NdCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) = Card(
    modifier  = modifier.fillMaxWidth(),
    shape     = RoundedCornerShape(18.dp),
    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
) { Column(Modifier.padding(18.dp), content = content) }

// - Section label -
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) = Text(
    text.uppercase(),
    style  = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
    color  = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = modifier.padding(bottom = 10.dp),
)

// - Stat card -
@Composable
fun StatCard(
    label: String,
    value: String,
    sub: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) = Card(
    modifier  = modifier,
    shape     = RoundedCornerShape(16.dp),
    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(2.dp),
) {
    Column(Modifier.padding(14.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(5.dp))
        Text(value, style = MaterialTheme.typography.displaySmall, color = valueColor)
        Spacer(Modifier.height(2.dp))
        Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// - Checklist item -
@Composable
fun ChecklistRow(
    name: String,
    dose: String,
    timing: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val border = if (checked) NdGreen else MaterialTheme.colorScheme.outline
    val bg     = if (checked) NdGreen.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, border, RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (checked) NdGreen else Color.Transparent)
                .border(2.dp, if (checked) NdGreen else MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) { if (checked) Text("v", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold) }

        Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(dose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        NdChip(timing)
    }
}

// - Chips -
@Composable
fun NdChip(
    text: String,
    bg: Color = MaterialTheme.colorScheme.surfaceVariant,
    fg: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) = Box(
    Modifier
        .clip(RoundedCornerShape(50.dp))
        .background(bg)
        .padding(horizontal = 8.dp, vertical = 3.dp),
) { Text(text, style = MaterialTheme.typography.labelSmall, color = fg) }

@Composable
fun OrangeChip(text: String) = NdChip(text, NdOrange.copy(.12f), NdOrange)

@Composable
fun GreenChip(text: String) = NdChip(text, NdGreen.copy(.12f), NdGreen)

@Composable
fun NdFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Box(
    modifier
        .clip(RoundedCornerShape(50.dp))
        .background(
            if (selected) NdOrange.copy(0.2f) else MaterialTheme.colorScheme.surfaceVariant
        )
        .clickable(onClick = onClick)
        .border(
            width = if (selected) 1.5.dp else 0.dp,
            color = if (selected) NdOrange else Color.Transparent,
            shape = RoundedCornerShape(50.dp)
        )
        .padding(horizontal = 12.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center,
) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = if (selected) NdOrange else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// - Buttons -
@Composable
fun NdButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    Button(
        onClick  = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        enabled  = enabled,
        shape    = RoundedCornerShape(50.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = NdOrange),
        elevation = ButtonDefaults.buttonElevation(4.dp),
    ) { Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White) }

@Composable
fun NdOutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) =
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier.height(44.dp),
        shape    = RoundedCornerShape(50.dp),
        border   = BorderStroke(1.5.dp, NdOrange),
        colors   = ButtonDefaults.outlinedButtonColors(contentColor = NdOrange),
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }

// - Metric slider -
@Composable
fun MetricSlider(label: String, value: Float, onValueChange: (Float) -> Unit, hint: String? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(value.toInt().toString(), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = NdOrange)
        }
        Slider(
            value = value, onValueChange = onValueChange,
            valueRange = 1f..10f, steps = 8,
            colors = SliderDefaults.colors(thumbColor = NdOrange, activeTrackColor = NdOrange),
        )
        hint?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

// - Streak dot -
@Composable
fun StreakDot(status: DayStatus) {
    val color = when (status) {
        DayStatus.DONE    -> NdOrange
        DayStatus.MISSED  -> NdRed.copy(alpha = 0.35f)
        DayStatus.NO_DATA -> MaterialTheme.colorScheme.surfaceVariant
    }
    Box(Modifier.size(14.dp).clip(RoundedCornerShape(3.dp)).background(color))
}

// - Empty state -
@Composable
fun EmptyState(icon: String, message: String) =
    Column(
        Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(icon, fontSize = 38.sp)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }

// Enhanced Empty State with Actions
@Composable
fun EnhancedEmptyState(
    icon: String,
    title: String,
    description: String,
    primaryAction: Pair<String, () -> Unit>? = null,
    secondaryAction: Pair<String, () -> Unit>? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Animated Icon
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NdOrange.copy(alpha = 0.2f),
                            NdOrange.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                icon,
                style = MaterialTheme.typography.displayMedium,
                color = NdOrange,
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))

        // Actions
        if (primaryAction != null) {
            NdButton(
                primaryAction.first,
                onClick = primaryAction.second,
                modifier = Modifier.fillMaxWidth(0.8f),
            )
        }

        if (secondaryAction != null) {
            Spacer(Modifier.height(12.dp))
            NdOutlineButton(
                secondaryAction.first,
                onClick = secondaryAction.second,
                modifier = Modifier.fillMaxWidth(0.8f),
            )
        }
    }
}

// Skeleton Loading Component
@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            // Title skeleton
            Box(
                Modifier
                    .fillMaxWidth(0.6f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = alpha))
            )

            Spacer(Modifier.height(12.dp))

            // Description skeleton
            Box(
                Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = alpha))
            )

            Spacer(Modifier.height(8.dp))

            Box(
                Modifier
                    .fillMaxWidth(0.4f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = alpha))
            )

            Spacer(Modifier.height(16.dp))

            // Chips skeleton
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Box(
                        Modifier
                            .width(60.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

// Error State with Retry
@Composable
fun ErrorState(
    title: String = "Something went wrong",
    message: String = "Please try again",
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "⚠️",
            style = MaterialTheme.typography.displayLarge,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (onRetry != null) {
            Spacer(Modifier.height(24.dp))
            NdButton("Try Again", onClick = onRetry)
        }
    }
}

// Success Animation
@Composable
fun SuccessAnimation(
    message: String,
    onComplete: () -> Unit = {},
) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        visible = false
        onComplete()
    }

    if (visible) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Column(
                Modifier
                    .scale(scale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "✅",
                    style = MaterialTheme.typography.displayLarge,
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    message,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// Loading Overlay
@Composable
fun LoadingOverlay(
    message: String = "Loading...",
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(color = NdOrange)

            Spacer(Modifier.height(16.dp))

            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
