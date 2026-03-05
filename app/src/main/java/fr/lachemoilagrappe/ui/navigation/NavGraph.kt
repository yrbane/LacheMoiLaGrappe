package fr.lachemoilagrappe.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.lachemoilagrappe.BuildConfig
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import fr.lachemoilagrappe.ui.screens.debug.DebugScreen
import fr.lachemoilagrappe.ui.screens.history.HistoryScreen
import fr.lachemoilagrappe.ui.screens.home.HomeScreen
import fr.lachemoilagrappe.ui.screens.onboarding.OnboardingScreen
import fr.lachemoilagrappe.ui.screens.settings.SettingsScreen
import fr.lachemoilagrappe.ui.screens.userlists.UserListsScreen
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    settingsRepository: SettingsRepository
) {
    val scope = rememberCoroutineScope()

    // Determine start destination based on onboarding state
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val completed = settingsRepository.getOnboardingCompleted()
        startDestination = if (completed) {
            Screen.Home.route
        } else {
            Screen.Onboarding.route
        }
    }

    // Show a loading indicator until we know the start destination
    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    scope.launch {
                        settingsRepository.setOnboardingCompleted(true)
                    }
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToUserLists = { navController.navigate(Screen.UserLists.route) },
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

        composable(Screen.UserLists.route) {
            UserListsScreen(
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
