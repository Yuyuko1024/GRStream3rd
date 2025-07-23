package net.hearnsoft.gr3rd.compose.ui.screens

sealed class ScreenRoute(val route: String) {
    object Home : ScreenRoute("home")
    object Radio : ScreenRoute("radio")
    object Library : ScreenRoute("library")
    object History : ScreenRoute("history")
    object Account : ScreenRoute("account")
}