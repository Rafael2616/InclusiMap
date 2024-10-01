package com.rafael.inclusimap.feature.auth.domain.model

sealed interface LoginEvent {
    data class SetIsNewUser(val isNewUser: Boolean) : LoginEvent
    data class OnLogin(val registeredUser: RegisteredUser) : LoginEvent
    data class OnRegisterNewUser(val user: User) : LoginEvent
    data object OnLogout : LoginEvent
    data class UpdatePassword(val password: String) : LoginEvent
    data class SetIsPasswordChanged(val isChanged: Boolean) : LoginEvent
    data class DeleteAccount(val keepContributions: Boolean) : LoginEvent
}
