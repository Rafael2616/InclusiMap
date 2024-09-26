package com.rafael.inclusimap.domain

sealed interface LoginEvent {
    data class SetIsNewUser(val isNewUser: Boolean) : LoginEvent
    data class SetUser(val user: User) : LoginEvent
}
