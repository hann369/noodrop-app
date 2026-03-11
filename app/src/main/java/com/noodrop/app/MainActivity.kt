package com.noodrop.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.noodrop.app.data.ThemePreferences
import com.noodrop.app.data.model.AuthState
import com.noodrop.app.data.repository.NoodropRepository
import com.noodrop.app.ui.*
import com.noodrop.app.ui.auth.AuthScreen
import com.noodrop.app.ui.dashboard.DashboardScreen
import com.noodrop.app.ui.library.LibraryScreen
import com.noodrop.app.ui.metrics.MetricsScreen
import com.noodrop.app.ui.onboarding.OnboardingScreen
import com.noodrop.app.ui.stack.StackScreen
import com.noodrop.app.ui.theme.*
import com.noodrop.app.ui.tracker.TrackerScreen
import com.noodrop.app.ui.profile.ProfileSheet
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Root ViewModel ────────────────────────────────────────────────────────────
data class RootState(
    val authState: AuthState = AuthState.Loading,
    val isDark: Boolean      = false,
    val onboardingDone: Boolean = false,
)

@HiltViewModel
class RootViewModel @Inject constructor(
    private val repo:  NoodropRepository,
    private val prefs: ThemePreferences,
) : ViewModel() {
    private val _state = MutableStateFlow(RootState())
    val state: StateFlow<RootState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repo.authState, prefs.isDarkFlow) { auth, dark ->
                RootState(
                    authState      = auth,
                    isDark         = dark,
                    onboardingDone = _state.value.onboardingDone,
                )
            }.collect { _state.value = it }
        }
    }

    fun toggleDark()        = viewModelScope.launch { prefs.setDark(!_state.value.isDark) }
    fun signOut()           = repo.signOut()
    fun completeOnboarding() = _state.update { it.copy(onboardingDone = true) }
}

// ── Activity ──────────────────────────────────────────────────────────────────
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // FIX: Install splash screen BEFORE super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { NoodropRoot() }
    }
}

// ── Root composable ───────────────────────────────────────────────────────────
@Composable
fun NoodropRoot(vm: RootViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    NoodropTheme(darkTheme = state.isDark) {
        when (val auth = state.authState) {
            is AuthState.Loading -> SplashScreen()

            is AuthState.Unauthenticated -> {
                if (!state.onboardingDone) {
                    OnboardingScreen(onComplete = { vm.completeOnboarding() })
                } else {
                    AuthScreen()
                }
            }

            is AuthState.Authenticated -> MainApp(
                onToggleDark = vm::toggleDark,
                onSignOut    = vm::signOut,
                isDark       = state.isDark,
            )
        }
    }
}

// ── Main app ──────────────────────────────────────────────────────────────────
@Composable
fun MainApp(onToggleDark: () -> Unit, onSignOut: () -> Unit, isDark: Boolean) {
    val navController = rememberNavController()
    val currentEntry  by navController.currentBackStackEntryAsState()
    val currentRoute  = currentEntry?.destination?.route
    var showProfile   by remember { mutableStateOf(false) }

    if (showProfile) {
        ProfileSheet(onClose = { showProfile = false })
    }

    Scaffold(
        topBar = {
            NoodropTopBar(
                currentRoute = currentRoute,
                isDark       = isDark,
                onToggleDark = onToggleDark,
                onSignOut    = onSignOut,
                onProfileClick = { showProfile = true },
            )
        },
        bottomBar = {
            NoodropBottomBar(currentRoute = currentRoute, navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Dashboard.route,
            modifier         = Modifier.padding(padding),
            enterTransition  = { fadeIn() },
            exitTransition   = { fadeOut() },
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(onLogToday = { navController.navigate(Screen.Tracker.route) })
            }
            composable(Screen.Stack.route)   { StackScreen() }
            composable(Screen.Tracker.route) { TrackerScreen() }
            composable(Screen.Metrics.route) { MetricsScreen() }
            composable(Screen.Library.route) { LibraryScreen() }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoodropTopBar(
    currentRoute: String?,
    isDark: Boolean,
    onToggleDark: () -> Unit,
    onSignOut: () -> Unit,
    onProfileClick: () -> Unit,
) {
    val title = when (currentRoute) {
        Screen.Stack.route   -> "My Stack"
        Screen.Tracker.route -> "Daily Tracker"
        Screen.Metrics.route -> "Metrics"
        Screen.Library.route -> "Protocols"
        else                 -> "noodrop"
    }
    TopAppBar(
        title = {
            Text(
                text  = title,
                style = MaterialTheme.typography.headlineMedium,
                color = if (currentRoute == Screen.Dashboard.route || currentRoute == null)
                    NdOrange else MaterialTheme.colorScheme.onBackground,
            )
        },
        actions = {
            IconButton(onClick = onToggleDark) {
                Text(
                    if (isDark) "\u2600\uFE0F" else "\uD83C\uDF19",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            IconButton(onClick = onProfileClick) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor    = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

// ── Bottom nav (Dropset-style pill indicator) ─────────────────────────────────
@Composable
private fun NoodropBottomBar(currentRoute: String?, navController: NavController) {
    Box(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).navigationBarsPadding()
    ) {
        HorizontalDivider(Modifier.align(Alignment.TopCenter), color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
        Row(
            Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.screen.route
                Box(
                    Modifier.weight(1f).fillMaxHeight().clickable {
                        navController.navigate(item.screen.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) NdOrange.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector        = item.icon,
                                contentDescription = item.label,
                                tint               = if (selected) NdOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(22.dp),
                            )
                        }
                        Text(
                            item.label,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = if (selected) NdOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

// ── Splash ────────────────────────────────────────────────────────────────────
@Composable
private fun SplashScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("noodrop", style = MaterialTheme.typography.displayMedium, color = NdOrange)
            CircularProgressIndicator(color = NdOrange, strokeWidth = 2.dp)
        }
    }
}
