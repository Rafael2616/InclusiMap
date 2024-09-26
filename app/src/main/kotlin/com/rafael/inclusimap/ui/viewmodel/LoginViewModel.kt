package com.rafael.inclusimap.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.rafael.inclusimap.domain.LoginEvent
import com.rafael.inclusimap.domain.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.SetIsNewUser -> {
                _state.update {
                    it.copy(
                        isNewUser = event.isNewUser
                    )
                }
            }
            is LoginEvent.SetUser -> {
                _state.update {
                    it.copy(
                        user = event.user
                    )
                }
            }
        }
    }
}