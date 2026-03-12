package com.noodrop.app.ui.profile

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noodrop.app.data.model.AuthState
import com.noodrop.app.data.repository.NoodropRepository
import com.noodrop.app.ui.theme.*
import com.noodrop.app.util.*
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
    val s       by vm.state.collectAsState()
    val context = LocalContext.current

    // ── Notification prefs ────────────────────────────────────────────────────
    val prefs       = remember { context.getSharedPreferences(PREFS_NOTIF, android.content.Context.MODE_PRIVATE) }
    var notifOn     by remember { mutableStateOf(prefs.getBoolean(KEY_NOTIF_ON, false)) }
    var notifHour   by remember { mutableIntStateOf(prefs.getInt(KEY_NOTIF_HOUR, 9)) }
    var notifMinute by remember { mutableIntStateOf(prefs.getInt(KEY_NOTIF_MIN, 0)) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Android 13+ POST_NOTIFICATIONS permission
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            notifOn = true
            scheduleDailyReminder(context, notifHour, notifMinute)
        }
    }

    fun toggleNotif(on: Boolean) {
        if (on) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                notifOn = true
                scheduleDailyReminder(context, notifHour, notifMinute)
            }
        } else {
            notifOn = false
            cancelDailyReminder(context)
        }
    }

    // ── Time Picker Dialog ────────────────────────────────────────────────────
    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour   = notifHour,
            initialMinute = notifMinute,
            is24Hour      = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title   = { Text("Reminder time") },
            text    = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    notifHour   = timeState.hour
                    notifMinute = timeState.minute
                    showTimePicker = false
                    if (notifOn) scheduleDailyReminder(context, notifHour, notifMinute)
                }) { Text("OK", color = NdOrange) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
        )
    }

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
                        Brush.verticalGradient(listOf(NdOrange.copy(alpha = 0.15f), Color.Transparent))
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
                // ── Stats row ─────────────────────────────────────────────────
                if (!s.statsLoading) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

                // ── APP section ───────────────────────────────────────────────
                SectionLabel("APP")

                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    SettingsRow(emoji = "📱", title = "Version", value = "1.0.0")
                    RowDivider()
                    SettingsRow(emoji = "🔬", title = "About", value = "Noodrop")
                    RowDivider()
                    SettingsRow(emoji = "📋", title = "Privacy Policy", value = "→")
                }

                Spacer(Modifier.height(4.dp))

                // ── NOTIFICATIONS section ─────────────────────────────────────
                SectionLabel("NOTIFICATIONS")

                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    // Toggle row
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            IconBox("🔔", NdOrange)
                            Text("Daily Reminder", style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked         = notifOn,
                            onCheckedChange = { toggleNotif(it) },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor  = Color.White,
                                checkedTrackColor  = NdOrange,
                            ),
                        )
                    }

                    // Time picker row (only when enabled)
                    if (notifOn) {
                        RowDivider()
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { showTimePicker = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                            ) {
                                IconBox("⏰", NdOrange)
                                Text("Reminder Time", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                "%02d:%02d".format(notifHour, notifMinute),
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = NdOrange,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // ── ACCOUNT section ───────────────────────────────────────────
                SectionLabel("ACCOUNT")

                // Premium row
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
                            IconBox("✦", NdOrange)
                            Text("Premium", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Text("→", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Sign out row
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

// ── Small helpers ─────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style    = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 52.dp),
    )
}

@Composable
private fun IconBox(emoji: String, tint: Color) {
    Box(
        Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) { Text(emoji, fontSize = 16.sp, color = tint) }
}

@Composable
private fun ProfileStatCard(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier.clip(RoundedCornerShape(12.dp))
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

@Composable
private fun SettingsRow(emoji: String, title: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(NdOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) { Text(emoji, fontSize = 16.sp) }
            Text(title, style = MaterialTheme.typography.bodyMedium)
        }
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
