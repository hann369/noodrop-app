package com.noodrop.app.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noodrop.app.data.model.AuthState
import com.noodrop.app.data.repository.NoodropRepository
import com.noodrop.app.ui.common.*
import com.noodrop.app.ui.theme.NdOrange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// - ViewModel -
data class ProfileState(
    val email: String = "",
    val uid: String = "",
    val totalDaysLogged: Int = 0,
    val currentStreak: Int = 0,
    val stackSize: Int = 0,
    val statsLoading: Boolean = true,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: NoodropRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.authState,
                repo.logsFlow(365),
                repo.stackFlow(),
            ) { auth, logs, stack ->
                val totalLogged = logs.count { it.mood > 0 || it.fog > 0 }
                val streak = repo.computeStreak(logs, stack.size)

                when (auth) {
                    is AuthState.Authenticated -> {
                        _state.update {
                            it.copy(
                                email = auth.email ?: "Unknown",
                                uid = auth.uid,
                                totalDaysLogged = totalLogged,
                                currentStreak = streak.current,
                                stackSize = stack.size,
                                statsLoading = false,
                            )
                        }
                    }
                    else -> {}
                }
            }.collect()
        }
    }

    fun signOut() = repo.signOut()
}

// - Sheet -
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheet(
    onClose: () -> Unit,
    vm: ProfileViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onClose,
        modifier = Modifier.fillMaxHeight(0.85f),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            // Avatar
            Box(
                Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(NdOrange.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("👤", style = MaterialTheme.typography.displaySmall)
            }

            Spacer(Modifier.height(16.dp))

            // Email
            Text(
                s.email,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "ID: ${s.uid.take(8)}...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))

            // Stats Grid
            if (s.statsLoading) {
                CircularProgressIndicator(color = NdOrange)
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatBox("Days Logged", s.totalDaysLogged.toString(), Modifier.weight(1f))
                    StatBox("Streak", s.currentStreak.toString(), Modifier.weight(1f))
                    StatBox("Compounds", s.stackSize.toString(), Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Info Card
            NdCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "About Noodrop",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Version 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Research-driven nootropics optimization platform",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Sign Out Button
            NdButton(
                "Sign Out 👋",
                onClick = { vm.signOut() },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                RoundedCornerShape(12.dp),
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

