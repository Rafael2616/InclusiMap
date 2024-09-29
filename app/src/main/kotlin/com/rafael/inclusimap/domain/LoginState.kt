package com.rafael.inclusimap.domain

data class LoginState(
    val user: User? = null,
    val isNewUser: Boolean = false,
    val userPathID: String? = null,
    val isLoggedIn: Boolean = true,
    val isRegistering : Boolean = false,
    val isUpdatingPassword : Boolean = false,
    val isSamePassword : Boolean = false,
    val isPasswordChanged : Boolean = false,
    val isLoginOut : Boolean = false,
    val userAlreadyRegistered : Boolean = false,
    val isPasswordCorrect : Boolean = true
)
