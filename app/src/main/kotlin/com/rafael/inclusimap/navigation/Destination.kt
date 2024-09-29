package com.rafael.inclusimap.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {
    @Serializable
    data object MapScreen : Destination
    @Serializable
    data object LoginScreen : Destination
    @Serializable
    data object SettingsScreen : Destination
}