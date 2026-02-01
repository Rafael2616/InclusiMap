package com.rafael.inclusimap.feature.intro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafael.inclusimap.feature.intro.domain.model.AppIntroEntity
import com.rafael.inclusimap.feature.intro.domain.model.AppIntroState
import com.rafael.inclusimap.feature.intro.domain.repository.AppIntroRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppIntroViewModel(
    private val appIntroRepository: AppIntroRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AppIntroState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val appIntroEntity = appIntroRepository.getAppIntro(1) ?: AppIntroEntity.getDefault()
            _state.update {
                it.copy(
                    showAppIntro = appIntroEntity.showAppIntro,
                )
            }
        }
    }

    fun setShowAppIntro(showAppIntro: Boolean) {
        _state.update {
            it.copy(
                showAppIntro = showAppIntro,
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val appIntroEntity = appIntroRepository.getAppIntro(1) ?: AppIntroEntity.getDefault()
            appIntroEntity.showAppIntro = showAppIntro
            appIntroRepository.updateAppIntro(appIntroEntity)
        }
    }
}
