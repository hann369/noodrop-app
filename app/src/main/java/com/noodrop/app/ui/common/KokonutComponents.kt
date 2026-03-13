package com.noodrop.app.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.noodrop.app.ui.theme.*

// ══════════════════════════════════════════════════════════════════════════════
// 1 ── LIQUID GLASS CARD
//      Frosted-glass card that sits over a coloured gradient background.
//      Use for: hero stack-progress card, daily summary, featured protocol.
//
//      How it works in Compose:
//        • Background Box draws blurred radial "blobs" via Canvas drawCircle
//          with a large BlurMaskFilter sigma — gives the soft colour field.
//        • Foreground card uses graphicsLayer { alpha / compositingStrategy }
//          + a semi-transparent white background to fake backdrop-blur.
//        • A white top-edge shimmer line (1dp) sells the glass refraction.
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    blobs: List<Pair<Color, Offset>> = listOf(
        NdOrange.copy(alpha = 0.55f) to Offset(30f, 20f),
        NdGreen.copy(alpha = 0.35f)  to Offset(260f, 110f),
        NdBlue.copy(alpha = 0.40f)   to Offset(180f, 10f),
    ),
    bgStart: Color = Color(0xFF1a1a2e),
    bgEnd:   Color = Color(0xFF533483),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(24.dp)),
    ) {
        // ── Blob background — drawn last-measured size ─────────────────────
        Canvas(Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(bgStart, bgEnd),
                    start  = Offset.Zero,
                    end    = Offset(size.width, size.height),
                )
            )
            blobs.forEach { (color, offset) ->
                val cx = (offset.x / 300f * size.width).coerceIn(0f, size.width)
                val cy = (offset.y / 160f * size.height).coerceIn(0f, size.height)
                val r  = size.width * 0.6f
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(color, Color.Transparent),
                        center = Offset(cx, cy),
                        radius = r,
                    ),
                    radius = r,
                    center = Offset(cx, cy),
                )
            }
        }

        // ── Glass panel ────────────────────────────────────────────────────
        Box(
            Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.09f))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.06f),
                        )
                    ),
                    shape = RoundedCornerShape(16.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.padding(14.dp), content = content)
        }
    }
}

// ── Stack Progress Glass Card ─────────────────────────────────────────────────
@Composable
fun StackProgressGlassCard(
    checkedCount: Int,
    totalCount:   Int,
    modifier:     Modifier = Modifier,
) {
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top label
            Text(
                "Today's Stack",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White.copy(alpha = 0.85f),
                maxLines   = 1,
            )

            // Center: animated progress ring
            Box(contentAlignment = Alignment.Center) {
                val animatedRing by animateFloatAsState(
                    targetValue   = checkedCount.toFloat() / totalCount.coerceAtLeast(1),
                    animationSpec = tween(900, easing = EaseOutCubic),
                    label         = "glass_ring",
                )
                androidx.compose.foundation.Canvas(Modifier.size(72.dp)) {
                    val sw = 6.dp.toPx()
                    val r  = (size.width - sw) / 2
                    drawCircle(
                        color  = Color.White.copy(alpha = 0.15f),
                        radius = r,
                        style  = androidx.compose.ui.graphics.drawscope.Stroke(width = sw),
                    )
                    if (animatedRing > 0f) {
                        drawArc(
                            color      = NdOrange,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedRing,
                            useCenter  = false,
                            topLeft    = androidx.compose.ui.geometry.Offset(sw / 2, sw / 2),
                            size       = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                            style      = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = sw,
                                cap   = androidx.compose.ui.graphics.StrokeCap.Round,
                            ),
                        )
                    }
                }
                Text(
                    checkedCount.toString(),
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = NdOrange,
                )
            }

            // Bottom: count text + progress bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    "$checkedCount/$totalCount taken",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.55f),
                )
                GlassProgressBar(
                    progress = checkedCount.toFloat() / totalCount.coerceAtLeast(1),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}


@Composable
private fun GlassProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(
        targetValue   = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = EaseOutCubic),
        label         = "glass_progress",
    )
    Box(
        modifier
            .height(4.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(Color.White.copy(alpha = 0.12f))
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(99.dp))
                .background(
                    Brush.horizontalGradient(listOf(NdOrange, Color(0xFFFF9A6C)))
                )
        )
    }
}


// ══════════════════════════════════════════════════════════════════════════════
// 2 ── ACTIVITY RINGS CARD  (Apple Watch style)
//      Three concentric animated arcs: Stack %, Streak %, Mood %.
//      Use for: Dashboard hero, Metrics screen overview.
//
//      How it works:
//        • Canvas draws three ring tracks + animated sweep arcs.
//        • animateFloatAsState drives each ring independently with staggered
//          delays so they animate in sequence — outer → middle → inner.
//        • Label rows beside the rings mirror the Apple Watch sidepanel.
// ══════════════════════════════════════════════════════════════════════════════

data class ActivityRing(
    val label:    String,
    val value:    String,   // display value e.g. "5"
    val total:    String,   // display total e.g. "/7"
    val progress: Float,    // 0f..1f
    val color:    Color,
)

@Composable
fun ActivityRingsCard(
    rings:    List<ActivityRing>,   // outer → inner (max 3)
    modifier: Modifier = Modifier,
) {
    val threeRings = rings.take(3)

    // Staggered animated progress values
    val delays = listOf(100, 250, 400)
    val animated = threeRings.mapIndexed { i, ring ->
        animateFloatAsState(
            targetValue   = ring.progress.coerceIn(0f, 1f),
            animationSpec = tween(
                durationMillis = 1000,
                delayMillis    = delays.getOrElse(i) { 0 },
                easing         = EaseOutCubic,
            ),
            label = "ring_$i",
        ).value
    }

    val cardBg = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Ring canvas ────────────────────────────────────────────────
            val ringSize = 110.dp
            Canvas(Modifier.size(ringSize)) {
                val cx = size.width / 2
                val cy = size.height / 2

                val radii        = listOf(46.dp.toPx(), 34.dp.toPx(), 22.dp.toPx())
                val strokeWidths = listOf(9.dp.toPx(), 8.dp.toPx(), 7.dp.toPx())

                threeRings.forEachIndexed { i, ring ->
                    val r  = radii[i]
                    val sw = strokeWidths[i]

                    drawCircle(
                        color       = ring.color.copy(alpha = 0.12f),
                        radius      = r,
                        center      = Offset(cx, cy),
                        style       = Stroke(width = sw),
                    )

                    val sweep = 360f * animated[i]
                    if (sweep > 0f) {
                        drawArc(
                            color      = ring.color,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter  = false,
                            topLeft    = Offset(cx - r, cy - r),
                            size       = Size(r * 2, r * 2),
                            style      = Stroke(width = sw, cap = StrokeCap.Round),
                        )
                    }
                }
            }

            // ── Labels ─────────────────────────────────────────────────────
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                threeRings.forEachIndexed { i, ring ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            ring.label.uppercase(),
                            style         = MaterialTheme.typography.labelSmall,
                            color         = ring.color.copy(alpha = 0.8f),
                            letterSpacing = 0.8.sp,
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                ring.value,
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color      = textPrimary,
                            )
                            Text(
                                ring.total,
                                style    = MaterialTheme.typography.bodySmall,
                                color    = textSecondary,
                                modifier = Modifier.padding(bottom = 2.dp),
                            )
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(ring.color.copy(alpha = 0.10f))
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animated[i])
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(ring.color)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Default rings for DashboardScreen ────────────────────────────────────────
fun defaultActivityRings(
    checkedCount: Int,
    stackSize:    Int,
    streak:       Int,
    streakGoal:   Int = 30,
    mood:         Int,
    moodStr:      String = mood.toString(),
) : List<ActivityRing> {
    val moodSet      = moodStr != "-" && moodStr.toIntOrNull() != null
    val moodDisplay  = if (moodSet) moodStr else "-"
    val moodProgress = if (moodSet) mood / 10f else 0f
    return listOf(
        ActivityRing(
            label    = "Stack",
            value    = checkedCount.toString(),
            total    = "/$stackSize taken",
            progress = checkedCount.toFloat() / stackSize.coerceAtLeast(1),
            color    = NdOrange,
        ),
        ActivityRing(
            label    = "Streak",
            value    = streak.toString(),
            total    = "/$streakGoal days",
            progress = streak.toFloat() / streakGoal,
            color    = NdGreen,
        ),
        ActivityRing(
            label    = "Mood",
            value    = moodDisplay,
            total    = if (moodSet) "/10 today" else "not logged",
            progress = moodProgress,
            color    = NdBlue,
        ),
    )
}


// ══════════════════════════════════════════════════════════════════════════════
// 3 ── FLIP PROTOCOL CARD
//      Front: protocol name, goal tags, "tap to see compounds" hint.
//      Back:  compound list + "Load into Stack" CTA.
//      Use for: LibraryScreen protocol cards, StackScreen preview.
//
//      How it works:
//        • A Boolean `flipped` state drives graphicsLayer { rotationY }.
//        • Front face rotates 0→90 (fades out) then Back face rotates -90→0
//          (fades in), but we use a single shared animatedRotation split at
//          90° to decide which face to show — zero overdraw.
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun FlipProtocolCard(
    name:        String,
    goal:        String,
    tags:        List<String>,
    compounds:   List<Pair<String, String>>,  // name to dose
    accentColor: Color,
    onLoadStack: () -> Unit,
    onTap:       () -> Unit = {},  // called on front-face tap → opens detail sheet
    modifier:    Modifier = Modifier,
) {
    var flipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue   = if (flipped) 180f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label         = "flip_rotation",
    )

    // Which face to show: front when rotation < 90, back when ≥ 90
    val showBack = rotation >= 90f
    // Correct the back face so it reads left-to-right after flip
    val backRotation = if (showBack) rotation - 180f else rotation

    Box(
        modifier
            .fillMaxWidth()
            .graphicsLayer { rotationY = if (showBack) backRotation else rotation; cameraDistance = 12f * density }
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        if (!showBack) {
            // ── Front ──────────────────────────────────────────────────────
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { onTap() }   // tap anywhere on front → detail sheet
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Category label
                Text(
                    "${goal.uppercase()} · PROTOCOL",
                    style         = MaterialTheme.typography.labelSmall,
                    color         = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp,
                )

                // Accent bar + name
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier
                            .width(3.dp)
                            .height(42.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                Brush.verticalGradient(listOf(accentColor, accentColor.copy(0.2f)))
                            )
                    )
                    Text(
                        name,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Tags
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEach { tag ->
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(accentColor.copy(alpha = 0.10f))
                                .border(0.5.dp, accentColor.copy(0.25f), RoundedCornerShape(99.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(tag, style = MaterialTheme.typography.labelSmall, color = accentColor)
                        }
                    }
                }

                // Bottom row: flip hint button (right-aligned)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = { flipped = true })
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text("↕", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "Compounds",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        } else {
            // ── Back ───────────────────────────────────────────────────────
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { flipped = false }  // tap anywhere on back → flip back
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "COMPOUNDS",
                    style         = MaterialTheme.typography.labelSmall,
                    color         = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.8.sp,
                )

                compounds.forEach { (compoundName, dose) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Text(
                            compoundName,
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color      = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            dose,
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(listOf(NdOrange, Color(0xFFFF8C42)))
                        )
                        .clickable {
                            flipped = false
                            onLoadStack()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Load into My Stack →",
                        style      = MaterialTheme.typography.labelLarge,
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
