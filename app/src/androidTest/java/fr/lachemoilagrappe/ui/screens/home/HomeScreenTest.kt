package fr.lachemoilagrappe.ui.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.lachemoilagrappe.ui.theme.LacheMoiLaGrappeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysAppName() {
        // Start the app
        composeTestRule.setContent {
            LacheMoiLaGrappeTheme {
                HomeScreenContent(
                    uiState = HomeUiState(
                        todayRejectedCount = 5,
                        totalBlockedCount = 42,
                        isLoading = false
                    ),
                    isScreeningEnabled = true,
                    onActivate = {},
                    onNavigateToHistory = {},
                    onNavigateToSettings = {},
                    onNavigateToUserLists = {},
                    onFilterUnknownChanged = {},
                    onAutoSmsChanged = {}
                )
            }
        }

        // Check if app name or key elements are displayed
        composeTestRule.onNodeWithText("LacheMoiLaGrappe", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Aujourd'hui", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("5", substring = true).assertIsDisplayed()
    }
}
