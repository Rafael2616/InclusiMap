package com.rafael.inclusimap.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {
    @Serializable
    data object MapScreen : Destination

    @Serializable
    data class LoginScreen(val isEditPasswordMode: Boolean = false) : Destination

    @Serializable
    data object SettingsScreen : Destination

    @Serializable
    data object LibraryScreen : Destination

    @Serializable
    data object AboutScreen : Destination

    @Serializable
    data object ContributionsScreen : Destination
}
