package com.noodrop.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noodrop.app.data.model.AuthState
import com.noodrop.app.data.repository.NoodropRepository
import com.noodrop.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── State & ViewModel ─────────────────────────────────────────────────────────
data class ProfileState(
    val email: String           = "",
    val uid: String             = "",
    val totalDaysLogged: Int    = 0,
    val currentStreak: Int      = 0,
    val stackSize: Int          = 0,
    val statsLoading: Boolean   = true,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: NoodropRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                combine(
                    repo.authState,
                    repo.logsFlow(365),
                    repo.stackFlow(),
                ) { auth, logs, stack ->
                    val totalLogged = logs.count { it.mood > 0 || it.fog > 0 }
                    val streak      = repo.computeStreak(logs, stack.size)
                    Triple(auth, totalLogged, Pair(streak.currentStreak, stack.size))
                }.collect { (auth, totalLogged, streakStack) ->
                    val (streakVal, stackSize) = streakStack
                    when (auth) {
                        is AuthState.Authenticated -> _state.update {
                            it.copy(
                                email           = auth.email ?: "Unknown",
                                uid             = auth.uid,
                                totalDaysLogged = totalLogged,
                                currentStreak   = streakVal,
                                stackSize       = stackSize,
                                statsLoading    = false,
                            )
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) { /* non-fatal */ }
        }
    }

    fun signOut() = repo.signOut()
}

// ── Sheet ─────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheet(
    onClose: () -> Unit,
    onOpenSubscription: () -> Unit = {},
    vm: ProfileViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onClose,
        modifier         = Modifier.fillMaxHeight(0.88f),
        containerColor   = MaterialTheme.colorScheme.surface,
        dragHandle       = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Hero header ───────────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                NdOrange.copy(alpha = 0.15f),
                                Color.Transparent,
                            )
                        )
                    ),
            ) {
                IconButton(
                    onClick  = onClose,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                ) {
                    Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Avatar
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(NdOrange.copy(alpha = 0.3f), NdOrange.copy(alpha = 0.1f)))
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            s.email.take(1).uppercase(),
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color      = NdOrange,
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(s.email, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Member · ID ${s.uid.take(6).uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Column(
                Modifier.padding(horizontal = 16.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // ── Stats row (Dropset style) ─────────────────────────────────
                if (!s.statsLoading) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ProfileStatCard("🗓", "Days Logged",  s.totalDaysLogged.toString(), Modifier.weight(1f))
                        ProfileStatCard("🔥", "Streak",       "${s.currentStreak}d",        Modifier.weight(1f))
                        ProfileStatCard("🧬", "Compounds",    s.stackSize.toString(),        Modifier.weight(1f))
                    }
                } else {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NdOrange, modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(Modifier.height(4.dp))

                // ── Settings section ──────────────────────────────────────────
                Text(
                    "APP",
                    style    = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    SettingsRow(emoji = "📱", title = "Version", value = "1.0.0")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.padding(start = 52.dp))
                    SettingsRow(emoji = "🔬", title = "About", value = "Noodrop")
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.padding(start = 52.dp))
                    SettingsRow(emoji = "📋", title = "Privacy Policy", value = "→")
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    "ACCOUNT",
                    style    = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )

                // ── Subscription ─────────────────────────────────────────────
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onOpenSubscription() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                    .background(NdOrange.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) { Text("✦", fontSize = 16.sp, color = NdOrange) }
                            Text("Premium", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        }
                        Text("→", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Sign out
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { vm.signOut() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                    .background(NdRed.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) { Text("👋", fontSize = 16.sp) }
                            Text("Sign Out", style = MaterialTheme.typography.bodyMedium, color = NdRed)
                        }
                        Text("→", style = MaterialTheme.typography.bodyMedium, color = NdRed)
                    }
                }
            }
        }
    }
}

// ── Profile stat card ─────────────────────────────────────────────────────────
@Composable
private fun ProfileStatCard(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Settings row ──────────────────────────────────────────────────────────────
@Composable
private fun SettingsRow(emoji: String, title: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                    .background(NdOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) { Text(emoji, fontSize = 16.sp) }
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
