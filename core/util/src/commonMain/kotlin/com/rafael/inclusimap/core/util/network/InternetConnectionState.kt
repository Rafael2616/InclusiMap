package com.rafael.inclusimap.core.util.network

import kotlinx.coroutines.flow.StateFlow

expect class InternetConnectionState() {
    val state: StateFlow<Boolean>

    fun unregister()
}
