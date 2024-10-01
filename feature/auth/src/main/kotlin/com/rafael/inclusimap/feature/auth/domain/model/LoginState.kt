package com.rafael.inclusimap.feature.auth.domain.model

import com.rafael.inclusimap.core.domain.model.DeleteProcess

data class LoginState(
    val user: User? = null,
    val isNewUser: Boolean = false,
    val userPathID: String? = null,
    val isLoggedIn: Boolean = true,
    val isRegistering: Boolean = false,
    val isUpdatingPassword: Boolean = false,
    val isSamePassword: Boolean = false,
    val isPasswordChanged: Boolean = false,
    val isLoginOut: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val deleteStep: DeleteProcess = DeleteProcess.NO_OP,
    val isAccountDeleted: Boolean = false,
    val userAlreadyRegistered: Boolean = false,
    val isPasswordCorrect: Boolean = true,
)
