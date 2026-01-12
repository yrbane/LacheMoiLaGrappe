package com.callfilter.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object SpamDb : Screen("spam_db")
    data object SmsTemplate : Screen("sms_template")
    data object UserLists : Screen("user_lists")
}
