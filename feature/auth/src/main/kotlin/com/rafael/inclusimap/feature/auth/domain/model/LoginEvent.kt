package com.rafael.inclusimap.feature.auth.domain.model

import androidx.compose.ui.graphics.ImageBitmap

sealed interface LoginEvent {
    data class SetIsNewUser(val isNewUser: Boolean) : LoginEvent
    data class OnLogin(val registeredUser: RegisteredUser) : LoginEvent
    data class OnRegisterNewUser(val user: User) : LoginEvent
    data object OnLogout : LoginEvent
    data class UpdatePassword(val password: String) : LoginEvent
    data class SetIsPasswordChanged(val isChanged: Boolean) : LoginEvent
    data class DeleteAccount(val keepContributions: Boolean) : LoginEvent
    data class UpdateUserName(val name: String) : LoginEvent
    data class OnAddEditUserProfilePicture(val image: ImageBitmap) : LoginEvent
    data object OnRemoveUserProfilePicture : LoginEvent
    data class OnAllowPictureOptedIn(val value: Boolean) : LoginEvent
}
