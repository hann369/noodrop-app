package com.noodrop.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.noodrop.app.ui.stack.StackScreen
import com.noodrop.app.ui.theme.*
import com.noodrop.app.ui.tracker.TrackerScreen
import com.noodrop.app.ui.profile.ProfileSheet
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// - Root ViewModel -
data class RootState(
    val authState: AuthState = AuthState.Loading,
    val isDark: Boolean      = false,
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
                RootState(authState = auth, isDark = dark)
            }.collect { _state.value = it }
        }
    }

    fun toggleDark() = viewModelScope.launch { prefs.setDark(!_state.value.isDark) }
    fun signOut()    = repo.signOut()
}

// - Activity -
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { NoodropRoot() }
    }
}

// - Root composable -
@Composable
fun NoodropRoot(vm: RootViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    NoodropTheme(darkTheme = state.isDark) {
        when (val auth = state.authState) {
            is AuthState.Loading         -> SplashScreen()
            is AuthState.Unauthenticated -> AuthScreen()
            is AuthState.Authenticated   -> MainApp(
                onToggleDark = vm::toggleDark,
                onSignOut    = vm::signOut,
                isDark       = state.isDark,
            )
        }
    }
}

// - Main app -
@Composable
fun MainApp(onToggleDark: () -> Unit, onSignOut: () -> Unit, isDark: Boolean) {
    val navController = rememberNavController()
    val currentEntry  by navController.currentBackStackEntryAsState()
    val currentRoute  = currentEntry?.destination?.route
    var showProfile by remember { mutableStateOf(false) }

    if (showProfile) {
        ProfileSheet(onClose = { showProfile = false })
    }

    Scaffold(
        topBar = {
            NoodropTopBar(currentRoute = currentRoute, isDark = isDark,
                onToggleDark = onToggleDark, onSignOut = onSignOut, onProfileClick = { showProfile = true })
        },
        bottomBar = { NoodropBottomBar(currentRoute = currentRoute, navController = navController) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        NavHost(
            navController     = navController,
            startDestination  = Screen.Dashboard.route,
            modifier          = Modifier.padding(padding),
            enterTransition   = { fadeIn() },
            exitTransition    = { fadeOut() },
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

// - Top bar -
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoodropTopBar(
    currentRoute: String?,
    isDark: Boolean,
    onToggleDark: () -> Unit,
    onSignOut: () -> Unit,
    onProfileClick: () -> Unit,  // NEW PARAMETER
) {
    val title = when (currentRoute) {
        Screen.Stack.route    -> "My Stack"
        Screen.Tracker.route  -> "Daily Tracker"
        Screen.Metrics.route  -> "Metrics"
        Screen.Library.route  -> "Protocols"
        else                  -> "noodrop"
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
                Text(if (isDark) "\u2600\uFE0F" else "\uD83C\uDF19", style = MaterialTheme.typography.bodyLarge)
            }
            IconButton(onClick = onProfileClick) {  // CHANGED - Now opens profile directly
                Icon(Icons.Filled.AccountCircle, contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSurface)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor    = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

// - Bottom nav -
@Composable
private fun NoodropBottomBar(currentRoute: String?, navController: NavController) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick  = {
                    navController.navigate(item.screen.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon  = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = NdOrange,
                    selectedTextColor   = NdOrange,
                    indicatorColor      = NdOrange.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

// - Splash -
@Composable
private fun SplashScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("noodrop", style = MaterialTheme.typography.displayMedium, color = NdOrange)
            CircularProgressIndicator(color = NdOrange, strokeWidth = 2.dp)
        }
    }
}
