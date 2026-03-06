package fr.lachemoilagrappe.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import fr.lachemoilagrappe.domain.repository.SettingsRepository
import fr.lachemoilagrappe.ui.navigation.NavGraph
import fr.lachemoilagrappe.ui.theme.LacheMoiLaGrappeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LacheMoiLaGrappeTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    settingsRepository = settingsRepository
                )
            }
        }
    }
}
