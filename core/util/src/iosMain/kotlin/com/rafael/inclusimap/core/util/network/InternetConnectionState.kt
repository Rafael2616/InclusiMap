package com.rafael.inclusimap.core.util.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class InternetConnectionState {
    actual val state: StateFlow<Boolean> = MutableStateFlow(false)

    actual fun unregister() { }
}
