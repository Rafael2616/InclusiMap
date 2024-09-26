package com.rafael.inclusimap.domain

data class LoginState(
    val user: User? = null,
    val isNewUser: Boolean = false,
)