package com.noodrop.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Auth      : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Stack     : Screen("stack")
    object Tracker   : Screen("tracker")
    object Metrics   : Screen("metrics")
    object Library   : Screen("library")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Home",    Icons.Filled.Home),
    BottomNavItem(Screen.Stack,     "Stack",   Icons.Filled.Science),
    BottomNavItem(Screen.Tracker,   "Track",   Icons.Filled.CheckCircle),
    BottomNavItem(Screen.Metrics,   "Metrics", Icons.Filled.BarChart),
    BottomNavItem(Screen.Library,   "Library", Icons.Filled.MenuBook),
)

val appScreens = bottomNavItems.map { it.screen.route }.toSet()
