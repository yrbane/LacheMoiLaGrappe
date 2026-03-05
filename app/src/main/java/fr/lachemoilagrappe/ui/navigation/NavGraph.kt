package fr.lachemoilagrappe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.lachemoilagrappe.BuildConfig
import fr.lachemoilagrappe.ui.screens.debug.DebugScreen
import fr.lachemoilagrappe.ui.screens.history.HistoryScreen
import fr.lachemoilagrappe.ui.screens.home.HomeScreen
import fr.lachemoilagrappe.ui.screens.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToDebug = if (BuildConfig.DEBUG) {
                    { navController.navigate(Screen.Debug.route) }
                } else null
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        if (BuildConfig.DEBUG) {
            composable(Screen.Debug.route) {
                DebugScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
