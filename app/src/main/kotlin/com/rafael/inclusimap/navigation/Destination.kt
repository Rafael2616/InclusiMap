package com.rafael.inclusimap.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {
    @Serializable
    object MapScreen : Destination
    @Serializable
    object LoginScreen : Destination
    @Serializable
    object AppIntroScreen : Destination
}